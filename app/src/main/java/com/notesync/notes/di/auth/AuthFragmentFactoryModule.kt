package com.notesync.notes.di.auth

import com.notesync.notes.di.auth.AuthScope
import com.notesync.notes.framework.presentation.common.AuthFragmentFactory
import com.notesync.notes.framework.presentation.common.AuthViewModelFactory
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi
import javax.inject.Named

@FlowPreview
@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
@DelicateCoroutinesApi
@Module
object AuthFragmentFactoryModule {

    @JvmStatic
    @AuthScope
    @Provides

    fun provideAuthFragmentFactory(
        viewModelFactory: AuthViewModelFactory,
    ): AuthFragmentFactory {
        return AuthFragmentFactory(
            viewModelFactory,
            )
    }
}