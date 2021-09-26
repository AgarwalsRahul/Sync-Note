package com.notesync.notes.framework

import android.content.Context
import androidx.test.core.app.ApplicationProvider

import com.notesync.notes.framework.presentation.TestBaseApplication
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview


@ExperimentalCoroutinesApi
@FlowPreview
abstract class BaseTest {

    val application: TestBaseApplication
            = ApplicationProvider.getApplicationContext<Context>() as TestBaseApplication

    abstract fun injectTest()

}
