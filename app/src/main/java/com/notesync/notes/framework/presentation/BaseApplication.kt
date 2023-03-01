package com.notesync.notes.framework.presentation

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.android.utils.FlipperUtils
import com.facebook.flipper.plugins.inspector.DescriptorMapping
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin
import com.facebook.soloader.SoLoader
import com.notesync.notes.BuildConfig
import com.notesync.notes.di.AppComponent
import com.notesync.notes.di.DaggerAppComponent
import com.notesync.notes.di.auth.AuthComponent
import com.notesync.notes.di.main.MainComponent
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi

@ExperimentalCoroutinesApi
@FlowPreview
@DelicateCoroutinesApi
@ObsoleteCoroutinesApi
open class BaseApplication : Application() {
//
//    @Inject
//    lateinit var workerFactory: WorkerFactory

    lateinit var appComponent: AppComponent
    private var mainComponent: MainComponent? = null

    private var authComponent: AuthComponent? = null
    override fun onCreate() {

        super.onCreate()
        initAppComponent()
        WorkManager.initialize(
            this,
            Configuration.Builder().setWorkerFactory(appComponent.workerFactory).build()
        )
        SoLoader.init(this, false)

        if (BuildConfig.DEBUG && FlipperUtils.shouldEnableFlipper(this)) {
            val client = AndroidFlipperClient.getInstance(this)
            client.addPlugin(InspectorFlipperPlugin(this, DescriptorMapping.withDefaults()))
            client.start()
        }
    }

    open fun initAppComponent() {
        appComponent = DaggerAppComponent.factory().create(this)
    }

    @ObsoleteCoroutinesApi
    fun authComponent(): AuthComponent {
        if (authComponent == null) {
            authComponent = appComponent.authComponent().create()
        }
        return authComponent as AuthComponent
    }

    fun releaseAuthComponent() {
        authComponent = null
    }

    @ObsoleteCoroutinesApi
    fun mainComponent(): MainComponent {
        if (authComponent == null) {
            mainComponent = appComponent.mainComponent().create()
        }
        return mainComponent as MainComponent
    }

    fun releaseMainComponent() {
        mainComponent = null
    }
}