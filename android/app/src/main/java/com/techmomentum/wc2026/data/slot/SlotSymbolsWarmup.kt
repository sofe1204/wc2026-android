package com.techmomentum.wc2026.data.slot

import android.content.Context
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.techmomentum.wc2026.data.repository.CatalogRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Loads slot symbol images and keeps decoded bitmaps pinned in memory so reel cells
 * paint instantly. Retries any URL that failed or produced a blank bitmap on each call.
 */
@Singleton
class SlotSymbolsWarmup @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val catalogRepository: CatalogRepository,
) {
    private val mutex = Mutex()
    private val pinnedBitmaps = ConcurrentHashMap<String, ImageBitmap>()

    fun bitmapFor(url: String): ImageBitmap? {
        if (url.isBlank()) return null
        return pinnedBitmaps[url]?.takeIf { isValidBitmap(it) }
    }

    /** Pins every active slot symbol; safe to call repeatedly (only missing/invalid URLs load). */
    suspend fun warmIfNeeded() {
        val symbols = catalogRepository.getSlotSymbols().filter { it.isActive }
        val urls = symbols
            .mapNotNull { symbol -> symbol.imageUrl.takeIf { it.isNotBlank() } }
            .distinct()
        val missing = urls.filter { url -> bitmapFor(url) == null }
        if (missing.isEmpty()) return

        coroutineScope {
            missing.map { url ->
                async { pinUrl(url) }
            }.awaitAll()
        }
    }

    private suspend fun pinUrl(url: String) {
        mutex.withLock {
            if (bitmapFor(url) != null) return
            val loader = appContext.imageLoader
            val result = loader.execute(
                ImageRequest.Builder(appContext)
                    .data(url)
                    .allowHardware(false)
                    .memoryCacheKey(url)
                    .diskCacheKey(url)
                    .build(),
            )
            val drawable = (result as? SuccessResult)?.drawable ?: return
            val bitmap = drawable.toBitmap().asImageBitmap()
            if (isValidBitmap(bitmap)) {
                pinnedBitmaps[url] = bitmap
            } else {
                pinnedBitmaps.remove(url)
            }
        }
    }

    private fun isValidBitmap(bitmap: ImageBitmap): Boolean =
        bitmap.width > 0 && bitmap.height > 0
}
