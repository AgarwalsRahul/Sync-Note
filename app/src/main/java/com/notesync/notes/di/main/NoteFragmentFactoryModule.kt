package com.notesync.notes.di.main

import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.notesync.notes.business.domain.util.DateUtil
import com.notesync.notes.framework.presentation.common.NoteFragmentFactory
import com.notesync.notes.framework.presentation.common.NoteViewModelFactory
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi
import javax.inject.Named
import javax.inject.Singleton

@FlowPreview
@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
@Module
object NoteFragmentFactoryModule {

    @JvmStatic
    @MainScope
    @Provides

    fun provideNoteFragmentFactory(
        viewModelFactory:NoteViewModelFactory,
        dateUtil: DateUtil
    ): NoteFragmentFactory {
        return NoteFragmentFactory(
            viewModelFactory,
            dateUtil
        )
    }
}