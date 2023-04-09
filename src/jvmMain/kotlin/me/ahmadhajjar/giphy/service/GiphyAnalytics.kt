package me.ahmadhajjar.giphy.service

import com.beust.klaxon.Klaxon
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

object GiphyAnalytics {
    private val client = HttpClient.newBuilder().build()
    private var userId: String? = null
    fun getUserId(): String {
        if (userId != null) {
            return userId as String
        }
        userId = fetchRandomId()
        return userId as String
    }

    fun handleGiphyEvent(giphy: Giphy, event: GiphyEvent) {
        val url = getUrlForEvent(giphy, event) ?: return
        val randomId = getUserId()
        val time = System.currentTimeMillis()
        val uri = URI.create(url + "&ts=${time}&random_id=$randomId")

        val request = HttpRequest.newBuilder()
            .uri(uri)
            .build()
        client.send(request, HttpResponse.BodyHandlers.ofString())
    }

    private fun getUrlForEvent(giphy: Giphy, event: GiphyEvent): String? {
        return when (event) {
            GiphyEvent.LOADED -> giphy.analytics?.onload?.url
            GiphyEvent.SENT -> giphy.analytics?.onsent?.url
            GiphyEvent.CLICKED -> giphy.analytics?.onclick?.url
        }
    }

    private fun fetchRandomId(): String {
        val uri = URI.create("https://api.giphy.com/v1/randomid?api_key=${GiphyService.getApiKey()}")

        val request = HttpRequest.newBuilder()
            .uri(uri)
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        val randomIdResponse = Klaxon().parse<GiphyRandomIdResponse>(response.body())
        return randomIdResponse?.data?.randomId ?: ""
    }
}

enum class GiphyEvent {
    LOADED,
    SENT,
    CLICKED,
}

data class GiphyRandomIdResponse(
    val data: GiphyRandomIdResponseData? = null,
)

data class GiphyRandomIdResponseData(
    val randomId: String? = null,
)
