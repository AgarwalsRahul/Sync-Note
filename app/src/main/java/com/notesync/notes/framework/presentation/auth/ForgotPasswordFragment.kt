package com.notesync.notes.framework.presentation.auth

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.transition.TransitionInflater
import com.notesync.notes.R
import com.notesync.notes.business.domain.state.StateMessageCallback
import com.notesync.notes.business.interactors.auth.ForgotPassword.Companion.FORGOT_PASSWORD_SUCCESS
import com.notesync.notes.framework.presentation.UIController
import com.notesync.notes.framework.presentation.auth.state.AuthStateEvent
import com.notesync.notes.framework.presentation.auth.state.ForgotPasswordFields
import com.notesync.notes.framework.presentation.common.hideKeyboard
import com.notesync.notes.framework.presentation.common.invisible
import com.notesync.notes.framework.presentation.common.visible
import kotlinx.android.synthetic.main.fragment_forgot_password.*
import kotlinx.android.synthetic.main.fragment_forgot_password.email_address
import kotlinx.android.synthetic.main.fragment_forgot_password.email_address_layout

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@FlowPreview
@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class ForgotPasswordFragment(private val viewModelProvider: ViewModelProvider.Factory) :
    Fragment(R.layout.fragment_forgot_password) {


    private val viewModel: AuthViewModel by activityViewModels {
        viewModelProvider
    }
    lateinit var uiController: UIController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.setupChannel()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(context)
            .inflateTransition(R.transition.shared_element_transition)
        reset_password_button.setOnClickListener {
            view.hideKeyboard()
            resetPassword()
        }
        initListener()
        subscribeObserver()
    }

    private fun resetPassword() {
        viewModel.setStateEvent(AuthStateEvent.ForgotPasswordEvent(email_address.text.toString()))
    }

    private fun subscribeObserver() {
        viewModel.viewState.observe(viewLifecycleOwner, { viewState ->
            viewState?.let { authViewState ->
                authViewState.forgotPasswordFields?.let {
                    email_address.setText(it.email)
                }
            }
        }

        )
        lifecycleScope.launch {
            viewModel.forgotPasswordEmail.collect { value ->
                reset_password_button.isEnabled = viewModel.validateEmail(value)==null
            }
        }

        viewModel.shouldDisplayProgressBar.observe(viewLifecycleOwner, Observer {
            it?.let {

                if (it) {
                    reset_password_button.invisible()
                } else {
                    reset_password_button.visible()
                }
            }

            uiController.displayProgressBar(it)
        })

        viewModel.stateMessage.observe(viewLifecycleOwner, Observer { stateMessage ->
            stateMessage?.response?.let { response ->
                if (response.message == FORGOT_PASSWORD_SUCCESS) {
                    findNavController(this).popBackStack()
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
        emailTextListener()
    }

    private fun emailTextListener() {
        email_address.apply {
            addTextChangedListener {
                viewModel.setForgotPasswordEmail(it.toString())
                if (email_address_layout.isErrorEnabled
                ) {

                    email_address_layout.error =
                        viewModel.validateEmail(this.text.toString())

                }
            }
            onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    email_address_layout.isErrorEnabled = true
                    email_address_layout.error =
                        viewModel.validateEmail(this.text.toString())
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
        viewModel.setForgotPasswordFields(ForgotPasswordFields(email_address.text.toString()))
    }

}