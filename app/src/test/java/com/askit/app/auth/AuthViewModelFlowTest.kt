package com.askit.app.auth

import app.cash.turbine.test
import com.askit.app.R
import com.askit.app.session.SessionProfile
import com.askit.app.session.SessionProfileStore
import com.askit.app.testing.MainDispatcherRule
import com.askit.app.testing.awaitItemMatching
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.util.Collections

/** Flow-level tests of the OTP sign-in state machine against a fake backend and session storage. */
class AuthViewModelFlowTest {
    @get:Rule val main = MainDispatcherRule()

    private val phone = "+919876543210"
    private var now = 1_000L
    private val storage = FakeSessionStorage()
    private val requests: MutableList<Pair<String, String>> = Collections.synchronizedList(mutableListOf())
    private val responses = mutableMapOf<String, Pair<Int, String>>()

    private val api = AuthApi(
        "https://backend.example/",
        OkHttpClient.Builder().addInterceptor { chain ->
            val path = chain.request().url.encodedPath.removePrefix("/")
            val body = chain.request().body?.let { Buffer().also(it::writeTo).readUtf8() }.orEmpty()
            requests += path to body
            val (status, content) = responses[path] ?: (500 to """{"code":"UNEXPECTED"}""")
            Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1).code(status).message("test")
                .body(content.toResponseBody("application/json".toMediaType())).build()
        }.build(),
    )

    private fun viewModel() = AuthViewModel(
        api = api,
        clock = { now },
        sessionStore = storage,
        profileStore = SessionProfileStore(),
    )

    /**
     * Revocation is fire-and-forget on an IO thread, so the signed-out state can be emitted before
     * the server has seen the request. Poll (real time) instead of asserting immediately.
     */
    private fun awaitRequest(path: String): Pair<String, String> {
        val deadline = System.currentTimeMillis() + REQUEST_WAIT_MS
        while (System.currentTimeMillis() < deadline) {
            synchronized(requests) { requests.filter { it.first == path } }.let { matches ->
                if (matches.isNotEmpty()) return matches.single()
            }
            Thread.sleep(POLL_MS)
        }
        throw AssertionError("no $path request within $REQUEST_WAIT_MS ms")
    }

    private fun respond(path: String, status: Int, body: String) {
        responses[path] = status to body
    }

    private fun challengeOk(retryAfter: Long = 30) =
        respond(CHALLENGES, 201, """{"challengeId":"c1","expiresInSeconds":300,"retryAfterSeconds":$retryAfter}""")

    private fun paths() = synchronized(requests) { requests.map { it.first } }

    @Test fun invalidPhoneIsRejectedWithoutCallingTheBackend() = runTest {
        val vm = viewModel()
        vm.requestCode("12")
        assertEquals(R.string.auth_invalid_phone, vm.state.value.errorRes)
        assertTrue(requests.isEmpty())
        assertEquals(0, storage.clears)
    }

    @Test fun requestingACodeClearsOldCredentialsAndOpensCodeEntry() = runTest {
        challengeOk(retryAfter = 30)
        val vm = viewModel()
        vm.state.test {
            vm.requestCode(phone)
            val code = awaitItemMatching { it.destination == AuthDestination.Code }
            assertEquals("c1", code.challenge?.challengeId)
            assertEquals(phone, code.phoneNumber)
            assertNull(code.operation)
            assertEquals(now + 30_000, code.resendAvailableAtMillis)
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(1, storage.clears)
        assertEquals(listOf(CHALLENGES), paths())
        assertTrue(requests.single().second.contains(phone))
    }

    @Test fun rateLimitedRequestShowsCooldownAndStaysOnEntry() = runTest {
        respond(CHALLENGES, 429, """{"code":"RATE_LIMITED","retryAfterSeconds":60}""")
        val vm = viewModel()
        vm.state.test {
            vm.requestCode(phone)
            val limited = awaitItemMatching { it.errorRes != null }
            assertEquals(R.string.auth_rate_limited, limited.errorRes)
            assertNull(limited.destination)
            assertNull(limited.operation)
            assertEquals(now + 60_000, limited.resendAvailableAtMillis)
            cancelAndIgnoreRemainingEvents()
        }
        // A second tap inside the cooldown never reaches the backend.
        vm.requestCode(phone)
        assertEquals(1, paths().size)
    }

    @Test fun malformedCodeIsRejectedLocally() = runTest {
        challengeOk()
        val vm = viewModel()
        vm.state.test {
            vm.requestCode(phone)
            awaitItemMatching { it.destination == AuthDestination.Code }
            vm.verifyCode("12ab")
            assertEquals(R.string.auth_invalid_code, awaitItemMatching { it.errorRes != null }.errorRes)
            cancelAndIgnoreRemainingEvents()
        }
        assertFalse(VERIFICATIONS in paths())
    }

    @Test fun wrongCodeFromServerKeepsTheChallengeForAnotherTry() = runTest {
        challengeOk()
        respond(VERIFICATIONS, 401, """{"code":"OTP_INVALID"}""")
        val vm = viewModel()
        vm.state.test {
            vm.requestCode(phone)
            awaitItemMatching { it.destination == AuthDestination.Code }
            vm.navigationHandled()
            vm.verifyCode("000000")
            val failed = awaitItemMatching { it.errorRes != null }
            assertEquals(R.string.auth_invalid_code, failed.errorRes)
            assertNull(failed.operation)
            assertEquals("c1", failed.challenge?.challengeId)
            cancelAndIgnoreRemainingEvents()
        }
        assertNull(vm.session)
        assertTrue(storage.saved.isEmpty())
    }

    @Test fun verifiedNewUserIsPersistedAndSentToSignup() = runTest {
        challengeOk()
        respond(VERIFICATIONS, 200, session(profileComplete = false))
        val vm = viewModel()
        vm.state.test {
            vm.requestCode(phone)
            awaitItemMatching { it.destination == AuthDestination.Code }
            vm.navigationHandled()
            vm.verifyCode("123456")
            val signup = awaitItemMatching { it.destination == AuthDestination.Signup }
            assertNull(signup.challenge)
            assertNull(signup.operation)
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals("u1", vm.session?.userId)
        assertEquals("refresh-1", storage.saved.last().refreshToken)
        assertFalse(storage.saved.last().profileComplete)
    }

    @Test fun storageFailureWhileSavingTheSessionIsReportedNotSwallowed() = runTest {
        challengeOk()
        respond(VERIFICATIONS, 200, session(profileComplete = false))
        storage.failSaves = true
        val vm = viewModel()
        vm.state.test {
            vm.requestCode(phone)
            awaitItemMatching { it.destination == AuthDestination.Code }
            vm.navigationHandled()
            vm.verifyCode("123456")
            assertEquals(R.string.auth_session_storage_failed, awaitItemMatching { it.errorRes != null }.errorRes)
            cancelAndIgnoreRemainingEvents()
        }
        assertNull(vm.session)
    }

    @Test fun signOutClearsStorageRevokesTheTokenAndEndsSignedOut() = runTest {
        challengeOk()
        respond(VERIFICATIONS, 200, session(profileComplete = false))
        respond(REVOKE, 204, "")
        val vm = viewModel()
        vm.state.test {
            vm.requestCode(phone)
            awaitItemMatching { it.destination == AuthDestination.Code }
            vm.verifyCode("123456")
            awaitItemMatching { it.destination == AuthDestination.Signup }
            vm.signOut()
            val signedOut = awaitItemMatching { it.signedOut }
            assertNull(signedOut.errorRes)
            cancelAndIgnoreRemainingEvents()
        }
        assertNull(vm.session)
        assertNull(storage.stored)
        assertEquals(2, storage.clears) // once before requesting the code, once on sign-out
        val revoke = awaitRequest(REVOKE)
        assertTrue(revoke.second.contains("refresh-1"))
        // Signed out is terminal for this view model: a stray request does nothing.
        vm.requestCode(phone)
        assertEquals(1, paths().count { it == CHALLENGES })
    }

    @Test fun offlineRevocationStillSignsOutLocally() = runTest {
        storage.stored = StoredAuthSession("refresh-stored", "u1", profileComplete = true)
        respond(REVOKE, 503, """{"code":"UNAVAILABLE"}""")
        val vm = viewModel()
        vm.state.test {
            vm.signOut()
            assertTrue(awaitItemMatching { it.signedOut }.signedOut)
            cancelAndIgnoreRemainingEvents()
        }
        assertNull(storage.stored)
        assertTrue(awaitRequest(REVOKE).second.contains("refresh-stored"))
    }

    @Test fun clientPreviewAcceptsOnlyTheDemoCode() = runTest {
        val vm = AuthViewModel(
            clientPreview = true,
            clock = { now },
            profileStore = SessionProfileStore(),
            previewProfileLoader = { id -> SessionProfile(accountId = id, displayName = "Preview owner") },
        )
        vm.signOut()
        vm.state.test {
            vm.requestCode(phone)
            assertNotNull(awaitItemMatching { it.destination == AuthDestination.Code }.challenge)
            vm.verifyCode("000000")
            assertEquals(R.string.auth_invalid_code, awaitItemMatching { it.errorRes != null }.errorRes)
            vm.verifyCode("123456")
            assertNull(awaitItemMatching { it.destination == AuthDestination.Signup }.errorRes)
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals("preview-signup-" + vm.state.value.phoneNumber.filter(Char::isDigit), vm.session?.userId)
        assertTrue(requests.isEmpty())
    }

    private fun session(profileComplete: Boolean) =
        """{"accessToken":"access-1","refreshToken":"refresh-1","expiresInSeconds":900,"profileComplete":$profileComplete,"userId":"u1"}"""

    private class FakeSessionStorage : AuthSessionStorage {
        @Volatile var stored: StoredAuthSession? = null
        val saved = Collections.synchronizedList(mutableListOf<StoredAuthSession>())

        @Volatile var clears = 0

        @Volatile var failSaves = false

        override suspend fun save(session: StoredAuthSession) {
            if (failSaves) throw java.io.IOException("disk full")
            saved += session
            stored = session
        }

        override suspend fun load(): StoredAuthSession? = stored

        override suspend fun clear() {
            clears++
            stored = null
        }
    }

    private companion object {
        const val CHALLENGES = "v1/auth/otp/challenges"
        const val VERIFICATIONS = "v1/auth/otp/verifications"
        const val REVOKE = "v1/auth/sessions/revoke"
        const val REQUEST_WAIT_MS = 5_000L
        const val POLL_MS = 10L
    }
}
