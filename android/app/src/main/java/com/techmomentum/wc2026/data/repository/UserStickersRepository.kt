package com.techmomentum.wc2026.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.techmomentum.wc2026.data.model.UserSticker
import com.techmomentum.wc2026.data.remote.toUserSticker
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserStickersRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) {
    fun observeUserStickers(): Flow<Map<String, UserSticker>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyMap())
            close()
            return@callbackFlow
        }
        val reg = firestore.collection("user_stickers")
            .document(uid)
            .collection("items")
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    // Sign-out revokes access; don't close the flow with an exception.
                    trySend(emptyMap())
                    return@addSnapshotListener
                }
                val map = snap?.documents?.associate { doc ->
                    val sticker = doc.toUserSticker()
                    sticker.stickerId to sticker
                } ?: emptyMap()
                trySend(map)
            }
        awaitClose { reg.remove() }
    }
}
