package com.notesync.notes.di

import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.notesync.notes.business.domain.util.DateUtil
import com.notesync.notes.framework.presentation.common.NoteFragmentFactory
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi
import javax.inject.Singleton

@FlowPreview
@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
@Module
object NoteFragmentFactoryModule {

    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteFragmentFactory(
        viewModelFactory: ViewModelProvider.Factory,
        dateUtil: DateUtil
    ): FragmentFactory {
        return NoteFragmentFactory(
            viewModelFactory,
            dateUtil
        )
    }
}