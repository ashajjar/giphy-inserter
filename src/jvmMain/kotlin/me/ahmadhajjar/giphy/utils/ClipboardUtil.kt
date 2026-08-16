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
 * Copies an animated GIF as clipboard *media* (file / GIF bytes), with a text URL
 * as a lower-priority fallback so plain-text targets (e.g. code editors) can still
 * paste the Giphy link.
 *
 * The URL is placed on a *separate* NSPasteboardItem after the media item, so
 * image-capable apps (WhatsApp, Slack, Messages) still pick the GIF, while
 * text-only targets fall through to the string flavor.
 */
object ClipboardUtil {
    fun copyAnimatedGif(gifFile: File, gifBytes: ByteArray, url: String? = null): Boolean {
        if (!gifFile.exists() || gifBytes.isEmpty()) {
            return false
        }

        if (PlatformUtils.isMac && copyGifViaMacPasteboard(gifFile, url)) {
            return true
        }

        return copyGifViaAwt(gifFile, gifBytes, url)
    }

    /**
     * On macOS, put:
     * - raw GIF bytes under native UTIs (`com.compuserve.gif` / `public.gif`) on item 1
     * - a POSIX filename list for legacy chat clients
     * - the Giphy URL as `public.utf8-plain-text` on a *second* pasteboard item so it
     *   only surfaces in text-only paste targets.
     */
    internal fun copyGifViaMacPasteboard(gifFile: File, url: String?): Boolean {
        return try {
            val args = mutableListOf(
                "osascript",
                "-l", "JavaScript",
                "-e", jxaSetClipboardToAnimatedGif(),
                gifFile.absolutePath,
            )
            if (!url.isNullOrEmpty()) args += url
            val process = ProcessBuilder(args)
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
     * JXA that receives the GIF path as argv[0] and (optionally) a URL as argv[1].
     * Using argv avoids escaping bugs in the generated script source.
     *
     * Writes two pasteboard items:
     *   1. GIF bytes under `com.compuserve.gif` / `public.gif`
     *   2. URL string under `public.utf8-plain-text` (fallback for text-only targets)
     *
     * Note: we intentionally avoid `public.file-url` here — Mac Catalyst apps
     * (including WhatsApp) have been observed to crash when that type is present.
     */
    internal fun jxaSetClipboardToAnimatedGif(): String = """
        ObjC.import('AppKit');
        ObjC.import('Foundation');
        function run(argv) {
          var path = argv[0];
          if (!path) { return 1; }
          var url = argv[1];
          var data = $.NSData.dataWithContentsOfFile(path);
          if (data.isNil()) { return 2; }

          var gifItem = $.NSPasteboardItem.alloc.init;
          gifItem.setDataForType(data, 'com.compuserve.gif');
          gifItem.setDataForType(data, 'public.gif');

          var items = $.NSMutableArray.alloc.init;
          items.addObject(gifItem);
          if (url) {
            var urlItem = $.NSPasteboardItem.alloc.init;
            urlItem.setStringForType(url, 'public.utf8-plain-text');
            items.addObject(urlItem);
          }

          var pb = $.NSPasteboard.generalPasteboard;
          pb.clearContents;
          pb.writeObjects(items);
          // Legacy filename list helps some chat clients attach the file as media.
          pb.setPropertyListForType($.NSArray.arrayWithObject(path), 'NSFilenamesPboardType');
          return 0;
        }
    """.trimIndent()

    private fun copyGifViaAwt(gifFile: File, gifBytes: ByteArray, url: String?): Boolean {
        return try {
            val clipboard: Clipboard = Toolkit.getDefaultToolkit().systemClipboard
            clipboard.setContents(GifFileTransferable(gifFile, gifBytes, url), null)
            true
        } catch (_: Exception) {
            false
        }
    }
}

/**
 * AWT transferable used on non-macOS platforms (and as a macOS fallback).
 *
 * Media flavors are advertised *first* so image-capable paste targets pick the GIF.
 * A plain-text URL flavor is advertised last as a fallback so text-only targets
 * (e.g. code editors) can still paste the Giphy link instead of getting nothing.
 */
class GifFileTransferable(
    private val gifFile: File,
    private val gifBytes: ByteArray,
    private val url: String? = null,
) : Transferable {
    private val gifInputStreamFlavor = DataFlavor("image/gif;class=java.io.InputStream", "Animated GIF")
    private val gifByteArrayFlavor = DataFlavor("image/gif;class=\"[B\"", "Animated GIF Bytes")

    override fun getTransferDataFlavors(): Array<DataFlavor> {
        val flavors = mutableListOf(
            DataFlavor.javaFileListFlavor,
            gifInputStreamFlavor,
            gifByteArrayFlavor,
        )
        if (!url.isNullOrEmpty()) {
            flavors += DataFlavor.stringFlavor
        }
        return flavors.toTypedArray()
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

        if (flavor.equals(DataFlavor.stringFlavor) && !url.isNullOrEmpty()) {
            return url
        }

        throw UnsupportedFlavorException(flavor)
    }
}
