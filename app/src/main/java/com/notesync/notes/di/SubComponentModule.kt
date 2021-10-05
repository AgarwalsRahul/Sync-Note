package com.notesync.notes.di

import com.notesync.notes.di.auth.AuthComponent
import com.notesync.notes.di.main.MainComponent
import dagger.Module

@Module(
    subcomponents = [
        AuthComponent::class,
        MainComponent::class
    ]
)
class SubComponentsModule {
}