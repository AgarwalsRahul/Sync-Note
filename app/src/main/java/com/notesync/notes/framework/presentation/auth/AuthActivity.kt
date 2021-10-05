package com.notesync.notes.framework.presentation.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.input.input
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.notesync.notes.R
import com.notesync.notes.business.domain.state.*
import com.notesync.notes.business.interactors.auth.CheckAuthenticatedUser
import com.notesync.notes.framework.presentation.BaseActivity
import com.notesync.notes.framework.presentation.BaseApplication
import com.notesync.notes.framework.presentation.MainActivity
import com.notesync.notes.framework.presentation.UIController
import com.notesync.notes.framework.presentation.auth.state.AuthStateEvent
import com.notesync.notes.framework.presentation.common.*
import com.notesync.notes.util.Constants
import com.notesync.notes.util.Constants.TAG
import com.notesync.notes.util.TodoCallback
import com.notesync.notes.util.printLogD
import kotlinx.android.synthetic.main.activity_auth.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.intellij.lang.annotations.Flow
import javax.inject.Inject
import javax.inject.Named

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
@FlowPreview
class AuthActivity : BaseActivity(), UIController {

    private var dialogInView: MaterialDialog? = null

    @Inject
    lateinit var fragmentFactory: AuthFragmentFactory

    @Inject
    lateinit var providerFactory: AuthViewModelFactory

    val viewModel: AuthViewModel by viewModels {
        providerFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        inject()
        setFragmentFactory()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        viewModel.setupChannel()
        subscribeObserver()
    }

    private fun setFragmentFactory() {
        supportFragmentManager.fragmentFactory = fragmentFactory
    }

    private fun subscribeObserver() {
        viewModel.viewState.observe(this, { authViewState ->
            authViewState?.let {
                Log.d(Constants.TAG, "AuthActivity, subscribeObservers: AuthViewState: $it")
                it.user?.let { user ->
                    printLogD("syncCacheWithNetwork", "executing data syncing")
                    printLogD("AuthActivity","${user.deviceId}")
                    sessionManager.login(user)

                }
            }
        })

        sessionManager.cachedUser.observe(this, { cachedUser ->
            cachedUser?.let {
                if (it.email.isNotEmpty() && it.id.isNotEmpty()) {
                    navMainActivity()
                }
            }

        })
    }

    override fun displayProgressBar(isDisplayed: Boolean) {
        if (isDisplayed)
            progress_bar.visible()
        else
            progress_bar.gone()
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
        dialogInView = MaterialDialog(this).show {
            title(text = title)

            input(
                waitForPositiveButton = true,
                inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
            ) { _, text ->
                callback.onTextCaptured(text.toString())
            }
            positiveButton(R.string.text_ok)
            onDismiss {
                dialogInView = null
            }
            cancelable(true)
        }
    }

    override fun onPause() {
        super.onPause()
        if (dialogInView != null) {
                printLogD("AuthActivity","Dialog is Dismissed")
            (dialogInView as MaterialDialog).cancel()
            (dialogInView as MaterialDialog).dismiss().let {
                printLogD("AuthActivity","Dialog is Dismissed")
            }
            dialogInView = null
        }
    }

    override fun onStop() {
        super.onStop()
        if (dialogInView != null) {
            (dialogInView as MaterialDialog).cancel()
            (dialogInView as MaterialDialog).dismiss()
            dialogInView = null
        }
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
                    dialogInView = areYouSureDialog(
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
                    dialogInView = null
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
                    dialogInView = null
                    stateMessageCallback.removeMessageFromStack()
                }
                cancelable(false)
            }
    }

    private fun navMainActivity() {
        Log.d(TAG, "navMainActivity: called.")
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
        (application as BaseApplication).releaseAuthComponent()
    }

    private fun displaySnackbar(
        message: String,
        snackbarUndoCallback: SnackbarUndoCallback?,
        onDismissCallback: TodoCallback?,
        stateMessageCallback: StateMessageCallback
    ) {
        val snackbar = Snackbar.make(
            findViewById(R.id.auth_container),
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

            dialogInView = when (response.messageType) {

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
                    dialogInView = null
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
                    dialogInView = null
                }
                cancelable(false)
            }
    }

    override fun inject() {

        (application as BaseApplication).authComponent()
            .inject(this)
    }
}