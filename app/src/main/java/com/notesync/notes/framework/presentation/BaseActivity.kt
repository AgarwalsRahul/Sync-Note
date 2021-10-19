package com.notesync.notes.framework.presentation

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.notesync.notes.R
import com.notesync.notes.business.domain.state.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi
import javax.inject.Inject

import android.view.Window
import android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS

import androidx.core.content.ContextCompat

import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import com.notesync.notes.business.domain.state.ThemeManager
import com.notesync.notes.framework.dataSource.preferences.PreferenceKeys
import com.notesync.notes.util.Constants.DARK_THEME
import com.notesync.notes.util.Constants.LIGHT_THEME


@FlowPreview
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
abstract class BaseActivity : AppCompatActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var themeManager: ThemeManager


    override fun onCreate(savedInstanceState: Bundle?) {
        (application as BaseApplication).appComponent.inject(this)
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
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