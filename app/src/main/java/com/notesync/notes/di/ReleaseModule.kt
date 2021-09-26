package com.notesync.notes.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.google.firebase.firestore.FirebaseFirestore
import com.notesync.notes.framework.dataSource.cache.database.NoteDatabase
import com.notesync.notes.framework.dataSource.preferences.PreferenceKeys
import com.notesync.notes.framework.presentation.BaseApplication
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@FlowPreview
@Module
object ReleaseModule {


    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteDb(app: BaseApplication): NoteDatabase {
        return Room
            .databaseBuilder(app, NoteDatabase::class.java, NoteDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideSharedPreferences(application: BaseApplication): SharedPreferences {
        return application.getSharedPreferences(
            PreferenceKeys.NOTE_PREFERENCES,
            Context.MODE_PRIVATE
        )
    }

}