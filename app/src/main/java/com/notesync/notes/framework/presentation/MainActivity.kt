package com.notesync.notes.framework.presentation

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.callbacks.*
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.notesync.notes.R
import com.notesync.notes.business.domain.state.*
import com.notesync.notes.framework.presentation.common.*
import com.notesync.notes.util.TodoCallback
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_sheet_dialog_layout.*
import kotlinx.android.synthetic.main.fragment_note_list.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi
import javax.inject.Inject
import com.google.android.material.bottomsheet.BottomSheetBehavior


import android.view.LayoutInflater
import com.notesync.notes.business.domain.model.USER_BUNDLE_KEY
import com.notesync.notes.business.domain.model.User


@ExperimentalCoroutinesApi
@FlowPreview
@ObsoleteCoroutinesApi
@DelicateCoroutinesApi
class MainActivity : BaseActivity(),
    UIController {


    private var appBarConfiguration: AppBarConfiguration? = null

    private var dialogInView: MaterialDialog? = null
    private var mBottomSheet: BottomSheetDialog? = null

    @Inject
    lateinit var fragmentFactory: NoteFragmentFactory

    @Inject
    lateinit var providerFactory: NoteViewModelFactory


    val viewModel: MainViewModel by viewModels {
        providerFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        inject()

        setFragmentFactory()
        super.onCreate(savedInstanceState)


        subscribeObservers()
        setContentView(R.layout.activity_main)

    }


    private fun setFragmentFactory() {
        supportFragmentManager.fragmentFactory = fragmentFactory
    }


    @DelicateCoroutinesApi
    private fun subscribeObservers() {
        viewModel.hasSyncBeenExecuted.observe(this, {
            it?.let { hasBeenExecuted ->
                if (!hasBeenExecuted) {
                    viewModel.init()
                }
            }
        })


//        networkStatusHelper.observe(this, {
//            if (it == null || it == NetworkStatus.Unavailable) {
//                printLogD("NetworkStatusHelper", "UNAVAILABLE")
//            }
//        })
    }

    override fun onResume() {
        super.onResume()
        viewModel.init()
    }


    override fun inject() {
        (application as BaseApplication).mainComponent()
            .inject(this)
    }

    override fun displayProgressBar(isDisplayed: Boolean) {
        if (isDisplayed)
            main_progress_bar.visible()
        else
            main_progress_bar.gone()
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_fragment)
            .navigateUp(appBarConfiguration as AppBarConfiguration)
                || super.onSupportNavigateUp()
    }


    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("CheckResult")
    override fun displayInputCaptureDialog(
        title: String,
        callback: DialogInputCaptureCallback
    ) {

        viewModel.setDialogInputCaptureCallback(callback)
//        mBottomSheet = MaterialDialog(this@MainActivity, BottomSheet(LayoutMode.WRAP_CONTENT))
//
//            .cornerRadius(16.0f)
//
//
//        mBottomSheet?.show {
//            title(text = title)
//
//
//            input(
//                waitForPositiveButton = true,
//                inputType = InputType.TYPE_CLASS_TEXT
//            ) { _, text ->
//                callback.onTextCaptured(text.toString())
//            }
//            getInputField().background = null
//            getInputField().setTextColor(getColor(R.color.secondary_text_color))
//
//            positiveButton(R.string.text_Create)
//
//            negativeButton(R.string.text_cancel)
//
////            onDismiss {
////                mBottomSheet=null
////            }
//            cancelable(false)


//        }
        showBottomSheet(null, callback)


    }

    private fun showBottomSheet(
        savedInstanceState: Bundle?,
        callback: DialogInputCaptureCallback?
    ) {
        mBottomSheet = BottomSheetDialog(this)

        val inflater = LayoutInflater.from(this)
        val sheetView = inflater.inflate(
            R.layout.bottom_sheet_dialog_layout,
            this.window.decorView.rootView as ViewGroup,
            false
        )
        mBottomSheet?.setContentView(sheetView)
        savedInstanceState?.let {
            mBottomSheet?.onRestoreInstanceState(it)
        }
        val bottomSheetBehavior: BottomSheetBehavior<*> =
            BottomSheetBehavior.from<View>(sheetView.parent as View)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED;

        mBottomSheet?.show()
        mBottomSheet?.setOnDismissListener {
            mBottomSheet = null
        }
        mBottomSheet?.findViewById<TextView>(R.id.cancel_button)?.setOnClickListener {
            mBottomSheet?.dismiss()
        }
        callback?.let {
            mBottomSheet?.findViewById<TextView>(R.id.create_button)?.setOnClickListener {
                mBottomSheet?.findViewById<EditText>(R.id.editTextTitle)?.let { editText ->
                    if (editText.text.isNotEmpty()) {
                        callback.onTextCaptured(editText.text.toString())
                        mBottomSheet?.dismiss()
                    } else {
                        displayToast("Title is mandatory")
                    }
                }
            }
        }


    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        restoreSession(savedInstanceState)
        savedInstanceState.getBundle("bottomSheet")?.let {
            showBottomSheet(it, viewModel.getDialogInputCaptureCallback())
        }
    }

    private fun restoreSession(savedInstanceState: Bundle?) {
        Log.d("searchNotes","restoreSession")
        savedInstanceState?.let { state ->
            (state[USER_BUNDLE_KEY] as User?)?.let {
                sessionManager.setValue(it)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBundle("bottomSheet", mBottomSheet?.onSaveInstanceState())
        outState.putParcelable(USER_BUNDLE_KEY, sessionManager.cachedUser.value)

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

    private fun displaySnackbar(
        message: String,
        snackbarUndoCallback: SnackbarUndoCallback?,
        onDismissCallback: TodoCallback?,
        stateMessageCallback: StateMessageCallback
    ) {
        val snackbar = Snackbar.make(
            findViewById(R.id.main_container),
            message,
            Snackbar.LENGTH_LONG
        )
        if (snackbarUndoCallback != null)
            snackbar.setAction(
                getString(R.string.text_undo),
                SnackbarUndoListener(snackbarUndoCallback)
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

    override fun hideSoftKeyboard() {
        if (currentFocus != null) {
            val inputMethodManager = getSystemService(
                Context.INPUT_METHOD_SERVICE
            ) as InputMethodManager
            inputMethodManager
                .hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
    }

    override fun onPause() {
        super.onPause()
        if (dialogInView != null) {
            (dialogInView as MaterialDialog).dismiss()
            dialogInView = null
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
                }
                cancelable(false)
            }
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

    override fun onDestroy() {
        super.onDestroy()
        if (mBottomSheet != null && mBottomSheet?.isShowing == true) {
            mBottomSheet?.dismiss()
            mBottomSheet = null
        }
    }


}
