package com.askit.app.auth

import com.askit.app.R
import com.askit.app.session.SessionProfileStore
import com.askit.app.testing.MainDispatcherRule
import okhttp3.Dispatcher
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.AbstractExecutorService
import java.util.concurrent.TimeUnit

/**
 * The OTP sign-in and session lifecycle of [AuthViewModel] against a scripted backend. Calls run
 * inline (synchronous OkHttp executor + unconfined main dispatcher), so every assertion sees the
 * settled state.
 */
class AuthSessionFlowTest {
    @get:Rule val main = MainDispatcherRule()

    private val backend = FakeBackend()
    private val storage = FakeSessionStorage()
    private val profiles = SessionProfileStore()
    private val now = 1_000L

    private fun viewModel() = AuthViewModel(
        api = AuthApi("https://backend.example/", backend.client),
        clock = { now },
        sessionStore = storage,
        profileStore = profiles,
    )

    private fun signedInWithCode(vm: AuthViewModel) {
        backend.respond(CHALLENGES, 201, """{"challengeId":"c1","expiresInSeconds":300,"retryAfterSeconds":30}""")
        vm.requestCode(PHONE)
        vm.verifyCode("123456")
    }

    @Test fun requestingACodeWipesStoredCredentialsAndOpensCodeEntry() {
        storage.stored = StoredAuthSession("old-refresh", "someone-else", true)
        backend.respond(CHALLENGES, 201, """{"challengeId":"c1","expiresInSeconds":300,"retryAfterSeconds":30}""")
        val vm = viewModel()
        vm.requestCode(PHONE)
        assertNull(storage.stored)
        assertEquals(AuthDestination.Code, vm.state.value.destination)
        assertEquals(now + 30_000, vm.state.value.resendAvailableAtMillis)
    }

    @Test fun wrongCodeFromTheServerSignsNobodyIn() {
        backend.respond(VERIFICATIONS, 401, """{"code":"OTP_INVALID"}""")
        val vm = viewModel()
        signedInWithCode(vm)
        assertEquals(R.string.auth_invalid_code, vm.state.value.errorRes)
        assertEquals("c1", vm.state.value.challenge?.challengeId)
        assertNull(vm.session)
        assertNull(storage.stored)
    }

    @Test fun newUserIsPersistedAndSentToSignup() {
        backend.respond(VERIFICATIONS, 200, session(profileComplete = false))
        val vm = viewModel()
        signedInWithCode(vm)
        assertEquals(AuthDestination.Signup, vm.state.value.destination)
        assertEquals("refresh-1", storage.stored?.refreshToken)
        assertEquals(USER, storage.stored?.userId)
    }

    @Test fun returningUserLoadsTheirAccountAndGoesHome() {
        backend.respond(VERIFICATIONS, 200, session(profileComplete = true))
        backend.respond(ME, 200, account(USER))
        val vm = viewModel()
        signedInWithCode(vm)
        assertEquals(AuthDestination.Home, vm.state.value.destination)
        assertEquals(USER, profiles.profile.value.accountId)
        assertEquals("Bearer access-1", backend.authorization.last())
    }

    @Test fun anAccountForADifferentUserIsNeverLoaded() {
        backend.respond(VERIFICATIONS, 200, session(profileComplete = true))
        backend.respond(ME, 200, account("intruder"))
        val vm = viewModel()
        signedInWithCode(vm)
        assertEquals(R.string.auth_account_load_failed, vm.state.value.errorRes)
        assertFalse(vm.state.value.destination == AuthDestination.Home)
        assertEquals("", profiles.profile.value.accountId)
    }

    @Test fun restoringASessionRotatesTheStoredRefreshToken() {
        storage.stored = StoredAuthSession("refresh-old", USER, true)
        backend.respond(REFRESH, 200, """{"accessToken":"access-2","refreshToken":"refresh-2","expiresInSeconds":900}""")
        backend.respond(ME, 200, account(USER))
        val vm = viewModel()
        vm.restoreSession()
        assertEquals(AuthDestination.Home, vm.state.value.destination)
        assertEquals("refresh-2", storage.stored?.refreshToken)
        assertTrue(backend.bodies.getValue(REFRESH).contains("refresh-old"))
    }

