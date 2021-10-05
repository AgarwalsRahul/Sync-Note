package com.notesync.notes.framework.presentation.splash

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment.findNavController
import com.notesync.notes.R
import com.notesync.notes.business.domain.state.StateMessageCallback
import com.notesync.notes.business.interactors.auth.CheckAuthenticatedUser
import com.notesync.notes.framework.presentation.UIController
import com.notesync.notes.framework.presentation.auth.AuthViewModel
import com.notesync.notes.framework.presentation.auth.state.AuthStateEvent
import com.notesync.notes.util.printLogD
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi

@FlowPreview
@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class SplashFragment(private val viewModelProvider: ViewModelProvider.Factory) :
    Fragment(R.layout.fragment_splash) {

    val viewModel: AuthViewModel by activityViewModels {
        viewModelProvider
    }

    lateinit var uiController: UIController

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        viewModel.setupChannel()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeObserver()

    }

    override fun onResume() {
        super.onResume()
        checkPreviousAuthUser()
    }

    private fun checkPreviousAuthUser() {
        Log.d("SplashFragment", "EVVENT CHECK PREVIOUS USER")
        viewModel.setStateEvent(AuthStateEvent.CheckPreviousAuthUser())
    }

    private fun subscribeObserver() {


        viewModel.stateMessage.observe(viewLifecycleOwner, { stateMessage ->
            stateMessage?.let {
                it.response?.let { response ->
                    printLogD("SplashFragment", "${response.message}")
                    if (response.message == CheckAuthenticatedUser.NO_USER_FOUND) {
                        findNavController(this).navigate(R.id.action_splashFragment_to_loginFragment)
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
            }
        })


    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.let {
            uiController = context as UIController

        }
    }


}