package com.notesync.notes.framework.presentation.settings

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.RadioGroup
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.notesync.notes.R
import com.notesync.notes.business.domain.state.*
import com.notesync.notes.framework.presentation.BaseActivity
import com.notesync.notes.framework.presentation.BaseApplication
import com.notesync.notes.framework.presentation.UIController
import com.notesync.notes.framework.presentation.common.NoteFragmentFactory
import com.notesync.notes.framework.presentation.common.displayToast
import com.notesync.notes.framework.presentation.common.gone
import com.notesync.notes.framework.presentation.common.visible
import com.notesync.notes.util.Constants.DARK_THEME
import com.notesync.notes.util.Constants.LIGHT_THEME
import com.notesync.notes.util.TodoCallback
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.fragment_setting.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@DelicateCoroutinesApi
@ObsoleteCoroutinesApi
class SettingsActivity : BaseActivity(), UIController {


    private var dialog: MaterialDialog? = null

    @Inject
    lateinit var fragmentFactory: NoteFragmentFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        inject()
        setFragmentFactory()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        subscribeObserver()
    }

    override fun inject() {
        (application as BaseApplication).mainComponent()
            .inject(this)
    }

    private fun setFragmentFactory() {
        supportFragmentManager.fragmentFactory = fragmentFactory
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
            ?.setOnCheckedChangeListener { _, checkedId ->
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

    fun logOut() {
        sessionManager.logout()
    }

    override fun onPause() {
        super.onPause()
        dialog?.dismiss()
        dialog = null
    }

    override fun displayProgressBar(isDisplayed: Boolean) {
        if (isDisplayed)
            progress_bar_setting.visible()
        else
            progress_bar_setting.gone()
    }

    override fun hideSoftKeyboard() {
        if (currentFocus != null) {
            val inputMethodManager = getSystemService(
                Context.INPUT_METHOD_SERVICE
            ) as InputMethodManager
            inputMethodManager
                .hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
    }

    override fun displayInputCaptureDialog(title: String, callback: DialogInputCaptureCallback) {
        //No need
    }

    override fun onResponseReceived(
        response: Response,
        stateMessageCallback: StateMessageCallback
    ) {
        when (response.uiComponentType) {

            is UIComponentType.SnackBar -> {
                val onDismissCallback: TodoCallback? = response.uiComponentType.onDismissCallback
                val undoCallback: SnackbarUndoCallback? = response.uiComponentType.undoCallback
                response.message?.let { msg ->
                    displaySnackbar(
                        message = msg,
                        snackbarUndoCallback = undoCallback,
                        onDismissCallback = onDismissCallback,
                        stateMessageCallback = stateMessageCallback
                    )
                }
            }

            is UIComponentType.AreYouSureDialog -> {

                response.message?.let {
                    dialog = areYouSureDialog(
                        message = it,
                        callback = response.uiComponentType.callback,
                        stateMessageCallback = stateMessageCallback
                    )
                }
            }

            is UIComponentType.Toast -> {
                response.message?.let {
                    displayToast(
                        message = it,
                        stateMessageCallback = stateMessageCallback
                    )
                }
            }

            is UIComponentType.Dialog -> {
                displayDialog(
                    response = response,
                    stateMessageCallback = stateMessageCallback
                )
            }

            is UIComponentType.None -> {
                // This would be a good place to send to your Error Reporting
                // software of choice (ex: Firebase crash reporting)
                stateMessageCallback.removeMessageFromStack()
            }
        }
    }

    private fun displaySuccessDialog(
        message: String?,
        stateMessageCallback: StateMessageCallback
    ): MaterialDialog {
        return MaterialDialog(this)
            .show {
                title(R.string.text_success)
                message(text = message)
                positiveButton(R.string.text_ok) {
                    stateMessageCallback.removeMessageFromStack()
                    dismiss()
                }
                onDismiss {
                    dialog = null
                    stateMessageCallback.removeMessageFromStack()
                }
                cancelable(false)
            }
    }

    private fun displayErrorDialog(
        message: String?,
        stateMessageCallback: StateMessageCallback
    ): MaterialDialog {
        return MaterialDialog(this)
            .show {
                title(R.string.text_error)
                message(text = message)
                positiveButton(R.string.text_ok) {
                    stateMessageCallback.removeMessageFromStack()
                    dismiss()
                }
                onDismiss {
                    dialog = null
                    stateMessageCallback.removeMessageFromStack()
                }
                cancelable(false)
            }
    }

    private fun displaySnackbar(
        message: String,
        snackbarUndoCallback: SnackbarUndoCallback?,
        onDismissCallback: TodoCallback?,
        stateMessageCallback: StateMessageCallback
    ) {
        val snackbar = Snackbar.make(
            findViewById(R.id.setting_container),
            message,
            Snackbar.LENGTH_LONG
        )
        snackbar.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                onDismissCallback?.execute()
            }
        })
        snackbar.show()
        stateMessageCallback.removeMessageFromStack()
    }

    private fun displayDialog(
        response: Response,
        stateMessageCallback: StateMessageCallback
    ) {
        response.message?.let { message ->

            dialog = when (response.messageType) {

                is MessageType.Error -> {
                    displayErrorDialog(
                        message = message,
                        stateMessageCallback = stateMessageCallback
                    )
                }

                is MessageType.Success -> {
                    displaySuccessDialog(
                        message = message,
                        stateMessageCallback = stateMessageCallback
                    )
                }

                is MessageType.Info -> {
                    displayInfoDialog(
                        message = message,
                        stateMessageCallback = stateMessageCallback
                    )
                }

                else -> {
                    // do nothing
                    stateMessageCallback.removeMessageFromStack()
                    null
                }
            }
        } ?: stateMessageCallback.removeMessageFromStack()
    }


    private fun displayInfoDialog(
        message: String?,
        stateMessageCallback: StateMessageCallback
    ): MaterialDialog {
        return MaterialDialog(this)
            .show {
                title(R.string.text_info)
                message(text = message)
                positiveButton(R.string.text_ok) {
                    stateMessageCallback.removeMessageFromStack()
                    dismiss()
                }
                onDismiss {
                    dialog = null
                    stateMessageCallback.removeMessageFromStack()
                }
                cancelable(false)
            }
    }

    private fun areYouSureDialog(
        message: String,
        callback: AreYouSureCallback,
        stateMessageCallback: StateMessageCallback
    ): MaterialDialog {
        return MaterialDialog(this)
            .show {
                title(R.string.are_you_sure)
                message(text = message)
                negativeButton(R.string.text_cancel) {
                    stateMessageCallback.removeMessageFromStack()
                    callback.cancel()
                    dismiss()
                }
                positiveButton(R.string.text_yes) {
                    stateMessageCallback.removeMessageFromStack()
                    callback.proceed()
                    dismiss()
                }
                onDismiss {
                    dialog = null
                }
                cancelable(false)
            }
    }

}