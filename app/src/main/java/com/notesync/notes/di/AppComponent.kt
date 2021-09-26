package com.notesync.notes.di

import com.notesync.notes.framework.presentation.BaseApplication
import com.notesync.notes.framework.presentation.MainActivity
import com.notesync.notes.framework.presentation.notedetail.NoteDetailFragment
import com.notesync.notes.framework.presentation.notelist.NoteListFragment
import dagger.BindsInstance
import dagger.Component
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi
import javax.inject.Singleton

@FlowPreview
@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
@Singleton
@Component(
    modules = [AppModule::class, ReleaseModule::class,
        NoteViewModelModule::class, NoteFragmentFactoryModule::class]
)
interface AppComponent {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance app: BaseApplication): AppComponent
    }

    fun inject(mainActivity: MainActivity)


}