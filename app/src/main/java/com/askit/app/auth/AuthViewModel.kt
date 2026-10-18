package com.askit.app.auth

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.askit.app.R
import com.askit.app.session.SessionProfileStore
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException
import java.util.concurrent.TimeUnit

enum class AuthOperation { RequestCode, ResendCode, VerifyCode, SaveSession, RestoreSession, LoadAccount, SignOut }
enum class AuthDestination { Entry, Code, Signup, Home }

data class AuthUiState(
    val phoneNumber: String = "",
    val challenge: OtpChallenge? = null,
    val operation: AuthOperation? = null,
    val errorRes: Int? = null,
    val destination: AuthDestination? = null,
    val resendAvailableAtMillis: Long = 0,
    val signedOut: Boolean = false,
) {
    val isSubmitting: Boolean get() = operation != null
    val isResending: Boolean get() = operation == AuthOperation.ResendCode
}

class AuthViewModel(
    val clientPreview: Boolean = false,
    private val api: AuthApi? = null,
    private val clock: () -> Long = SystemClock::elapsedRealtime,
    private val sessionStore: AuthSessionStore? = null,
    val profileStore: SessionProfileStore = SessionProfileStore(),
    private val previewProfileLoader: (String) -> com.askit.app.session.SessionProfile? = { null },
) : ViewModel() {
    private val mutableState = MutableStateFlow(AuthUiState())
    val state = mutableState.asStateFlow()
    private var request: Job? = null
    private var sessionPendingStorage: AuthSession? = null
    private var accessExpiresAtMillis: Long = 0
    private var restorationStarted = false
    private var refreshTokenToRevoke: String? = null
    var session: AuthSession? = null
        private set

    private val previewProfiles = mutableMapOf<String, com.askit.app.session.SessionProfile>()

    init {
        if (clientPreview) selectPreviewAccount("preview-owner")
    }

    fun selectPreviewAccount(id: String) {
        if (!clientPreview) return
        session?.userId?.let { previewProfiles[it] = profileStore.profile.value }
        val next = runCatching { previewProfiles[id] ?: checkNotNull(previewProfileLoader(id)) }
            .getOrElse { mutableState.value = mutableState.value.copy(errorRes = R.string.profile_load_error); return }
        profileStore.replaceAccount(next)
        session = AuthSession("local-preview", "local-preview", 86_400, true, id)
        mutableState.value = AuthUiState(phoneNumber = profileStore.profile.value.phoneNumber, destination = AuthDestination.Home)
    }

    fun restoreSession() {
        if (restorationStarted || state.value.signedOut || request?.isActive == true || session != null || state.value.challenge != null) return
        restorationStarted = true
        if (api == null || sessionStore == null) {
            mutableState.value = AuthUiState(destination = AuthDestination.Entry)
            return
        }
        mutableState.value = AuthUiState(operation = AuthOperation.RestoreSession)
        retryAccountLoad()
    }

    fun retryAccountLoad() {
        if (state.value.operation !in setOf(AuthOperation.RestoreSession, AuthOperation.LoadAccount) ||
            request?.isActive == true || state.value.errorRes == R.string.auth_session_expired
        ) return
        mutableState.value = state.value.copy(errorRes = null)
        request = viewModelScope.launch {
            try {
                loadAccount()
            } catch (error: IOException) {
                ensureActive()
                accountLoadFailed(error)
            }
        }
    }

    private suspend fun loadAccount() {
        val api = checkNotNull(api)
        val store = checkNotNull(sessionStore)
        sessionPendingStorage?.let { persistSession(it, store) }
        val current = session
        if (current == null) {
            val stored = store.load()
            if (stored == null) {
                mutableState.value = AuthUiState(destination = AuthDestination.Entry)
                return
            }
            refreshSession(stored, api, store)
        } else if (clock() >= accessExpiresAtMillis) {
            refreshSession(StoredAuthSession(current.refreshToken, current.userId, current.profileComplete), api, store)
        }
        mutableState.value = state.value.copy(operation = AuthOperation.LoadAccount)
        val authenticated = checkNotNull(session)
        val account = try {
            api.currentAccount(authenticated.accessToken)
        } catch (error: AuthApiException) {
            if (error.error != AuthError.SessionExpired) throw error
            refreshSession(StoredAuthSession(authenticated.refreshToken, authenticated.userId, authenticated.profileComplete), api, store)
            api.currentAccount(checkNotNull(session).accessToken)
        }
        val loadedSession = checkNotNull(session)
        if (account.userId != loadedSession.userId) throw AuthApiException(AuthError.InvalidResponse)
        val profileComplete = account.profile != null
        if (profileComplete != loadedSession.profileComplete) {
            persistSession(AuthSession(
                loadedSession.accessToken, loadedSession.refreshToken, loadedSession.expiresInSeconds,
                profileComplete, loadedSession.userId,
            ), store)
        }
        account.profile?.let(profileStore::replaceAccount)
        mutableState.value = AuthUiState(
            phoneNumber = account.phoneNumber,
            destination = if (profileComplete) AuthDestination.Home else AuthDestination.Signup,
        )
    }

    private suspend fun refreshSession(stored: StoredAuthSession, api: AuthApi, store: AuthSessionStore) {
        val tokens = api.refreshSession(stored.refreshToken)
        accessExpiresAtMillis = retryDeadline(tokens.expiresInSeconds)
        persistSession(AuthSession(
            tokens.accessToken, tokens.refreshToken, tokens.expiresInSeconds, stored.profileComplete, stored.userId,
        ), store)
    }

    private suspend fun persistSession(value: AuthSession, store: AuthSessionStore) {
        sessionPendingStorage = value
        store.save(StoredAuthSession(value.refreshToken, value.userId, value.profileComplete))
        sessionPendingStorage = null
        session = value
    }

    private fun accountLoadFailed(error: IOException) {
        mutableState.value = state.value.copy(errorRes = when {
            sessionPendingStorage != null -> R.string.auth_session_storage_failed
            error is AuthApiException && error.error == AuthError.SessionExpired -> R.string.auth_session_expired
            else -> R.string.auth_account_load_failed
        })
    }

    fun requestCode(phoneNumber: String) {
        if (state.value.isSubmitting || state.value.signedOut) return
        val validated = PhoneNumberValidator.validate(phoneNumber)
        if (validated !is PhoneValidation.Valid) {
            mutableState.value = AuthUiState(errorRes = R.string.auth_invalid_phone)
            return
        }
        val normalized = validated.e164
        if (clientPreview) {
            session?.userId?.let { previewProfiles[it] = profileStore.profile.value }
            session = null
            mutableState.value = AuthUiState(phoneNumber = normalized,
                challenge = OtpChallenge("preview-${clock()}", 300, 0), destination = AuthDestination.Code)
            return
        }
        if (api == null || sessionStore == null) {
            mutableState.value = AuthUiState(errorRes = R.string.auth_not_configured)
            return
        }
        if (normalized == state.value.phoneNumber && clock() < state.value.resendAvailableAtMillis) {
            mutableState.value = state.value.copy(errorRes = R.string.auth_rate_limited)
            return
        }
        mutableState.value = AuthUiState(phoneNumber = normalized, operation = AuthOperation.RequestCode)
        session = null
        sessionPendingStorage = null
        submitCodeRequest(api, sessionStore, normalized, isResend = false)
    }

    fun resendCode() {
        val current = state.value
        if (clientPreview && !current.isSubmitting && current.challenge != null) {
            mutableState.value = current.copy(challenge = OtpChallenge("preview-${clock()}", 300, 0), errorRes = null)
            return
        }
        if (current.isSubmitting || current.signedOut || session != null || sessionPendingStorage != null || api == null || sessionStore == null || current.challenge == null) return
        if (clock() < current.resendAvailableAtMillis) return
        mutableState.value = current.copy(operation = AuthOperation.ResendCode, errorRes = null)
        submitCodeRequest(api, sessionStore, current.phoneNumber, isResend = true)
    }

    private fun submitCodeRequest(api: AuthApi, store: AuthSessionStore, phoneNumber: String, isResend: Boolean) {
        request = viewModelScope.launch {
            var storageReady = isResend
            try {
                if (!isResend) store.clear()
                storageReady = true
                val challenge = api.requestCode(phoneNumber)
                mutableState.value = AuthUiState(
                    phoneNumber = phoneNumber,
                    challenge = challenge,
                    destination = if (isResend) null else AuthDestination.Code,
                    resendAvailableAtMillis = retryDeadline(challenge.retryAfterSeconds),
                )
            } catch (error: IOException) {
                ensureActive()
                val retryAfter = (error as? AuthApiException)?.retryAfterSeconds
                mutableState.value = state.value.copy(
                    operation = null,
                    resendAvailableAtMillis = retryAfter?.let(::retryDeadline) ?: state.value.resendAvailableAtMillis,
                    errorRes = if (!storageReady) {
                        R.string.auth_session_storage_failed
                    } else if (error is AuthApiException && error.error == AuthError.RateLimited) {
                        R.string.auth_rate_limited
                    } else {
                        R.string.auth_request_failed
                    },
                )
            }
        }
    }

    fun verifyCode(code: String) {
        if (state.value.isSubmitting || state.value.signedOut || session != null) return
        val challenge = state.value.challenge
        if (clientPreview && challenge != null) {
            if (code != "123456") {
                mutableState.value = state.value.copy(errorRes = R.string.auth_invalid_code)
            } else {
                val signupId = "preview-signup"
                val signupProfile = com.askit.app.session.SessionProfile(
                    accountId = signupId, displayName = "", username = "", bio = "", about = "", city = "",
                    joinedYear = "", lookingFor = emptyList(), languages = emptyList(),
                    phoneNumber = state.value.phoneNumber, profileStrengthPercent = 0,
                )
                runCatching { profileStore.replaceAccount(signupProfile) }.getOrElse {
                    mutableState.value = state.value.copy(errorRes = R.string.profile_load_error)
                    return
                }
                session = AuthSession("local-preview", "local-preview", 86_400, false, signupId)
                mutableState.value = state.value.copy(challenge = null, errorRes = null, destination = AuthDestination.Signup)
            }
            return
        }
        if (api == null || challenge == null || sessionStore == null) {
            mutableState.value = state.value.copy(errorRes = R.string.auth_new_code_required)
            return
        }
        if (sessionPendingStorage == null && (code.length != 6 || code.any { it !in '0'..'9' })) {
            mutableState.value = state.value.copy(errorRes = R.string.auth_invalid_code)
            return
        }
        mutableState.value = state.value.copy(operation = AuthOperation.VerifyCode, errorRes = null)
        request = viewModelScope.launch {
            try {
                val verified = sessionPendingStorage ?: api.verifyCode(challenge.challengeId, code).also {
                    accessExpiresAtMillis = retryDeadline(it.expiresInSeconds)
                }
                mutableState.value = state.value.copy(operation = AuthOperation.SaveSession)
                persistSession(verified, sessionStore)
                if (verified.profileComplete) {
                    mutableState.value = state.value.copy(operation = AuthOperation.LoadAccount, challenge = null)
                    loadAccount()
                } else {
                    mutableState.value = state.value.copy(operation = null, challenge = null, destination = AuthDestination.Signup)
                }
            } catch (error: IOException) {
                ensureActive()
                if (state.value.operation == AuthOperation.LoadAccount) {
                    accountLoadFailed(error)
                    return@launch
                }
                val retryAfter = (error as? AuthApiException)?.retryAfterSeconds
                mutableState.value = state.value.copy(
                    operation = null,
                    resendAvailableAtMillis = retryAfter?.let(::retryDeadline) ?: state.value.resendAvailableAtMillis,
                    errorRes = if (sessionPendingStorage != null) R.string.auth_session_storage_failed else when ((error as? AuthApiException)?.error) {
                        AuthError.InvalidCode -> R.string.auth_invalid_code
                        AuthError.ExpiredCode -> R.string.auth_expired_code
                        AuthError.RateLimited -> R.string.auth_rate_limited
                        else -> R.string.auth_verification_failed
                    },
                )
            }
        }
    }

    fun navigationHandled() {
        mutableState.value = state.value.copy(destination = null)
    }

    fun signOut() {
        if (clientPreview) {
            session?.userId?.let { previewProfiles[it] = profileStore.profile.value }
            session = null
            mutableState.value = AuthUiState(destination = AuthDestination.Entry)
            return
        }
        if (state.value.signedOut || (state.value.operation == AuthOperation.SignOut && state.value.errorRes == null)) return
        val previousRequest = request
        refreshTokenToRevoke = refreshTokenToRevoke ?: sessionPendingStorage?.refreshToken ?: session?.refreshToken
        session = null
        sessionPendingStorage = null
        mutableState.value = AuthUiState(operation = AuthOperation.SignOut)
        request = viewModelScope.launch {
            previousRequest?.cancelAndJoin()
            if (refreshTokenToRevoke == null) {
                refreshTokenToRevoke = try {
                    sessionStore?.load()?.refreshToken
                } catch (_: IOException) {
                    // Unreadable credentials must still be removable.
                    null
                }
            }
            try {
                sessionStore?.clear()
            } catch (_: IOException) {
                mutableState.value = state.value.copy(errorRes = R.string.auth_logout_storage_failed)
                return@launch
            }
            val token = refreshTokenToRevoke
            if (token != null && api != null) {
                withTimeoutOrNull(5_000) {
                    try {
                        api.revokeSession(token)
                    } catch (_: IOException) {
                        // Offline revocation must not prevent local logout.
                    }
                }
            }
            refreshTokenToRevoke = null
            mutableState.value = AuthUiState(signedOut = true)
        }
    }

    fun cancelPendingRequest(vararg operations: AuthOperation) {
        if (state.value.operation !in setOf(AuthOperation.SignOut, AuthOperation.RestoreSession, AuthOperation.LoadAccount) &&
            state.value.operation in operations
        ) {
            request?.cancel()
            mutableState.value = state.value.copy(operation = null)
        }
    }

    private fun retryDeadline(seconds: Long): Long {
        val now = clock()
        return now + TimeUnit.SECONDS.toMillis(seconds).coerceAtMost(Long.MAX_VALUE - now)
    }
}
