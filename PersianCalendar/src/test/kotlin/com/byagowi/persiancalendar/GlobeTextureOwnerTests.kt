package com.byagowi.persiancalendar

import com.byagowi.persiancalendar.ui.map.GlobeTextureOwner
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class GlobeTextureOwnerTests {
    private class Texture(var recycled: Boolean = false)

    @Test
    fun `replacement and context recreation use latest texture`() {
        val first = Texture()
        val latest = Texture()
        val recycled = mutableListOf<Texture>()
        val owner = GlobeTextureOwner(first) {
            it.recycled = true
            recycled += it
        }
        owner.replace(first)
        assertTrue(recycled.isEmpty())
        owner.replace(latest)
        assertEquals(listOf(first), recycled)
        // Both a delayed queued upload and a recreated surface resolve the latest texture.
        repeat(2) {
            owner.withTexture {
                assertSame(latest, it)
                assertFalse(it.recycled)
            }
        }
        owner.close()
        owner.close()
        assertEquals(listOf(first, latest), recycled)
        var uploadedAfterRelease = false
        owner.withTexture { uploadedAfterRelease = true }
        assertFalse(uploadedAfterRelease)
    }

    @Test
    fun `replacement waits for an in flight upload`() = checkUploadLifetime(close = false)

    @Test
    fun `view release waits for an in flight upload`() = checkUploadLifetime(close = true)

    private fun checkUploadLifetime(close: Boolean) {
        val texture = Texture()
        val owner = GlobeTextureOwner(texture) { it.recycled = true }
        val uploading = CountDownLatch(1)
        val releaseUpload = CountDownLatch(1)
        val replacementStarted = CountDownLatch(1)
        val replacementFinished = CountDownLatch(1)
        var uploadFailure: Throwable? = null
        val upload = thread {
            runCatching {
                owner.withTexture {
                    uploading.countDown()
                    assertTrue(releaseUpload.await(5, TimeUnit.SECONDS))
                    assertFalse(it.recycled)
                }
            }.onFailure { uploadFailure = it }
        }
        assertTrue(uploading.await(5, TimeUnit.SECONDS))
        val replacement = thread {
            replacementStarted.countDown()
            if (close) owner.close() else owner.replace(Texture())
            replacementFinished.countDown()
        }
        val waitResult = runCatching {
            assertTrue(replacementStarted.await(5, TimeUnit.SECONDS))
            assertFalse(replacementFinished.await(100, TimeUnit.MILLISECONDS))
        }
        releaseUpload.countDown()
        upload.join(5000)
        replacement.join(5000)
        waitResult.getOrThrow()
        assertFalse(upload.isAlive)
        assertFalse(replacement.isAlive)
        assertEquals(null, uploadFailure, uploadFailure?.stackTraceToString())
        assertTrue(texture.recycled)
        owner.close()
    }
}
