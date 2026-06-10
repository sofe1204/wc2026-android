package com.techmomentum.wc2026.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileAvatarRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val storage: FirebaseStorage,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) {
    suspend fun uploadAvatar(imageUri: Uri): Result<String> = runCatching {
        val jpegBytes = compressAvatar(imageUri)
        uploadSignedInAvatar(jpegBytes)
    }

    private suspend fun uploadSignedInAvatar(jpegBytes: ByteArray): String {
        val uid = auth.currentUser?.uid
            ?: error("Sign in to upload a profile photo.")
        val ref = storage.reference.child("avatars/$uid.jpg")
        val metadata = StorageMetadata.Builder()
            .setContentType("image/jpeg")
            .build()
        ref.putBytes(jpegBytes, metadata).await()
        val downloadUrl = ref.downloadUrl.await().toString()
        firestore.collection("users").document(uid)
            .update(mapOf("photoUrl" to downloadUrl))
            .await()
        return downloadUrl
    }

    private fun compressAvatar(uri: Uri): ByteArray {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, bounds)
        }
        val sampleSize = calculateSampleSize(bounds.outWidth, bounds.outHeight, MAX_EDGE_PX)
        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val bitmap = context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, decodeOptions)
        } ?: error("Could not read image.")
        val scaled = scaleDown(bitmap, MAX_EDGE_PX)
        if (scaled !== bitmap) bitmap.recycle()
        return encodeJpeg(scaled, JPEG_QUALITY).also { scaled.recycle() }
    }

    private fun scaleDown(source: Bitmap, maxEdge: Int): Bitmap {
        val width = source.width
        val height = source.height
        val largest = maxOf(width, height)
        if (largest <= maxEdge) return source
        val scale = maxEdge.toFloat() / largest
        val targetW = (width * scale).toInt().coerceAtLeast(1)
        val targetH = (height * scale).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(source, targetW, targetH, true)
    }

    private fun encodeJpeg(bitmap: Bitmap, quality: Int): ByteArray {
        val stream = ByteArrayOutputStream()
        var q = quality
        do {
            stream.reset()
            bitmap.compress(Bitmap.CompressFormat.JPEG, q, stream)
            q -= 10
        } while (stream.size() > MAX_BYTES && q >= 50)
        return stream.toByteArray()
    }

    private fun calculateSampleSize(width: Int, height: Int, maxEdge: Int): Int {
        var sample = 1
        var halfW = width / 2
        var halfH = height / 2
        while (halfW / sample >= maxEdge && halfH / sample >= maxEdge) {
            sample *= 2
        }
        return sample.coerceAtLeast(1)
    }

    private companion object {
        const val MAX_EDGE_PX = 512
        const val MAX_BYTES = 900_000
        const val JPEG_QUALITY = 88
    }
}
