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
 * Loads every slot symbol image once and keeps the decoded bitmaps pinned in memory
 * so the slot grid can paint instantly (no Coil cache miss / flash) when the user
 * opens the Slots screen. There are only a handful of symbols, so the memory cost is tiny.
 */
@Singleton
class SlotSymbolsWarmup @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val catalogRepository: CatalogRepository,
) {
    private val mutex = Mutex()
    private var completed = false
    private val pinnedBitmaps = ConcurrentHashMap<String, ImageBitmap>()

    fun bitmapFor(url: String): ImageBitmap? = pinnedBitmaps[url]

    suspend fun warmIfNeeded() {
        mutex.withLock {
            if (completed) return
        }
        val symbols = catalogRepository.getSlotSymbols().filter { it.isActive }
        val loader = appContext.imageLoader
        coroutineScope {
            symbols
                .mapNotNull { symbol -> symbol.imageUrl.takeIf { it.isNotBlank() } }
                .distinct()
                .map { url ->
                    async {
                        val result = loader.execute(
                            ImageRequest.Builder(appContext)
                                .data(url)
                                // Software bitmap so it can be drawn directly via Compose Image.
                                .allowHardware(false)
                                .memoryCacheKey(url)
                                .diskCacheKey(url)
                                .build(),
                        )
                        val drawable = (result as? SuccessResult)?.drawable
                        if (drawable != null) {
                            pinnedBitmaps[url] = drawable.toBitmap().asImageBitmap()
                        }
                    }
                }
                .awaitAll()
        }
        mutex.withLock { completed = true }
    }
}
