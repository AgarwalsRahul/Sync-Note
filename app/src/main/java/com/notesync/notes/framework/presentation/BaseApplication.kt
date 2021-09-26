package com.notesync.notes.framework.presentation

import android.app.AppComponentFactory
import android.app.Application
import com.notesync.notes.di.AppComponent
import com.notesync.notes.di.DaggerAppComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
open class BaseApplication : Application() {



    lateinit var appComponent: AppComponent
    override fun onCreate() {

        super.onCreate()
        initAppComponent()
    }

    open fun initAppComponent(){
        appComponent = DaggerAppComponent.factory().create(this)
    }
}