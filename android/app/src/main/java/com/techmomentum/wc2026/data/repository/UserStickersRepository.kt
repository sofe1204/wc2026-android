package com.techmomentum.wc2026.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.techmomentum.wc2026.data.model.UserSticker
import com.techmomentum.wc2026.data.remote.toUserSticker
import com.techmomentum.wc2026.data.session.AppSession
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserStickersRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val appSession: AppSession,
) {
    fun observeUserStickers(): Flow<Map<String, UserSticker>> {
        if (appSession.isActive()) {
            return appSession.guestStickers.map { it }
        }
        return callbackFlow {
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
                        close(error)
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

}
