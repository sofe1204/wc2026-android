package com.techmomentum.wc2026.di

import android.app.Application
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage
import com.techmomentum.wc2026.data.firebase.FirebaseBootstrap
import com.techmomentum.wc2026.data.firebase.FirebaseServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideFirebaseServices(application: Application): FirebaseServices =
        FirebaseBootstrap.create(application)

    @Provides
    @Singleton
    fun provideAuth(services: FirebaseServices): FirebaseAuth = services.auth

    @Provides
    @Singleton
    fun provideFirestore(services: FirebaseServices): FirebaseFirestore = services.firestore

    @Provides
    @Singleton
    fun provideFunctions(services: FirebaseServices): FirebaseFunctions = services.functions

    @Provides
    @Singleton
    fun provideStorage(services: FirebaseServices): FirebaseStorage = services.storage
}
