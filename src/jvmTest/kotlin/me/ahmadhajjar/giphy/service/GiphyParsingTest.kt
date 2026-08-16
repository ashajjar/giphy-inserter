package me.ahmadhajjar.giphy.service

import com.beust.klaxon.Klaxon
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class GiphyParsingTest {

    @Test
    fun testGiphyImagesParsing() {
        val json = """
        {
            "id": "test_id",
            "images": {
                "original": { "url": "http://original" },
                "downsized_medium": { "url": "http://medium" },
                "downsized_large": { "url": "http://large" }
            }
        }
        """.trimIndent()

        val giphy = Klaxon().parse<Giphy>(json)
        assertNotNull(giphy)
        assertEquals("test_id", giphy.id)
        assertNotNull(giphy.images)
        assertEquals("http://original", giphy.images?.original?.url)
        assertEquals("http://medium", giphy.images?.downsizedMedium?.url)
        assertEquals("http://large", giphy.images?.downsizedLarge?.url)
    }
}
