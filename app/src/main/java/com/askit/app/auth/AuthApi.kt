package com.askit.app.auth

import com.askit.app.network.BackendClient
import com.askit.app.network.BackendResponseException
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.OkHttpClient
import java.io.IOException

@Serializable
data class OtpChallenge(val challengeId: String, val expiresInSeconds: Long, val retryAfterSeconds: Long)

@Serializable
class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val expiresInSeconds: Long,
    val profileComplete: Boolean,
    val userId: String,
)

enum class AuthError { InvalidCode, ExpiredCode, RateLimited, SessionExpired, RequestFailed, InvalidResponse }

@Serializable
class RefreshedTokens(val accessToken: String, val refreshToken: String, val expiresInSeconds: Long)

class AuthApiException(val error: AuthError, val retryAfterSeconds: Long? = null) :
    IOException("Authentication request failed: ${error.name}")

class AuthApi(baseUrl: String, client: OkHttpClient = OkHttpClient()) {
    private val backend = BackendClient(baseUrl, client)
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun requestCode(phoneNumber: String): OtpChallenge {
        val validated = PhoneNumberValidator.validate(phoneNumber)
        require(validated is PhoneValidation.Valid && validated.e164 == phoneNumber) {
            "Phone number must be a valid supported mobile number in E.164 format."
        }
        val body = buildJsonObject { put("phoneNumber", phoneNumber) }.toString()
        val challenge = decode<OtpChallenge>(post("v1/auth/otp/challenges", body, 201))
        if (challenge.challengeId.isBlank() || challenge.expiresInSeconds <= 0 || challenge.retryAfterSeconds < 0) {
            throw AuthApiException(AuthError.InvalidResponse)
        }
        return challenge
    }

    suspend fun verifyCode(challengeId: String, code: String): AuthSession {
        require(challengeId.isNotBlank())
        require(code.length == 6 && code.all { it in '0'..'9' }) { "OTP must contain six ASCII digits." }
        val body = buildJsonObject {
            put("challengeId", challengeId)
            put("code", code)
        }.toString()
        val session = decode<AuthSession>(post("v1/auth/otp/verifications", body, 200))
        if (session.accessToken.isBlank() || session.accessToken.any { it !in '!'..'~' } ||
            session.refreshToken.isBlank() || session.userId.isBlank() || session.expiresInSeconds <= 0
        ) {
            throw AuthApiException(AuthError.InvalidResponse)
        }
        return session
    }

    suspend fun refreshSession(refreshToken: String): RefreshedTokens {
        require(refreshToken.isNotBlank())
        val body = buildJsonObject { put("refreshToken", refreshToken) }.toString()
        val response = try {
            backend.request("v1/auth/sessions/refresh", body, 200)
        } catch (error: BackendResponseException) {
            // A rejected refresh token (revoked, reused, expired, or a bare 401/403 from a proxy)
            // can never succeed on retry. Route it to expired-session recovery instead of a
            // generic Retry that would replay the dead token forever.
            if (isRefreshTokenRejected(error.status)) throw AuthApiException(AuthError.SessionExpired)
            val content = error.body ?: throw AuthApiException(AuthError.InvalidResponse)
            throw responseError(error.status, content)
        }
        val tokens = decode<RefreshedTokens>(response)
        if (tokens.accessToken.isBlank() || tokens.accessToken.any { it !in '!'..'~' } ||
            tokens.refreshToken.isBlank() || tokens.expiresInSeconds <= 0
        ) {
            throw AuthApiException(AuthError.InvalidResponse)
        }
        return tokens
    }

    suspend fun revokeSession(refreshToken: String) {
        require(refreshToken.isNotBlank())
        val body = buildJsonObject { put("refreshToken", refreshToken) }.toString()
        post("v1/auth/sessions/revoke", body, 204)
    }

    suspend fun currentAccount(accessToken: String): CurrentAccount {
        require(accessToken.isNotBlank() && accessToken.all { it in '!'..'~' })
        val response = try {
            backend.request("v1/me", accessToken = accessToken)
        } catch (error: BackendResponseException) {
            if (error.status == 401) throw AuthApiException(AuthError.SessionExpired)
            val content = error.body ?: throw AuthApiException(AuthError.InvalidResponse)
            throw responseError(error.status, content)
        }
        return parseCurrentAccount(response, json)
    }

    private inline fun <reified T> decode(body: String): T = try {
        json.decodeFromString<T>(body)
    } catch (_: SerializationException) {
        throw AuthApiException(AuthError.InvalidResponse)
    }

    private suspend fun post(path: String, body: String, expectedStatus: Int): String = try {
        backend.request(path, body, expectedStatus)
    } catch (error: BackendResponseException) {
        val content = error.body ?: throw AuthApiException(AuthError.InvalidResponse)
        throw responseError(error.status, content)
    }

    private fun responseError(status: Int, body: String): AuthApiException {
        val payload = try {
            json.decodeFromString<ErrorResponse>(body)
        } catch (_: SerializationException) {
            null
        }
        val error = when {
            status == 429 -> AuthError.RateLimited
            status == 401 && payload?.code == "OTP_INVALID" -> AuthError.InvalidCode
            status == 401 && payload?.code == "OTP_EXPIRED" -> AuthError.ExpiredCode
            status == 401 && payload?.code == "SESSION_EXPIRED" -> AuthError.SessionExpired
            else -> AuthError.RequestFailed
        }
        return AuthApiException(error, payload?.retryAfterSeconds?.takeIf { error == AuthError.RateLimited && it >= 0 })
    }

    @Serializable
    private data class ErrorResponse(val code: String, val retryAfterSeconds: Long? = null)
}

/** Any 401 or 403 from the refresh endpoint means the refresh token is unusable, whatever the body says. */
internal fun isRefreshTokenRejected(status: Int): Boolean = status == 401 || status == 403

/**
 * Parses and validates a `GET /v1/me` body (docs/current-account-contract.md). Every failure,
 * including unexpected exceptions while mapping the profile, becomes [AuthError.InvalidResponse].
 */
internal fun parseCurrentAccount(body: String, json: Json): CurrentAccount {
    val account = try {
        json.decodeFromString<AccountResponse>(body)
    } catch (_: IllegalArgumentException) {
        // SerializationException (malformed JSON, missing required field) is an IllegalArgumentException.
        throw AuthApiException(AuthError.InvalidResponse)
    }
    if (account.userId.isBlank() || !account.phoneNumber.matches(Regex("\\+[1-9][0-9]{6,14}"))) {
        throw AuthApiException(AuthError.InvalidResponse)
    }
    val profile = try {
        account.profile?.toSessionProfile(account.phoneNumber, account.userId)
    } catch (error: AuthApiException) {
        throw error
    } catch (_: RuntimeException) {
        throw AuthApiException(AuthError.InvalidResponse)
    }
    return CurrentAccount(account.userId, account.phoneNumber, profile)
}
