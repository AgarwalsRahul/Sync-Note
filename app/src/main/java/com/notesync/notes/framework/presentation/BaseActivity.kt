package com.notesync.notes.framework.presentation

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.notesync.notes.business.domain.state.SessionManager
import com.notesync.notes.business.domain.state.ThemeManager
import com.notesync.notes.util.Constants.DARK_THEME
import com.notesync.notes.util.Constants.LIGHT_THEME
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi
import javax.inject.Inject


@FlowPreview
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
@DelicateCoroutinesApi
abstract class BaseActivity : AppCompatActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var themeManager: ThemeManager


    @RequiresApi(Build.VERSION_CODES.M)
    @Suppress("Deprecated")
    override fun onCreate(savedInstanceState: Bundle?) {
        (application as BaseApplication).appComponent.inject(this)
        themeManager.initTheme()
        themeManager.themeMode.observe(this, { value ->
            value?.let {
                when (it) {
                    DARK_THEME -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R)
                            this.window.decorView.windowInsetsController
                                ?.setSystemBarsAppearance(0, APPEARANCE_LIGHT_STATUS_BARS);
                        else
                            window.decorView.systemUiVisibility = 0
                    }
                    LIGHT_THEME -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R)
                            this.window.decorView.windowInsetsController
                                ?.setSystemBarsAppearance(
                                    APPEARANCE_LIGHT_STATUS_BARS,
                                    APPEARANCE_LIGHT_STATUS_BARS
                                );
                        else
                            this.window.decorView.systemUiVisibility =
                                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    }
                }
            }
        })


        super.onCreate(savedInstanceState)

    }

    abstract fun inject()


}