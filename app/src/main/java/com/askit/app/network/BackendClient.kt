package com.askit.app.network

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal class BackendResponseException(val status: Int, val body: String?) : IOException("Invalid backend response.")

internal class BackendClient(baseUrl: String, client: OkHttpClient) {
    private val baseUrl = baseUrl.toHttpUrl().also {
        require(it.isHttps && it.username.isEmpty() && it.password.isEmpty() && it.query == null && it.fragment == null) {
            "Backend requires an HTTPS base URL without credentials, query, or fragment."
        }
    }
    private val client = client.newBuilder()
        .followRedirects(false)
        .followSslRedirects(false)
        .retryOnConnectionFailure(false)
        .callTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun request(path: String, body: String? = null, expectedStatus: Int = 200, accessToken: String? = null): String =
        suspendCancellableCoroutine { continuation ->
            val request = Request.Builder()
                .url(baseUrl.newBuilder().addPathSegments(path).build())
                .header("Accept", "application/json")
                .apply { if (accessToken != null) header("Authorization", "Bearer $accessToken") }
                .apply { if (body != null) post(body.toRequestBody("application/json; charset=utf-8".toMediaType())) }
                .build()
            val call = client.newCall(request)
            continuation.invokeOnCancellation { call.cancel() }
            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        val content = response.use {
                            if (expectedStatus == 204 && it.code == 204) return@use ""
                            val source = it.body?.source() ?: throw BackendResponseException(it.code, null)
                            source.request(65_537)
                            if (source.buffer.size > 65_536) throw BackendResponseException(it.code, null)
                            val content = source.readUtf8()
                            if (it.code != expectedStatus) throw BackendResponseException(it.code, content)
                            content
                        }
                        continuation.resume(content)
                    } catch (e: IOException) {
                        continuation.resumeWithException(e)
                    }
                }
            })
        }
}
