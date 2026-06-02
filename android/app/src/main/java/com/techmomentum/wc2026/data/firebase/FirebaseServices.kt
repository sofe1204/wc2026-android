package com.techmomentum.wc2026.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage

data class FirebaseServices(
    val auth: FirebaseAuth,
    val firestore: FirebaseFirestore,
    val functions: FirebaseFunctions,
    val storage: FirebaseStorage,
)
