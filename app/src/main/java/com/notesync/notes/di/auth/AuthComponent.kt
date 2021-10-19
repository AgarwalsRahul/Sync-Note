package com.notesync.notes.di.auth

import com.notesync.notes.framework.presentation.auth.AuthActivity
import dagger.Subcomponent
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi

@FlowPreview
@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
@DelicateCoroutinesApi
@AuthScope
@Subcomponent(
    modules = [

        AuthViewModelModule::class,
        AuthFragmentFactoryModule::class,

    ]
)
interface AuthComponent {
    @Subcomponent.Factory
    interface Factory {
        fun create(): AuthComponent
    }

    fun inject(authActivity: AuthActivity)
}