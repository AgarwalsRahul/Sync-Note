package com.notesync.notes.di

import androidx.room.Room
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.notesync.notes.business.domain.model.NoteFactory
import com.notesync.notes.framework.dataSource.cache.database.NoteDatabase
import com.notesync.notes.framework.datasource.data.NoteDataFactory
import com.notesync.notes.framework.presentation.TestBaseApplication
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Singleton


@FlowPreview
@ExperimentalCoroutinesApi
@Module
object TestModule {
    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteDb(app: TestBaseApplication): NoteDatabase {
        return Room
            .inMemoryDatabaseBuilder(app, NoteDatabase::class.java)
            .fallbackToDestructiveMigration()
            .build()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideFirestoreSettings(): FirebaseFirestoreSettings {
        return FirebaseFirestoreSettings.Builder().setHost("10.0.2.2:8080")
            .setSslEnabled(false)
            .setPersistenceEnabled(false).build()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideFirebaseFirestore(settings: FirebaseFirestoreSettings): FirebaseFirestore {
        val firestore = FirebaseFirestore.getInstance()
        firestore.firestoreSettings = settings
        return firestore
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteDataFactory(
        application: TestBaseApplication,
        noteFactory: NoteFactory
    ): NoteDataFactory {
        return NoteDataFactory(application, noteFactory)
    }
}