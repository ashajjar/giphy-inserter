package me.ahmadhajjar.giphy.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.graphics.drawscope.scale
import org.jetbrains.skia.*
import java.net.URL

@Composable
fun AnimatedGif(gif: AnimatedGif, modifier: Modifier) {
    if (gif.frames.isEmpty()) return

    val transition = rememberInfiniteTransition()
    val frameIndex by transition.animateValue(
        initialValue = 0,
        targetValue = gif.frames.lastIndex,
        Int.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 0
                for ((index, frame) in gif.frames.withIndex()) {
                    index at durationMillis
                    durationMillis += frame.duration
                }
            }
        )
    )

    val gifSize = Size(gif.width.toFloat(), gif.height.toFloat())

    Canvas(modifier.aspectRatio(gifSize.width / gifSize.height)) {
        scale(size.width / gifSize.width, size.height / gifSize.height, Offset.Zero) {
            drawImage(gif.frames[frameIndex].image)
        }
    }
}

class AnimatedGif(
    val width: Int,
    val height: Int,
    val frames: List<ImageFrame>
) {
    class ImageFrame(
        val image: ImageBitmap,
        val duration: Int
    )

    companion object {
        fun fromURL(url: URL): AnimatedGif {
            val bytes = url.readBytes()
            val data = Data.makeFromBytes(bytes)
            val codec = Codec.makeFromData(data)

            val frames = mutableListOf<ImageFrame>()
            val imageInfo = ImageInfo.makeN32Premul(codec.width, codec.height)

            val masterBitmap = Bitmap()
            masterBitmap.allocPixels(imageInfo)

            val cachedBitmaps = mutableMapOf<Int, Bitmap>()

            val frameRectField = try {
                AnimationFrameInfo::class.java.getDeclaredField("frameRect").apply { isAccessible = true }
            } catch (e: Exception) {
                null
            }

            for (i in 0 until codec.frameCount) {
                val frameInfo = codec.getFrameInfo(i)

                // 1. Prepare base bitmap
                val requiredFrame = frameInfo.requiredFrame
                if (requiredFrame != -1 && cachedBitmaps.containsKey(requiredFrame)) {
                    val prev = cachedBitmaps[requiredFrame]!!
                    Canvas(masterBitmap).drawImage(Image.makeFromBitmap(prev), 0f, 0f)
                } else if (requiredFrame == -1) {
                    masterBitmap.erase(0)
                }

                // 2. Decode current frame onto master
                codec.readPixels(masterBitmap, i)

                // 3. Save frame
                val frameImage = Image.makeFromBitmap(masterBitmap).toComposeImageBitmap()

                // Ensure duration is reasonable
                val duration = if (frameInfo.duration <= 0) 100 else frameInfo.duration
                frames.add(ImageFrame(frameImage, duration))

                // 4. Handle disposal for future dependencies
                val nextBase = Bitmap()
                nextBase.allocPixels(imageInfo)
                Canvas(nextBase).drawImage(Image.makeFromBitmap(masterBitmap), 0f, 0f)

                // Using ordinals because Skiko enum names can vary across versions
                // Typical Skia/Skiko ordinals: 0: UNSPECIFIED, 1: KEEP, 2: RESTORE_BACKGROUND, 3: RESTORE_PREVIOUS
                when (frameInfo.disposalMethod.ordinal) {
                    2 -> {
                        val rect = frameRectField?.get(frameInfo) as? IRect
                        if (rect != null) {
                            nextBase.erase(0, rect)
                        } else {
                            nextBase.erase(0)
                        }
                    }
                    3 -> {
                        if (requiredFrame != -1 && cachedBitmaps.containsKey(requiredFrame)) {
                            val prev = cachedBitmaps[requiredFrame]!!
                            Canvas(nextBase).drawImage(Image.makeFromBitmap(prev), 0f, 0f)
                        } else {
                            nextBase.erase(0)
                        }
                    }
                }
                cachedBitmaps[i] = nextBase
            }

            // Cleanup native resources
            cachedBitmaps.values.forEach { it.close() }
            masterBitmap.close()
            data.close()

            return AnimatedGif(
                width = codec.width,
                height = codec.height,
                frames = frames
            )
        }
    }
}
