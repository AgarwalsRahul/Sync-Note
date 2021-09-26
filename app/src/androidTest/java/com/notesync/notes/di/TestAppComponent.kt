package com.notesync.notes.di

import com.notesync.notes.framework.datasource.cache.NoteDaoServiceTest
import com.notesync.notes.framework.datasource.network.NoteNetworkServiceTest
import com.notesync.notes.framework.presentation.TestBaseApplication
import dagger.BindsInstance
import dagger.Component
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Singleton

@FlowPreview
@Singleton
@ExperimentalCoroutinesApi
@Component(
    modules = [
        AppModule::class,
        TestModule::class
    ]
)
interface TestAppComponent : AppComponent {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance app: TestBaseApplication): TestAppComponent
    }

    fun inject(noteNetworkServiceTest: NoteNetworkServiceTest)
    fun inject(noteDaoServiceTest: NoteDaoServiceTest)
}