package com.techmomentum.wc2026.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.techmomentum.wc2026.data.model.UserProfile
import com.techmomentum.wc2026.data.remote.toUserProfile
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) {
    fun observeUserProfile(): Flow<UserProfile?> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val reg = firestore.collection("users").document(uid)
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    // Sign-out revokes access; don't close the flow with an exception.
                    trySend(null)
                    return@addSnapshotListener
                }
                trySend(snap?.toUserProfile())
            }
        awaitClose { reg.remove() }
    }
}
