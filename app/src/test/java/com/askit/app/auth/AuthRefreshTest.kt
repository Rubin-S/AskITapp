package com.askit.app.auth

import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/** A dead refresh token must lead to expired-session recovery, never a Retry loop. */
class AuthRefreshTest {
    private fun api(status: Int, body: String = ""): AuthApi = AuthApi(
        "https://backend.example/",
        OkHttpClient.Builder().addInterceptor { chain ->
            Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1).code(status).message("test")
                .body(body.toResponseBody("application/json".toMediaType())).build()
        }.build(),
    )

    private fun refreshError(status: Int, body: String = ""): AuthError =
        assertThrows(AuthApiException::class.java) { runBlocking { api(status, body).refreshSession("refresh-token") } }.error

    @Test fun anyRejectedRefreshIsAnExpiredSessionNotARetryLoop() {
        assertEquals(AuthError.SessionExpired, refreshError(401))
        assertEquals(AuthError.SessionExpired, refreshError(401, """{"code":"TOKEN_REUSED"}"""))
        assertEquals(AuthError.SessionExpired, refreshError(403))
        assertEquals(AuthError.SessionExpired, refreshError(401, """{"code":"SESSION_EXPIRED"}"""))
    }

    @Test fun otherRefreshFailuresStayRetryable() {
        assertEquals(AuthError.RequestFailed, refreshError(500, """{"code":"INTERNAL"}"""))
        assertEquals(AuthError.RateLimited, refreshError(429, """{"code":"RATE_LIMITED","retryAfterSeconds":5}"""))
        assertTrue(isRefreshTokenRejected(401) && isRefreshTokenRejected(403) && !isRefreshTokenRejected(500))
    }
}
