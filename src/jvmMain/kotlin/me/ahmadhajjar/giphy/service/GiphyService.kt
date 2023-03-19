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
    private lateinit var results: Iterator<Giphy>
    private var _searchTerm: String = ""

    fun nextGiphy(searchTerm: String): Giphy? {
        if (searchTerm != _searchTerm || !results.hasNext()) {
            getNextPage(searchTerm)
        }

        return if (results.hasNext()) {
            println("here is a new URL")
            results.next()
        } else {
            println("No more URLs found")
            null
        }
    }

    private fun getNextPage(searchTerm: String) {
        println("Fetching next page ...")
        nextPage++
        if (searchTerm != _searchTerm) {
            println("New search detected ...")
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

        println(uri)

        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder()
            .uri(uri)
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        val giphyResponse = Klaxon().parse<GiphyResponse>(response.body())
        val resultsList = giphyResponse?.data ?: mutableListOf()
        results = resultsList.iterator()
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
