package me.ahmadhajjar.giphy.ui

import java.awt.datatransfer.DataFlavor
import java.io.File
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GiphyTransferableTest {

    @Test
    fun testGiphyTransferableFlavors() {
        val url = "https://example.com/giphy.gif"
        val bytes = byteArrayOf(1, 2, 3)
        val tempFile = File.createTempFile("test", ".gif")
        tempFile.deleteOnExit()

        val transferable = GiphyTransferable(url, bytes, tempFile)
        val flavors = transferable.transferDataFlavors

        assertTrue(transferable.isDataFlavorSupported(DataFlavor.stringFlavor))
        assertTrue(transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
        assertTrue(transferable.isDataFlavorSupported(DataFlavor.imageFlavor))
        
        val gifFlavor = DataFlavor("image/gif;class=java.io.InputStream", "Animated GIF")
        assertTrue(transferable.isDataFlavorSupported(gifFlavor))

        assertEquals(url, transferable.getTransferData(DataFlavor.stringFlavor))
        
        val files = transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<File>
        assertEquals(1, files.size)
        assertEquals(tempFile, files[0])
    }
}
