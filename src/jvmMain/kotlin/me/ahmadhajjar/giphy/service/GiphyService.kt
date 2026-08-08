package me.ahmadhajjar.giphy.service

import com.beust.klaxon.Klaxon
import java.net.URI
import java.net.URL
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.Charset
import javax.swing.ImageIcon


object GiphyService {
    private var nextPage: Int = 0
    private var results: MutableList<Giphy> = mutableListOf()
    private var _searchTerm: String = ""
    private var _currentIndex: Int = -1
    private val client = HttpClient.newBuilder().build()
    private var isFetchingMetadata = false

    private const val PAGE_SIZE = 10
    private val apiKey: String
        get() = me.ahmadhajjar.giphy.config.ConfigService.apiKey

    fun nextGiphy(searchTerm: String): Giphy? {
        val effectiveSearchTerm = searchTerm.ifEmpty { "__TRENDING__" }
        if (effectiveSearchTerm != _searchTerm) {
            _searchTerm = effectiveSearchTerm
            results = mutableListOf()
            nextPage = 0
            _currentIndex = -1
        }

        _currentIndex++

        if (_currentIndex >= results.size) {
            fetchBatch(searchTerm)
        }

        return if (results.isNotEmpty() && _currentIndex < results.size) {
            val currentGiphy = results[_currentIndex]

            // Pre-load images for the next 5 giphies
            val nextIdx = _currentIndex + 1
            val endIdx = (nextIdx + 5).coerceAtMost(results.size)
            if (nextIdx < endIdx) {
                preLoadImages(results.subList(nextIdx, endIdx))
            }

            // If we are reaching the end of the current results, fetch more metadata
            if (_currentIndex >= results.size - 5) {
                fetchBatchAsync(searchTerm)
            }

            currentGiphy
        } else {
            null
        }
    }

    fun previousGiphy(searchTerm: String): Giphy? {
        _currentIndex--
        if (_currentIndex < 0) {
            _currentIndex = 0
        }
        return if (results.isNotEmpty() && _currentIndex < results.size) {
            results[_currentIndex]
        } else {
            null
        }
    }

    private fun fetchBatch(searchTerm: String) {
        if (isFetchingMetadata) return
        isFetchingMetadata = true
        try {
            if (searchTerm.isEmpty()) {
                fetchTrending(searchTerm)
            } else {
                fetchPage(searchTerm)
            }
            nextPage++
        } finally {
            isFetchingMetadata = false
        }
    }

    private fun fetchBatchAsync(searchTerm: String) {
        if (isFetchingMetadata) return
        Thread {
            fetchBatch(searchTerm)
        }.start()
    }

    private fun preLoadImages(giphies: List<Giphy>) {
        Thread {
            giphies.forEach { giphy ->
                if (giphy.icon == null && giphy.id != null && !giphy.isPreloading) {
                    giphy.isPreloading = true
                    try {
                        val mediaUrl = "https://i.giphy.com/media/${giphy.id}/giphy.gif"
                        giphy.icon = ImageIcon(URL(mediaUrl))
                    } catch (e: Exception) {
                        // Ignore
                    } finally {
                        giphy.isPreloading = false
                    }
                }
            }
        }.start()
    }

    private fun fetchTrending(searchTerm: String) {
        val uri = URI.create(
            "https://api.giphy.com/v1/gifs/trending?" +
                    "api_key=$apiKey&" +
                    "limit=$PAGE_SIZE&" +
                    "offset=${PAGE_SIZE * nextPage}&" +
                    "rating=g"
        )
        executeRequest(uri, searchTerm)
    }

    private fun fetchPage(searchTerm: String) {
        val urlEncodedSearchTerm = URLEncoder.encode(searchTerm, Charset.defaultCharset())

        val uri = URI.create(
            "https://api.giphy.com/v1/gifs/search?" +
                    "api_key=$apiKey&" +
                    "q=$urlEncodedSearchTerm&" +
                    "limit=$PAGE_SIZE&" +
                    "offset=${PAGE_SIZE * nextPage}&" +
                    "random_id=${GiphyAnalytics.getUserId()}&" +
                    "rating=g&" +
                    "lang=en"
        )
        executeRequest(uri, searchTerm)
    }

    fun randomGiphy(): Giphy? {
        val uri = URI.create(
            "https://api.giphy.com/v1/gifs/random?" +
                    "api_key=$apiKey&" +
                    "rating=g"
        )
        val request = HttpRequest.newBuilder()
            .uri(uri)
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        val giphyResponse = Klaxon().parse<GiphyRandomResponse>(response.body())
        return giphyResponse?.data
    }

    private fun executeRequest(uri: URI, searchTerm: String) {
        val request = HttpRequest.newBuilder()
            .uri(uri)
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        val giphyResponse = Klaxon().parse<GiphyResponse>(response.body())
        val newGiphies = giphyResponse?.data ?: emptyList()

        val effectiveSearchTerm = if (searchTerm.isEmpty()) "__TRENDING__" else searchTerm
        if (effectiveSearchTerm == _searchTerm) {
            results.addAll(newGiphies)
        }
    }
}

data class GiphyResponse(
    val data: List<Giphy>,
    // todo later add more meta data
)

data class GiphyRandomResponse(
    val data: Giphy,
)

data class Giphy(
    var id: String? = null,
    var url: String? = null,
    var images: GiphyImages? = null,
    var analytics: GiphyAnalyticsObject? = null,
    @com.beust.klaxon.Json(ignored = true) var icon: javax.swing.ImageIcon? = null,
    @com.beust.klaxon.Json(ignored = true) var isPreloading: Boolean = false
)

data class GiphyImages(
    var original: GiphyImageDetails? = null,
    var downsized: GiphyImageDetails? = null,
    var downsizedLarge: GiphyImageDetails? = null,
    var downsizedMedium: GiphyImageDetails? = null,
    var downsizedSmall: GiphyImageDetails? = null,
)

data class GiphyImageDetails(
    var url: String? = null,
    var height: String? = null,
    var width: String? = null,
)

data class GiphyAnalyticsObject(
    var onload: GiphyAnalyticsUrlObject? = null,
    var onclick: GiphyAnalyticsUrlObject? = null,
    var onsent: GiphyAnalyticsUrlObject? = null,
)

data class GiphyAnalyticsUrlObject(
    var url: String? = null,
)
