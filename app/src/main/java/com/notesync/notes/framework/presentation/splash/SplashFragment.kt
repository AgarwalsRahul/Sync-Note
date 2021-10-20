package com.notesync.notes.framework.presentation.splash

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigator
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.NavHostFragment.findNavController
import com.notesync.notes.R
import com.notesync.notes.business.domain.state.StateMessageCallback
import com.notesync.notes.business.interactors.auth.CheckAuthenticatedUser
import com.notesync.notes.framework.presentation.UIController
import com.notesync.notes.framework.presentation.auth.AuthViewModel
import com.notesync.notes.framework.presentation.auth.state.AuthStateEvent
import com.notesync.notes.util.printLogD
import kotlinx.android.synthetic.main.fragment_splash.*
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

    lateinit var logoAnim: Animation

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        logoAnim = AnimationUtils.loadAnimation(context, R.anim.top_animation)

        viewModel.setupChannel()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        app_logo.startAnimation(logoAnim)
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
                it.response.let { response ->
                    printLogD("SplashFragment", "${response.message}")
                    if (response.message == CheckAuthenticatedUser.NO_USER_FOUND) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            val extras =FragmentNavigator.Extras.Builder()
                                .addSharedElement(app_logo, "logo_image").build()
                            findNavController(this).navigate(R.id.action_splashFragment_to_loginFragment,
                            null,null,extras)
                        }, 1500)
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