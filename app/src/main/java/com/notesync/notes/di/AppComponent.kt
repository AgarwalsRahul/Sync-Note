package com.notesync.notes.di

import com.notesync.notes.business.domain.state.SessionManager
import com.notesync.notes.business.domain.state.ThemeManager
import com.notesync.notes.di.auth.AuthComponent
import com.notesync.notes.di.main.MainComponent
import com.notesync.notes.di.worker.WorkerBindingModule
import com.notesync.notes.framework.presentation.BaseActivity
import com.notesync.notes.framework.presentation.BaseApplication
import com.notesync.notes.framework.presentation.notelist.NoteListFragment
import com.notesync.notes.framework.presentation.settings.SettingsActivity
import com.notesync.notes.framework.workers.CustomWorkerFactory
import dagger.BindsInstance
import dagger.Component
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi
import javax.inject.Singleton

@DelicateCoroutinesApi
@FlowPreview
@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
@Singleton
@Component(
    modules = [AppModule::class, ReleaseModule::class, SubComponentsModule::class, WorkerBindingModule::class]
)
interface AppComponent {

    @DelicateCoroutinesApi
    val sessionManager: SessionManager

    val workerFactory:CustomWorkerFactory

    val themeManager:ThemeManager

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance app: BaseApplication): AppComponent
    }

    fun inject(baseActivity: BaseActivity)

    @DelicateCoroutinesApi
    fun inject(noteListFragment: NoteListFragment)

    fun inject(settingsActivity: SettingsActivity)

    fun authComponent(): AuthComponent.Factory

    fun mainComponent(): MainComponent.Factory


}