package me.ahmadhajjar.giphy.ui

import me.ahmadhajjar.giphy.utils.ClipboardUtil
import me.ahmadhajjar.giphy.utils.GifFileTransferable
import org.junit.Test
import java.awt.datatransfer.DataFlavor
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GiphyTransferableTest {

    private val minimalGifBytes = byteArrayOf(
        0x47, 0x49, 0x46, 0x38, 0x39, 0x61, 0x01, 0x00, 0x01, 0x00, 0x80.toByte(), 0x00, 0x00,
        0xff.toByte(), 0xff.toByte(), 0xff.toByte(),
        0x00, 0x00, 0x00, 0x21, 0xf9.toByte(), 0x04, 0x01, 0x00, 0x00, 0x00, 0x00, 0x2c, 0x00, 0x00,
        0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00, 0x02, 0x02, 0x44, 0x01, 0x00, 0x3b
    )

    @Test
    fun testGifFileTransferableExposesMediaFlavorsWithoutUrlText() {
        val tempFile = File.createTempFile("test", ".gif")
        tempFile.deleteOnExit()
        tempFile.writeBytes(minimalGifBytes)

        val transferable = GifFileTransferable(tempFile, minimalGifBytes)

        // Chat apps paste links when stringFlavor is present — keep it absent.
        assertFalse(transferable.isDataFlavorSupported(DataFlavor.stringFlavor))
        assertTrue(transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor))

        val gifFlavor = DataFlavor("image/gif;class=java.io.InputStream", "Animated GIF")
        assertTrue(transferable.isDataFlavorSupported(gifFlavor))

        val inputStream = transferable.getTransferData(gifFlavor) as java.io.InputStream
        assertTrue(inputStream.readBytes().contentEquals(minimalGifBytes))

        val files = transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<*>
        assertEquals(1, files.size)
        assertEquals(tempFile, files[0])
    }

    @Test
    fun testAppleScriptSetsClipboardToPosixFile() {
        // Build expectation from absolutePath so this passes on Windows CI and macOS/Linux.
        val file = File("example gif\"path.gif")
        val escapedPath = file.absolutePath
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
        val script = ClipboardUtil.appleScriptSetClipboardToFile(file)
        assertEquals("set the clipboard to POSIX file \"$escapedPath\"", script)
        assertTrue(escapedPath.contains("\\\""), "quotes in the path must be AppleScript-escaped")
    }

    @Test
    fun testJxaScriptWritesGifUtisWithoutPlainTextOrFileUrl() {
        val script = ClipboardUtil.jxaSetClipboardToAnimatedGif()
        assertTrue(script.contains("com.compuserve.gif"))
        assertTrue(script.contains("public.gif"))
        assertTrue(script.contains("NSFilenamesPboardType"))
        assertTrue(script.contains("writeObjects"))
        assertFalse(script.contains("public.utf8-plain-text"))
        assertFalse(script.contains("NSPasteboardTypeString"))
        // Catalyst apps (WhatsApp) can crash when public.file-url is on the pasteboard.
        assertFalse(script.contains("public.file-url"))
    }
}
