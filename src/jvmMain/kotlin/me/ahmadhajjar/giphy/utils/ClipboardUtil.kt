package me.ahmadhajjar.giphy.utils

import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.io.ByteArrayInputStream
import java.io.File
import java.util.ArrayList
import java.util.concurrent.TimeUnit

/**
 * Copies an animated GIF as clipboard *media* (file / GIF bytes), not as a URL string.
 *
 * Apps like WhatsApp prefer plain-text flavors when present, which is why including the
 * Giphy link caused paste-as-link instead of a multimedia attachment.
 */
object ClipboardUtil {
    fun copyAnimatedGif(gifFile: File, gifBytes: ByteArray): Boolean {
        if (!gifFile.exists() || gifBytes.isEmpty()) {
            return false
        }

        if (PlatformUtils.isMac && copyGifViaMacPasteboard(gifFile)) {
            return true
        }

        return copyGifViaAwt(gifFile, gifBytes)
    }

    /**
     * On macOS, put both:
     * - raw GIF bytes under native UTIs (`com.compuserve.gif` / `public.gif`)
     * - a POSIX file reference
     *
     * Chat apps then paste a multimedia attachment instead of a URL. Plain-text /
     * HTML link flavors are intentionally omitted.
     */
    internal fun copyGifViaMacPasteboard(gifFile: File): Boolean {
        return try {
            val process = ProcessBuilder(
                "osascript",
                "-l", "JavaScript",
                "-e", jxaSetClipboardToAnimatedGif(),
                gifFile.absolutePath,
            )
                .redirectErrorStream(true)
                .start()
            val finished = process.waitFor(5, TimeUnit.SECONDS)
            if (finished && process.exitValue() == 0) {
                return true
            }
            // Fall back to the simpler AppleScript file-only form.
            copyGifFileViaAppleScript(gifFile)
        } catch (_: Exception) {
            copyGifFileViaAppleScript(gifFile)
        }
    }

    /**
     * macOS apps (WhatsApp, Slack, Messages) reliably paste animated GIFs when the
     * pasteboard holds a POSIX file reference rather than Java image/URL flavors.
     */
    internal fun copyGifFileViaAppleScript(gifFile: File): Boolean {
        return try {
            val script = appleScriptSetClipboardToFile(gifFile)
            val process = ProcessBuilder("osascript", "-e", script)
                .redirectErrorStream(true)
                .start()
            val finished = process.waitFor(5, TimeUnit.SECONDS)
            finished && process.exitValue() == 0
        } catch (_: Exception) {
            false
        }
    }

    internal fun appleScriptSetClipboardToFile(gifFile: File): String {
        val escapedPath = gifFile.absolutePath
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
        return "set the clipboard to POSIX file \"$escapedPath\""
    }

    /**
     * JXA that receives the GIF path as argv[0] and writes native pasteboard types.
     * Using argv avoids path-escaping bugs in the generated script source.
     *
     * Note: we intentionally avoid `public.file-url` here — Mac Catalyst apps
     * (including WhatsApp) have been observed to crash when that type is present.
     * `NSFilenamesPboardType` + GIF UTI bytes is enough for media paste.
     */
    internal fun jxaSetClipboardToAnimatedGif(): String = """
        ObjC.import('AppKit');
        ObjC.import('Foundation');
        function run(argv) {
          var path = argv[0];
          if (!path) { return 1; }
          var data = $.NSData.dataWithContentsOfFile(path);
          if (data.isNil()) { return 2; }

          var item = $.NSPasteboardItem.alloc.init;
          item.setDataForType(data, 'com.compuserve.gif');
          item.setDataForType(data, 'public.gif');

          var pb = $.NSPasteboard.generalPasteboard;
          pb.clearContents;
          pb.writeObjects($.NSArray.arrayWithObject(item));
          // Legacy filename list helps some chat clients attach the file as media.
          pb.setPropertyListForType($.NSArray.arrayWithObject(path), 'NSFilenamesPboardType');
          return 0;
        }
    """.trimIndent()

    private fun copyGifViaAwt(gifFile: File, gifBytes: ByteArray): Boolean {
        return try {
            val clipboard: Clipboard = Toolkit.getDefaultToolkit().systemClipboard
            clipboard.setContents(GifFileTransferable(gifFile, gifBytes), null)
            true
        } catch (_: Exception) {
            false
        }
    }
}

/**
 * AWT transferable used on non-macOS platforms (and as a macOS fallback).
 * Intentionally omits string/HTML URL flavors so chat apps paste the GIF file.
 */
class GifFileTransferable(
    private val gifFile: File,
    private val gifBytes: ByteArray,
) : Transferable {
    private val gifInputStreamFlavor = DataFlavor("image/gif;class=java.io.InputStream", "Animated GIF")
    private val gifByteArrayFlavor = DataFlavor("image/gif;class=\"[B\"", "Animated GIF Bytes")

    override fun getTransferDataFlavors(): Array<DataFlavor> {
        return arrayOf(
            DataFlavor.javaFileListFlavor,
            gifInputStreamFlavor,
            gifByteArrayFlavor,
        )
    }

    override fun isDataFlavorSupported(flavor: DataFlavor): Boolean {
        if (flavor.isMimeTypeEqual("image/gif")) return true
        return getTransferDataFlavors().any { it.equals(flavor) }
    }

    override fun getTransferData(flavor: DataFlavor): Any {
        if (flavor.equals(DataFlavor.javaFileListFlavor)) {
            return ArrayList(listOf(gifFile))
        }

        if (flavor.isMimeTypeEqual("image/gif")) {
            return when (flavor.representationClass) {
                ByteArray::class.java -> gifBytes
                else -> ByteArrayInputStream(gifBytes)
            }
        }

        throw UnsupportedFlavorException(flavor)
    }
}
