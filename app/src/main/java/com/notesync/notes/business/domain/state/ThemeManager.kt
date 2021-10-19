package com.notesync.notes.business.domain.state

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.view.View
import android.view.WindowInsetsController
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.notesync.notes.framework.dataSource.preferences.PreferenceKeys
import com.notesync.notes.framework.presentation.BaseApplication
import com.notesync.notes.util.Constants.DARK_THEME
import com.notesync.notes.util.Constants.LIGHT_THEME
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject
import javax.inject.Singleton


@FlowPreview
@Singleton
@ExperimentalCoroutinesApi
class ThemeManager @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val editor: SharedPreferences.Editor,
    private val application: BaseApplication
) {

    private val _themeMode = MutableLiveData<Int>(LIGHT_THEME)
    val themeMode: LiveData<Int>
        get() = _themeMode

    private fun saveTheme() {
        _themeMode.value?.let {
            editor.putInt(
                PreferenceKeys.THEME_PREFERENCES,
                it
            )
            editor.apply()
        }
    }

    private fun getSavedTheme(): Int {
        return sharedPreferences.getInt(PreferenceKeys.THEME_PREFERENCES, LIGHT_THEME)
    }

    fun setTheme() {
        when (_themeMode.value) {
            LIGHT_THEME -> {
                _themeMode.value = DARK_THEME
            }
            DARK_THEME -> {
                _themeMode.value = LIGHT_THEME
            }
        }
        saveTheme()
    }


    fun initTheme() {
        when (getSavedTheme()) {
            LIGHT_THEME -> {
                _themeMode.value = LIGHT_THEME
            }
            DARK_THEME -> {
                _themeMode.value = DARK_THEME
            }
        }
    }
}