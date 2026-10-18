package com.askit.app.jobs

import java.security.MessageDigest
import java.security.SecureRandom

/** Result of checking a job start code. */
sealed interface StartCodeCheck {
    data object Verified : StartCodeCheck
    data class Rejected(val attemptsRemaining: Int) : StartCodeCheck
    data object LockedOut : StartCodeCheck
}

/**
 * Issues and checks the code a seeker shows the provider to start an on-site job.
 *
 * **This is a placeholder for a server-verified contract** (see docs/job-start-code-contract.md).
 * In production the server must generate the code, show it only to the seeker, verify the
 * provider's entry, and enforce expiry and attempt limits; the provider's device must never hold
 * the code. Until that backend exists, [LocalJobStartCodeAuthority] keeps the client honest:
 * codes come from [SecureRandom] (not `kotlin.random`), comparison is constant-time, and entry
 * locks after [LocalJobStartCodeAuthority.maxAttempts] wrong tries per job.
 */
interface JobStartCodeAuthority {
    fun issue(): String
    fun verify(jobId: String, expected: String, entered: String): StartCodeCheck
}

class LocalJobStartCodeAuthority(
    private val random: SecureRandom = SecureRandom(),
    val maxAttempts: Int = DEFAULT_MAX_ATTEMPTS,
) : JobStartCodeAuthority {
    private val failures = mutableMapOf<String, Int>()

    /** Four digits, matching the current entry UI. Leading zeros are allowed. */
    override fun issue(): String = random.nextInt(10_000).toString().padStart(CODE_LENGTH, '0')

    @Synchronized
    override fun verify(jobId: String, expected: String, entered: String): StartCodeCheck {
        val used = failures[jobId] ?: 0
        if (used >= maxAttempts) return StartCodeCheck.LockedOut
        val matches = entered.length == CODE_LENGTH && entered.all { it in '0'..'9' } &&
            MessageDigest.isEqual(expected.toByteArray(), entered.toByteArray())
        if (matches) {
            failures.remove(jobId)
            return StartCodeCheck.Verified
        }
        val next = used + 1
        failures[jobId] = next
        return if (next >= maxAttempts) StartCodeCheck.LockedOut else StartCodeCheck.Rejected(maxAttempts - next)
    }

    companion object {
        const val CODE_LENGTH = 4
        const val DEFAULT_MAX_ATTEMPTS = 5
    }
}
