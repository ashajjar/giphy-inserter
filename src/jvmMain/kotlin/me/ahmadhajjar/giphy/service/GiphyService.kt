package me.ahmadhajjar.giphy.service

import com.beust.klaxon.Klaxon
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.Charset


class GiphyService {
    private var nextPage: Int = 0
    private lateinit var results: List<Giphy>
    private var _searchTerm: String = ""
    private var _currentItemOnPage: Int = 0

    fun nextGiphy(searchTerm: String): Giphy? {
        _currentItemOnPage++
        if (searchTerm != _searchTerm || results.size <= _currentItemOnPage) {
            nextPage++
            fetchPage(searchTerm)
            _currentItemOnPage = 0
        }

        return if (results.size > _currentItemOnPage) {
            results[_currentItemOnPage]
        } else {
            null
        }
    }

    fun previousGiphy(searchTerm: String): Giphy? {
        _currentItemOnPage--
        if (searchTerm != _searchTerm || _currentItemOnPage < 0) {
            if (_currentItemOnPage < 0) {
                _currentItemOnPage = 0
            }

            if (nextPage == 0) {
                return results[_currentItemOnPage]
            }

            nextPage--
            fetchPage(searchTerm)
            _currentItemOnPage = results.size - 1
        }

        return if (results.isNotEmpty() && results.size >= _currentItemOnPage) {
            results[_currentItemOnPage]
        } else {
            null
        }
    }

    private fun fetchPage(searchTerm: String) {
        if (searchTerm != _searchTerm) {
            _searchTerm = searchTerm
            nextPage = 0
        }

        val apiKey = "nng5n18cImwDy26Yb2UW8sypJ9OYH8M8"
        val urlEncodedSearchTerm = URLEncoder.encode(_searchTerm, Charset.defaultCharset())

        val uri = URI.create(
            "https://api.giphy.com/v1/gifs/search?" +
                    "api_key=$apiKey&" +
                    "q=$urlEncodedSearchTerm&" +
                    "limit=$PAGE_SIZE&" +
                    "offset=${PAGE_SIZE * nextPage}&" +
                    "rating=g&" +
                    "lang=en"
        )

        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder()
            .uri(uri)
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        val giphyResponse = Klaxon().parse<GiphyResponse>(response.body())
        results = giphyResponse?.data ?: mutableListOf()
    }

    companion object {
        private const val PAGE_SIZE = 10
    }
}

data class GiphyResponse(
    val data: List<Giphy>,
    // todo later add more meta data
)

data class Giphy(
    var id: String? = null,
    var url: String? = null,
    var images: GiphyImages? = null,
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
