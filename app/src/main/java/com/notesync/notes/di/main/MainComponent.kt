package com.notesync.notes.di.main

import com.notesync.notes.framework.presentation.MainActivity
import com.notesync.notes.framework.presentation.settings.SettingsActivity
import dagger.Subcomponent
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi

@FlowPreview
@ExperimentalCoroutinesApi
@DelicateCoroutinesApi
@ObsoleteCoroutinesApi
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
    fun inject(settingsActivity: SettingsActivity)

}