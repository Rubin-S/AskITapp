package com.askit.app.auth

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.File

/** Returning-user sign-in must accept exactly what docs/current-account-contract.md promises. */
class AuthContractTest {
    private val json = Json { ignoreUnknownKeys = true }

    private fun me(profile: String) = """{"userId":"u1","phoneNumber":"+919898989898","profile":$profile}"""

    @Test fun documentedSampleResponseSignsIn() {
        val doc = listOf(File("../docs/current-account-contract.md"), File("docs/current-account-contract.md")).first { it.isFile }
        val sample = Regex("```json\\s*(\\{.*?\\})\\s*```", RegexOption.DOT_MATCHES_ALL).find(doc.readText())!!.groupValues[1]
        val account = parseCurrentAccount(sample, json)
        val profile = requireNotNull(account.profile)
        assertEquals("opaque-user-id", account.userId)
        assertEquals("Kavin", profile.displayName)
        assertEquals("Madurai", profile.city)
        assertNull(profile.email)
        assertEquals("", profile.pincode)
        assertEquals("Seeker", profile.activeRole)
    }

    @Test fun profileWithoutEmailOrPincodeIsAccepted() {
        val profile = parseCurrentAccount(me("""{"displayName":"Arun","username":"arun","city":"Chennai","joinedYear":"2024"}"""), json).profile
        assertNotNull(profile)
        assertNull(profile!!.email)
    }

    @Test fun usernameMayBeEmptyUntilAssigned() {
        val profile = parseCurrentAccount(me("""{"displayName":"Arun","username":"","city":"Chennai","joinedYear":"2024"}"""), json).profile
        assertEquals("", profile!!.username)
    }

    @Test fun optionalEmailAndPincodeAreStillValidatedWhenPresent() {
        val good = parseCurrentAccount(me("""{"displayName":"A","username":"a","city":"C","joinedYear":"2024","email":"a@example.com","pincode":"641001"}"""), json)
        assertEquals("a@example.com", good.profile!!.email)
        listOf(""""email":"not-an-email"""", """"pincode":"000"""").forEach { field ->
            assertThrows(AuthApiException::class.java) {
                parseCurrentAccount(me("""{"displayName":"A","username":"a","city":"C","joinedYear":"2024",$field}"""), json)
            }
        }
    }

    @Test fun requiredContractFieldsAreEnforced() {
        listOf(
            """{"displayName":"A","username":"a","joinedYear":"2024"}""", // city missing
            """{"displayName":"A","username":"a","city":" ","joinedYear":"2024"}""", // city blank
            """{"displayName":"","username":"a","city":"C","joinedYear":"2024"}""", // name blank
            """{"displayName":"A","username":"a","city":"C","joinedYear":"24"}""", // year not 4 digits
        ).forEach { profile ->
            val error = assertThrows(AuthApiException::class.java) { parseCurrentAccount(me(profile), json) }
            assertEquals(AuthError.InvalidResponse, error.error)
        }
    }

    @Test fun nullProfileMeansSignupIsIncompleteAndOmittedProfileIsInvalid() {
        assertNull(parseCurrentAccount(me("null"), json).profile)
        assertThrows(AuthApiException::class.java) {
            parseCurrentAccount("""{"userId":"u1","phoneNumber":"+919898989898"}""", json)
        }
    }
}
