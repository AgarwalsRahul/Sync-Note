package com.notesync.notes.framework.presentation.auth

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment.findNavController
import com.notesync.notes.R
import com.notesync.notes.business.domain.state.StateMessageCallback
import com.notesync.notes.framework.presentation.UIController
import com.notesync.notes.framework.presentation.auth.state.AuthStateEvent
import com.notesync.notes.framework.presentation.auth.state.LoginFields
import com.notesync.notes.framework.presentation.auth.state.RegistrationFields
import com.notesync.notes.framework.presentation.common.AuthViewModelFactory
import com.notesync.notes.framework.presentation.common.hideKeyboard
import com.notesync.notes.framework.presentation.common.invisible
import com.notesync.notes.framework.presentation.common.visible
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_register.*
import kotlinx.android.synthetic.main.fragment_register.register_button
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@FlowPreview
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class RegisterFragment(private val viewModelFactory: AuthViewModelFactory) :
    Fragment(R.layout.fragment_register) {

    private val viewModel: AuthViewModel by activityViewModels {
        viewModelFactory
    }
    lateinit var uiController: UIController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.setupChannel()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        register_button.setOnClickListener {
            view.hideKeyboard()
            register()
        }
        login_button_register.setOnClickListener{
            findNavController(this).popBackStack()
        }
        initListener()
        subscribeObserver()
    }

    private fun register() {
        viewModel.setStateEvent(
            AuthStateEvent.RegisterAttemptEvent(
                register_email_address.text.toString(),
                register_password.text.toString(),
            )
        )
    }

    private fun initListener() {
        emailTextListener()
        passwordTextListener()
    }

    private fun subscribeObserver() {
        viewModel.viewState.observe(viewLifecycleOwner, { viewState ->
            viewState?.let {
                it.registrationFields?.let { registrationFields ->
                    registrationFields.registration_email?.let {
                        register_email_address.setText(it)

                    }
                    registrationFields.registration_password?.let {
                        register_password.setText(it)

                    }
                }
            }
        })


        lifecycleScope.launch {
            viewModel.isRegisterButtonEnabled.collect { value ->
                register_button.isEnabled = value
            }
        }

        viewModel.shouldDisplayProgressBar.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it) {
                    register_button.invisible()
                } else {
                    register_button.visible()
                }
            }

            uiController.displayProgressBar(it)
        })

        viewModel.stateMessage.observe(viewLifecycleOwner, Observer { stateMessage ->
            stateMessage?.response?.let { response ->
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


    override fun onPause() {
        super.onPause()
        viewModel.setRegistrationFields(
            RegistrationFields(
                register_email_address.text.toString(),
                register_password.text.toString(),
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.setRegistrationFields(
            RegistrationFields(
                register_email_address.text.toString(),
                register_password.text.toString(),
            )
        )
    }

    private fun passwordTextListener() {
        register_password.apply {
            addTextChangedListener {
                viewModel.setRegisterPassword(it.toString())
                if (register_password_layout.isErrorEnabled
                ) {
                    register_password_layout.error =
                        viewModel.validatePasswords(this.text.toString())

                }
            }
            onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    viewModel.passwordRegisterFocusInitial=false
                    register_password_layout.isErrorEnabled =
                        !viewModel.validatePasswords(this.text.toString()).isNullOrEmpty()
                    register_password_layout.error =
                        viewModel.validatePasswords(this.text.toString())
                } else {
                    register_password_layout.isErrorEnabled =
                        !viewModel.passwordRegisterFocusInitial
                }

            }
        }
    }

    private fun emailTextListener() {
        register_email_address.apply {
            addTextChangedListener {
                viewModel.setRegisterEmail(it.toString())
                if (register_email_address_layout.isErrorEnabled
                ) {

                    register_email_address_layout.error =
                        viewModel.validateEmail(this.text.toString())

                }
            }
            onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    viewModel.emailRegisterFocusInitial=false
                    register_email_address_layout.isErrorEnabled =
                        !viewModel.validateEmail(this.text.toString()).isNullOrEmpty()
                    register_email_address_layout.error =
                        viewModel.validateEmail(this.text.toString())
                } else {
                    register_email_address_layout.isErrorEnabled =
                        !viewModel.emailRegisterFocusInitial
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


}