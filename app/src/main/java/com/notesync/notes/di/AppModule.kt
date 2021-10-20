package com.notesync.notes.di

import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.notesync.notes.business.data.cache.abstraction.AuthCacheDataSource
import com.notesync.notes.business.data.cache.abstraction.NoteCacheDataSource
import com.notesync.notes.business.data.cache.implementation.AuthCacheDataSourceImpl
import com.notesync.notes.business.data.cache.implementation.NoteCacheDataSourceImpl
import com.notesync.notes.business.data.network.abstraction.AuthNetworkDataSource
import com.notesync.notes.business.data.network.abstraction.NoteNetworkDataSource
import com.notesync.notes.business.data.network.implementation.AuthNetworkDataSourceImpl
import com.notesync.notes.business.data.network.implementation.NoteNetworkDataSourceImpl
import com.notesync.notes.business.domain.model.NoteFactory
import com.notesync.notes.business.domain.state.SessionManager
import com.notesync.notes.business.domain.util.DateUtil
import com.notesync.notes.business.interactors.auth.*
import com.notesync.notes.business.interactors.common.DeleteNote
import com.notesync.notes.business.interactors.noteDetail.NoteDetailInteractors
import com.notesync.notes.business.interactors.noteDetail.UpdateNote
import com.notesync.notes.business.interactors.noteList.*
import com.notesync.notes.business.interactors.splash.SyncDeletedNotes
import com.notesync.notes.business.interactors.splash.SyncNotes
import com.notesync.notes.framework.dataSource.cache.abstraction.AuthDaoService
import com.notesync.notes.framework.dataSource.cache.abstraction.NoteDaoService
import com.notesync.notes.framework.dataSource.cache.database.AuthDao
import com.notesync.notes.framework.dataSource.cache.database.NoteDao
import com.notesync.notes.framework.dataSource.cache.database.NoteDatabase
import com.notesync.notes.framework.dataSource.cache.implementation.AuthDaoServiceImpl
import com.notesync.notes.framework.dataSource.cache.implementation.NoteDaoServiceImpl
import com.notesync.notes.framework.dataSource.cache.mappers.CacheMapper
import com.notesync.notes.framework.dataSource.cache.mappers.UserMapper
import com.notesync.notes.framework.dataSource.network.abstraction.AuthFirestoreService
import com.notesync.notes.framework.dataSource.network.abstraction.NoteFirestoreService
import com.notesync.notes.framework.dataSource.network.implementation.AuthFirestoreServiceImpl
import com.notesync.notes.framework.dataSource.network.implementation.NoteFirestoreServiceImpl
import com.notesync.notes.framework.dataSource.network.mappers.NetworkMapper
import com.notesync.notes.framework.presentation.BaseApplication
import com.notesync.notes.framework.presentation.splash.NoteNetworkSyncManager
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Singleton

