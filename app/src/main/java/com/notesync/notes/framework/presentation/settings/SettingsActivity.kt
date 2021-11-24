package com.notesync.notes.framework.presentation.settings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.RadioGroup
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.notesync.notes.R
import com.notesync.notes.framework.presentation.BaseActivity
import com.notesync.notes.framework.presentation.BaseApplication
import com.notesync.notes.framework.presentation.auth.AuthActivity
import com.notesync.notes.util.Constants.DARK_THEME
import com.notesync.notes.util.Constants.LIGHT_THEME
import kotlinx.android.synthetic.main.fragment_setting.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi

@FlowPreview
@ExperimentalCoroutinesApi
@DelicateCoroutinesApi
@ObsoleteCoroutinesApi
class SettingsActivity : BaseActivity() {


    private var dialog: MaterialDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        inject()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        subscribeObserver()
    }

    override fun inject() {
    }


    private fun subscribeObserver() {
        themeManager.themeMode.observe(this, {
            it?.let { value ->
                when (value) {
                    DARK_THEME -> {
                        theme_text.text = getString(R.string.dark)

                    }
                    LIGHT_THEME -> {
                        theme_text.text = getString(R.string.light)

                    }
                }
            }
        })
        sessionManager.cachedUser.observe(this, {
            if (it == null) {
                Log.d("SessionManager","LOGGING OUT")
                finish()

            }
        })
    }

    fun showThemeDialog() {

        dialog = MaterialDialog(this).cornerRadius(20.0f)
            .noAutoDismiss()
            .customView(R.layout.layout_theme)

        val view = dialog?.getCustomView()
        val theme = themeManager.themeMode.value
        view?.findViewById<RadioGroup>(R.id.theme_group)?.apply {
            when (theme) {
                DARK_THEME -> {
                    check(R.id.theme_dark)
                }
                LIGHT_THEME -> {
                    check(R.id.theme_light)
                }
            }

        }
        view?.findViewById<RadioGroup>(R.id.theme_group)
            ?.setOnCheckedChangeListener { group, checkedId ->
                dialog?.dismiss()
                when (checkedId) {
                    R.id.theme_dark -> {
                        themeManager.setTheme(DARK_THEME)
                    }
                    R.id.theme_light -> {
                        themeManager.setTheme(LIGHT_THEME)
                    }
                }

            }
        view?.findViewById<TextView>(R.id.negative_button)?.setOnClickListener {
            dialog?.dismiss()
        }
        dialog?.show()
    }

    fun logOut(){
        sessionManager.logout()
    }

    override fun onPause() {
        super.onPause()
        dialog?.dismiss()
    }
}