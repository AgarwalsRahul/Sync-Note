package com.notesync.notes.di.main

import com.notesync.notes.framework.presentation.MainActivity
import dagger.Subcomponent

@MainScope
@Subcomponent(
    modules = [
        NoteFragmentFactoryModule::class,
    NoteViewModelModule::class
    ]
)
interface MainComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): MainComponent
    }

    fun inject(mainActivity: MainActivity)

}