    @Test fun aRejectedRefreshTokenEndsTheSessionInsteadOfRetryingForever() {
        storage.stored = StoredAuthSession("revoked", USER, true)
        backend.respond(REFRESH, 403, "")
        val vm = viewModel()
        vm.restoreSession()
        assertEquals(R.string.auth_session_expired, vm.state.value.errorRes)
        vm.retryAccountLoad()
        assertEquals(1, backend.paths.count { it == REFRESH })
    }

    @Test fun signOutClearsCredentialsAndRevokesTheRefreshToken() {
        backend.respond(VERIFICATIONS, 200, session(profileComplete = false))
        backend.respond(REVOKE, 204, "")
        val vm = viewModel()
        signedInWithCode(vm)
        vm.signOut()
        assertTrue(vm.state.value.signedOut)
        assertNull(vm.session)
        assertNull(storage.stored)
        assertTrue(backend.bodies.getValue(REVOKE).contains("refresh-1"))
    }

    @Test fun signOutStillCompletesLocallyWhenRevocationFails() {
        backend.respond(VERIFICATIONS, 200, session(profileComplete = false))
        backend.respond(REVOKE, 503, """{"code":"UNAVAILABLE"}""")
        val vm = viewModel()
        signedInWithCode(vm)
        vm.signOut()
        assertTrue(vm.state.value.signedOut)
        assertNull(storage.stored)
    }

    @Test fun theDemoSignInIsUnavailableOutsideLocalBuilds() {
        val vm = viewModel()
        vm.enterDemo()
        vm.selectPreviewAccount(PREVIEW_OWNER_ID)
        assertNull(vm.session)
        assertNull(vm.state.value.destination)
    }

    private fun session(profileComplete: Boolean) =
        """{"accessToken":"access-1","refreshToken":"refresh-1","expiresInSeconds":900,"profileComplete":$profileComplete,"userId":"$USER"}"""

    private fun account(userId: String) =
        """{"userId":"$userId","phoneNumber":"$PHONE","profile":{"displayName":"Asha","username":"asha","city":"Karaikal","joinedYear":"2026"}}"""

    private class FakeSessionStorage : AuthSessionStorage {
        var stored: StoredAuthSession? = null

        override suspend fun save(session: StoredAuthSession) {
            stored = session
        }

        override suspend fun load(): StoredAuthSession? = stored

        override suspend fun clear() {
            stored = null
        }
    }

    /** Answers each path with a scripted status and body; unscripted paths get a 500. */
    private class FakeBackend {
        private val responses = mutableMapOf<String, Pair<Int, String>>()
        val paths = mutableListOf<String>()
        val bodies = mutableMapOf<String, String>()
        val authorization = mutableListOf<String?>()

        fun respond(path: String, status: Int, body: String) {
            responses[path] = status to body
        }

        val client: OkHttpClient = OkHttpClient.Builder()
            .dispatcher(Dispatcher(InlineExecutor()))
            .addInterceptor { chain ->
                val request = chain.request()
                val path = request.url.encodedPath.removePrefix("/")
                paths += path
                bodies[path] = request.body?.let { Buffer().also(it::writeTo).readUtf8() }.orEmpty()
                authorization += request.header("Authorization")
                val (status, body) = responses[path] ?: (500 to """{"code":"UNEXPECTED"}""")
                Response.Builder().request(request).protocol(Protocol.HTTP_1_1).code(status).message("test")
                    .body(body.toResponseBody("application/json".toMediaType())).build()
            }
            .build()
    }

    /** Runs OkHttp's async calls on the calling thread, so a request completes before enqueue returns. */
    private class InlineExecutor : AbstractExecutorService() {
        override fun execute(command: Runnable) = command.run()
        override fun shutdown() = Unit
        override fun shutdownNow(): MutableList<Runnable> = mutableListOf()
        override fun isShutdown() = false
        override fun isTerminated() = false
        override fun awaitTermination(timeout: Long, unit: TimeUnit) = true
    }

    private companion object {
        const val PHONE = "+919876543210"
        const val USER = "user-1"
        const val CHALLENGES = "v1/auth/otp/challenges"
        const val VERIFICATIONS = "v1/auth/otp/verifications"
        const val REFRESH = "v1/auth/sessions/refresh"
        const val REVOKE = "v1/auth/sessions/revoke"
        const val ME = "v1/me"
    }
}