@Module
@FlowPreview
@ExperimentalCoroutinesApi
@DelicateCoroutinesApi
@ObsoleteCoroutinesApi
object AppModule {
    // https://developer.android.com/reference/java/text/SimpleDateFormat.html?hl=pt-br
    @JvmStatic
    @Singleton
    @Provides
    fun provideDateFormat(): SimpleDateFormat {
        val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.ENGLISH)
        sdf.timeZone = TimeZone.getTimeZone("UTC-7") // match firestore
        return sdf
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideDateUtil(dateFormat: SimpleDateFormat): DateUtil {
        return DateUtil(
            dateFormat
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideSharedPrefsEditor(
        sharedPreferences: SharedPreferences
    ): SharedPreferences.Editor {
        return sharedPreferences.edit()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteFactory(dateUtil: DateUtil, sessionManager: SessionManager): NoteFactory {
        return NoteFactory(
            dateUtil,
            sessionManager
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteDAO(noteDatabase: NoteDatabase): NoteDao {
        return noteDatabase.noteDao()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideAuthDAO(noteDatabase: NoteDatabase): AuthDao {
        return noteDatabase.authDao()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideAuthDaoService(
        authDao: AuthDao,
        userMapper: UserMapper,

        ): AuthDaoService {
        return AuthDaoServiceImpl(authDao, userMapper)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteCacheMapper(dateUtil: DateUtil): CacheMapper {
        return CacheMapper(dateUtil)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideAuthUserMapper(): UserMapper {
        return UserMapper()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteNetworkMapper(dateUtil: DateUtil): NetworkMapper {
        return NetworkMapper(dateUtil)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideAuthCacheDataSource(
        authDaoService: AuthDaoService
    ): AuthCacheDataSource {
        return AuthCacheDataSourceImpl(authDaoService)
    }


    @JvmStatic
    @Singleton
    @Provides
    fun provideAuthNetworkDataSource(
        firestoreService: AuthFirestoreService
    ): AuthNetworkDataSource {
        return AuthNetworkDataSourceImpl(
            firestoreService
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteDaoService(
        noteDao: NoteDao,
        noteEntityMapper: CacheMapper,
        dateUtil: DateUtil
    ): NoteDaoService {
        return NoteDaoServiceImpl(noteDao, noteEntityMapper, dateUtil)
    }


    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteCacheDataSource(
        noteDaoService: NoteDaoService
    ): NoteCacheDataSource {
        return NoteCacheDataSourceImpl(noteDaoService)
    }


    @JvmStatic
    @Singleton
    @Provides
    fun provideFirestoreService(
        firebaseAuth: FirebaseAuth,
        firebaseFirestore: FirebaseFirestore,
        networkMapper: NetworkMapper
    ): NoteFirestoreService {
        return NoteFirestoreServiceImpl(
            firebaseAuth,
            firebaseFirestore,
            networkMapper
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideAuthFirestoreService(
        firebaseAuth: FirebaseAuth,

        ): AuthFirestoreService {
        return AuthFirestoreServiceImpl(
            firebaseAuth,
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteNetworkDataSource(
        firestoreService: NoteFirestoreServiceImpl
    ): NoteNetworkDataSource {
        return NoteNetworkDataSourceImpl(
            firestoreService
        )
    }


    @JvmStatic
    @Singleton
    @Provides
    fun provideSyncNotes(
        noteCacheDataSource: NoteCacheDataSource,
        noteNetworkDataSource: NoteNetworkDataSource,
        dareUtil: DateUtil
    ): SyncNotes {
        return SyncNotes(
            noteCacheDataSource,
            noteNetworkDataSource,
            dareUtil
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideSyncDeletedNotes(
        noteCacheDataSource: NoteCacheDataSource,
        noteNetworkDataSource: NoteNetworkDataSource,
        application: BaseApplication
    ): SyncDeletedNotes {
        return SyncDeletedNotes(
            noteCacheDataSource,
            noteNetworkDataSource,
            application
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteSyncManager(
        syncNotes: SyncNotes,
        syncDeletedNotes: SyncDeletedNotes
    ): NoteNetworkSyncManager {
        return NoteNetworkSyncManager(syncNotes, syncDeletedNotes)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteDetailInteractors(
        noteCacheDataSource: NoteCacheDataSource,
        noteNetworkDataSource: NoteNetworkDataSource,
        application: BaseApplication
    ): NoteDetailInteractors {
        return NoteDetailInteractors(
            DeleteNote(noteCacheDataSource, noteNetworkDataSource, application),
            UpdateNote(noteCacheDataSource, noteNetworkDataSource, application)
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteListInteractors(
        noteCacheDataSource: NoteCacheDataSource,
        noteNetworkDataSource: NoteNetworkDataSource,
        noteFactory: NoteFactory,
        application: BaseApplication
    ): NoteListInteractors {
        return NoteListInteractors(
            InsertNewNote(noteCacheDataSource, noteNetworkDataSource, noteFactory, application),
            DeleteNote(noteCacheDataSource, noteNetworkDataSource, application),
            SearchNotes(noteCacheDataSource),
            GetNumNotes(noteCacheDataSource),
            RestoreDeletedNote(noteCacheDataSource, noteNetworkDataSource, application),
            DeleteMultipleNotes(noteCacheDataSource, noteNetworkDataSource, application),
            GetAllNotesFromNetwork(noteCacheDataSource, noteNetworkDataSource),
            GetUpdatedNotes(noteCacheDataSource, noteNetworkDataSource, application)
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideAuthInteractors(
        authCacheDataSource: AuthCacheDataSource,
        authNetworkDataSource: AuthNetworkDataSource,
        sharedPreferences: SharedPreferences,
        sharedPrefsEditor: SharedPreferences.Editor
    ): AuthInteractors {
        return AuthInteractors(
            Login(authCacheDataSource, authNetworkDataSource, sharedPreferences, sharedPrefsEditor),
            Register(
                authCacheDataSource,
                authNetworkDataSource,
                sharedPreferences,
                sharedPrefsEditor
            ),
            ForgotPassword(authNetworkDataSource),
            CheckAuthenticatedUser(authCacheDataSource, sharedPreferences)
        )
    }



}