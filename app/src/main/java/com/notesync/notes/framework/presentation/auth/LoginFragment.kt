package com.notesync.notes.framework.presentation.auth

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment.findNavController
import com.notesync.notes.R
import com.notesync.notes.business.domain.state.StateMessageCallback
import com.notesync.notes.framework.presentation.UIController
import com.notesync.notes.framework.presentation.auth.state.AuthStateEvent
import com.notesync.notes.framework.presentation.auth.state.LoginFields
import com.notesync.notes.framework.presentation.common.hideKeyboard
import com.notesync.notes.framework.presentation.common.invisible
import com.notesync.notes.framework.presentation.common.visible
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect

@FlowPreview
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
@DelicateCoroutinesApi
class LoginFragment constructor(private val viewModelFactory: ViewModelProvider.Factory) :
    Fragment() {

    val viewModel: AuthViewModel by activityViewModels {
        viewModelFactory
    }

    lateinit var uiController: UIController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.setupChannel()


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupOnBackPressedCallback()

        login_button.setOnClickListener {

            view.hideKeyboard()
            Log.d("LoginFragment", "Login button is clicked")
            viewModel.setStateEvent(
                AuthStateEvent.LoginAttemptEvent(
                    email_address.text.toString(),
                    password.text.toString()
                )
            )
        }

        register_button.setOnClickListener {

            findNavController(this).navigate(R.id.action_loginFragment_to_registerFragment)
        }

        forgot_password_button.setOnClickListener {

            findNavController(this).navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
        }
        initListener()
        subscribeObservers()
    }


    private fun initListener() {
        emailTextListener()


        passwordTextListener()
    }

    private fun passwordTextListener() {

        password.apply {
            addTextChangedListener {
                viewModel.setLoginPassword(it.toString())
                if (password_layout.isErrorEnabled
                ) {
                    password_layout.error =
                        viewModel.validatePasswords(this.text.toString())

                }
            }
            onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    viewModel.passwordFocusInitial = false
                    password_layout.isErrorEnabled =
                        !viewModel.validatePasswords(this.text.toString()).isNullOrEmpty()
                    password_layout.error =
                        viewModel.validatePasswords(this.text.toString())
                }else{
                    password_layout.isErrorEnabled=!viewModel.passwordFocusInitial
                }

            }
        }
    }

    private fun emailTextListener() {
        email_address.apply {

            addTextChangedListener {
                viewModel.setLoginEmail(it.toString())
                if (email_address_layout.isErrorEnabled
                ) {
                    email_address_layout.error =
                        viewModel.validateEmail(this.text.toString())
                }
            }
            onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    viewModel.emailFocusInitial = false
                    email_address_layout.isErrorEnabled =
                        !viewModel.validateEmail(this.text.toString()).isNullOrEmpty()
                    email_address_layout.error =
                        viewModel.validateEmail(this.text.toString())


                } else {
                    email_address_layout.isErrorEnabled = !viewModel.emailFocusInitial
                }

            }
        }
    }

    @DelicateCoroutinesApi
    private fun subscribeObservers() {
        viewModel.viewState.observe(viewLifecycleOwner, { viewState ->
            viewState?.let {
                it.loginFields?.let { loginFields ->
                    loginFields.login_email?.let {
                        email_address.setText(it)

                    }
                    loginFields.login_password?.let {
                        password.setText(it)

                    }
                }
            }
        })

        lifecycleScope.launch {
            viewModel.isLoginEnabled.collect { value ->
//                if (value != null)
                login_button.isEnabled = value
            }
        }

        viewModel.shouldDisplayProgressBar.observe(viewLifecycleOwner, Observer {
            it?.let {
                email_address_layout.isEnabled = !it
                password_layout.isEnabled = !it
                register_button.isEnabled = !it

                forgot_password_button.isEnabled = !it

                if (it) {
                    login_button.invisible()
                } else {
                    login_button.visible()
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

    private fun setupOnBackPressedCallback() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressed()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun onBackPressed() {
        view?.hideKeyboard()

        if (viewModel.dataChannelManager.getActiveJobs().size == 0) {
            activity?.finish()
        }
        viewModel.cancelActiveJobs()
    }

    override fun onPause() {
        super.onPause()

        viewModel.setLoginFields(
            LoginFields(
                email_address.text.toString(),
                password.text.toString(),
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()

        viewModel.setLoginFields(
            LoginFields(
                email_address.text.toString(),
                password.text.toString()
            )
        )
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.let {
            uiController = context as UIController
        }
    }


}