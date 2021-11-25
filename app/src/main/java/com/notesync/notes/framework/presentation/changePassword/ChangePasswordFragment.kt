package com.notesync.notes.framework.presentation.changePassword

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.notesync.notes.R
import com.notesync.notes.business.domain.state.StateMessageCallback
import com.notesync.notes.business.interactors.auth.ChangePassword.Companion.CHANGE_PASSWORD_SUCCESS
import com.notesync.notes.framework.presentation.UIController
import com.notesync.notes.framework.presentation.changePassword.state.ChangePasswordEvent
import com.notesync.notes.framework.presentation.common.displayToast
import com.notesync.notes.framework.presentation.common.hideKeyboard
import com.notesync.notes.framework.presentation.common.invisible
import com.notesync.notes.framework.presentation.common.visible
import kotlinx.android.synthetic.main.fragment_change_password.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect

@FlowPreview
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
@DelicateCoroutinesApi
class ChangePasswordFragment(private val viewModelFactory: ViewModelProvider.Factory) : Fragment() {

    val viewModel: ChangePasswordViewModel by viewModels {
        viewModelFactory
    }

    lateinit var uiController: UIController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view?.hideKeyboard()
        viewModel.setupChannel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_change_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        submit_button.setOnClickListener {
            view.hideKeyboard()
            viewModel.setStateEvent(
                ChangePasswordEvent(
                    oldPassword.text.toString(),
                    new_password.text.toString()
                )
            )
        }
        imageView.setOnClickListener {
            findNavController().popBackStack()
        }
        initListener()
        subscribeObservers()
    }

    private fun subscribeObservers() {
        viewModel.viewState.observe(viewLifecycleOwner, { viewState ->
            viewState?.let {
                it.oldPassword?.let { p ->
                    oldPassword.setText(p)
                }
                it.newPassword?.let { p ->
                    new_password.setText(p)
                }
            }
        })

        lifecycleScope.launch {
            viewModel.isSubmitButtonEnabled.collect { value ->
//                if (value != null)
                if (oldPassword.text.toString() != new_password.text.toString()) {
                    submit_button.isEnabled = value
                } else {
                    if (value) {
                        requireActivity().displayToast("Both old and new password must be different")
                    }else{
                        submit_button.isEnabled = value
                    }
                }

            }
        }
        viewModel.shouldDisplayProgressBar.observe(viewLifecycleOwner, {
            it?.let {
                old_password_layout.isEnabled = !it
                new_password_layout.isEnabled = !it

                if (it) {
                    submit_button.invisible()
                } else {
                    submit_button.visible()
                }
            }

            uiController.displayProgressBar(it)
        })

        viewModel.stateMessage.observe(viewLifecycleOwner, { stateMessage ->
            stateMessage?.response?.let { response ->
                if(response.message?.equals(CHANGE_PASSWORD_SUCCESS)==true){
                    findNavController().popBackStack()
                }
                uiController.onResponseReceived(
                    response = response,
                    stateMessageCallback = object : StateMessageCallback {
                        override fun removeMessageFromStack() {
                            viewModel.clearStateMessage()
                        }
                    }
                )

            }
        })
    }

    private fun initListener() {
        oldPasswordTextListener()
        newPasswordTextListener()
    }

    private fun newPasswordTextListener() {

        new_password.apply {
            addTextChangedListener {
                viewModel.setNewPassword(it.toString())
                if (new_password_layout.isErrorEnabled
                ) {
                    new_password_layout.error =
                        viewModel.validatePasswords(this.text.toString())

                }
            }
            onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    viewModel.newPasswordFocusInitial = false
                    new_password_layout.isErrorEnabled =
                        !viewModel.validatePasswords(this.text.toString()).isNullOrEmpty()
                    new_password_layout.error =
                        viewModel.validatePasswords(this.text.toString())
                } else {
                    new_password_layout.isErrorEnabled = !viewModel.newPasswordFocusInitial
                }

            }
        }
    }

    private fun oldPasswordTextListener() {
        oldPassword.apply {

            addTextChangedListener {
                viewModel.setOldPassword(it.toString())
                if (old_password_layout.isErrorEnabled
                ) {
                    old_password_layout.error =
                        viewModel.validatePasswords(this.text.toString())
                }
            }
            onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    viewModel.oldPasswordFocusInitial = false
                    old_password_layout.isErrorEnabled =
                        !viewModel.validatePasswords(this.text.toString()).isNullOrEmpty()
                    old_password_layout.error =
                        viewModel.validatePasswords(this.text.toString())


                } else {
                    old_password_layout.isErrorEnabled = !viewModel.oldPasswordFocusInitial
                }

            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.let {
            uiController = context as UIController
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.setChangePasswordFields(oldPassword.text.toString(), new_password.text.toString())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.setChangePasswordFields(oldPassword.text.toString(), new_password.text.toString())
    }

}