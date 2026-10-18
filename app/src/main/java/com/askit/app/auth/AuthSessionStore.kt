package com.askit.app.auth

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okio.buffer
import okio.source
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.security.ProviderException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

@Serializable
class StoredAuthSession(val refreshToken: String, val userId: String, val profileComplete: Boolean)

/** Where [AuthViewModel] keeps the refresh token. [AuthSessionStore] is the encrypted on-device implementation. */
interface AuthSessionStorage {
    suspend fun save(session: StoredAuthSession)
    suspend fun load(): StoredAuthSession?
    suspend fun clear()
}

class AuthSessionStore(context: Context, backendBaseUrl: String, private val keyProvider: () -> SecretKey = ::sessionKey) :
    AuthSessionStorage {
    private val file = File(context.noBackupFilesDir, "auth-session")
    private val pendingFile = File(context.noBackupFilesDir, "auth-session.pending")
    private val binding = backendBaseUrl.toHttpUrl().toString().toByteArray(Charsets.UTF_8)

    override suspend fun save(session: StoredAuthSession): Unit = withContext(Dispatchers.IO) {
        require(session.refreshToken.isNotBlank() && session.userId.isNotBlank())
        mutex.withLock {
            val cipher = cipher(Cipher.ENCRYPT_MODE)
            val plaintext = Json.encodeToString(session).toByteArray(Charsets.UTF_8)
            if (plaintext.size > MAX_BYTES - 30) throw IOException("Session exceeds storage limit.")
            val encrypted = try {
                cipher.doFinal(plaintext)
            } catch (_: GeneralSecurityException) {
                throw IOException("Could not encrypt session.")
            } catch (_: ProviderException) {
                throw IOException("Could not encrypt session.")
            } finally {
                plaintext.fill(0)
            }
            try {
                pendingFile.outputStream().use {
                    it.write(1)
                    it.write(cipher.iv.size)
                    it.write(cipher.iv)
                    it.write(encrypted)
                    it.fd.sync()
                }
                ensureActive()
                Files.move(pendingFile.toPath(), file.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
            } finally {
                Files.deleteIfExists(pendingFile.toPath())
            }
        }
    }

    override suspend fun load(): StoredAuthSession? = withContext(Dispatchers.IO) {
        mutex.withLock {
            val input = try {
                file.inputStream()
            } catch (_: FileNotFoundException) {
                if (file.exists()) throw IOException("Stored session is unreadable.")
                return@withLock null
            }
            val bytes = input.source().buffer().use {
                it.request(MAX_BYTES.toLong() + 1)
                if (it.buffer.size > MAX_BYTES) throw IOException("Stored session exceeds storage limit.")
                it.readByteArray()
            }
            if (bytes.size < 30 || bytes[0] != 1.toByte() || bytes[1] != 12.toByte()) {
                throw IOException("Stored session has an invalid format.")
            }
            val cipher = cipher(Cipher.DECRYPT_MODE, bytes.copyOfRange(2, 14))
            val plaintext = try {
                cipher.doFinal(bytes, 14, bytes.size - 14)
            } catch (_: GeneralSecurityException) {
                throw IOException("Stored session could not be authenticated.")
            } catch (_: ProviderException) {
                throw IOException("Stored session could not be authenticated.")
            }
            try {
                val session = Json.decodeFromString<StoredAuthSession>(plaintext.toString(Charsets.UTF_8))
                if (session.refreshToken.isBlank() || session.userId.isBlank()) throw IOException("Stored session is incomplete.")
                session
            } catch (_: SerializationException) {
                throw IOException("Stored session is invalid.")
            } finally {
                plaintext.fill(0)
            }
        }
    }

    override suspend fun clear(): Unit = withContext(Dispatchers.IO) {
        mutex.withLock {
            Files.deleteIfExists(file.toPath())
            Files.deleteIfExists(pendingFile.toPath())
        }
    }

    private fun cipher(mode: Int, iv: ByteArray? = null): Cipher = try {
        Cipher.getInstance("AES/GCM/NoPadding").apply {
            if (iv == null) init(mode, keyProvider()) else init(mode, keyProvider(), GCMParameterSpec(128, iv))
            updateAAD(binding)
        }
    } catch (_: GeneralSecurityException) {
        throw IOException("Secure session storage is unavailable.")
    } catch (_: ProviderException) {
        throw IOException("Secure session storage is unavailable.")
    }

    private companion object {
        const val MAX_BYTES = 131_072
        const val KEY_ALIAS = "AskIT.auth.session.v1"
        val mutex = Mutex()

        fun sessionKey(): SecretKey {
            val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            val existing = keyStore.getKey(KEY_ALIAS, null)
            if (existing is SecretKey) return existing
            return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
                init(
                    KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                        .setKeySize(256)
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .build(),
                )
            }.generateKey()
        }
    }
}
