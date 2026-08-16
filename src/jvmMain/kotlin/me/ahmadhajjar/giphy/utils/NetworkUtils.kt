package me.ahmadhajjar.giphy.utils

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

object NetworkUtils {
    private val client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .followRedirects(HttpClient.Redirect.ALWAYS)
        .build()

    const val USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) GiphyInserter/1.0"

    fun downloadBytes(url: String): ByteArray {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("User-Agent", USER_AGENT)
            .timeout(Duration.ofSeconds(30))
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofByteArray())
        
        if (response.statusCode() >= 400) {
            throw Exception("Failed to download image: HTTP ${response.statusCode()} for $url")
        }
        
        return response.body()
    }
}
