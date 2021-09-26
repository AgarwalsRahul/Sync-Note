package com.notesync.notes.framework.presentation.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.notesync.notes.R
import com.notesync.notes.business.domain.state.DialogInputCaptureCallback
import com.notesync.notes.framework.dataSource.network.implementation.NoteFirestoreServiceImpl.Companion.EMAIL
import com.notesync.notes.framework.presentation.BaseApplication
import com.notesync.notes.framework.presentation.common.BaseNoteFragment
import com.notesync.notes.util.printLogD
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi
import javax.inject.Singleton

@FlowPreview
@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi


@Singleton
class SplashFragment

constructor(
    private val viewModelFactory: ViewModelProvider.Factory
): BaseNoteFragment(R.layout.fragment_splash) {

    val viewModel: SplashViewModel by viewModels {
        viewModelFactory
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkFirebaseAuth()
    }

    private fun checkFirebaseAuth(){
        if(FirebaseAuth.getInstance().currentUser == null){
            displayCapturePassword()
        }
        else{
            subscribeObservers()
        }
    }


    private fun displayCapturePassword(){
        uiController.displayInputCaptureDialog(
            "Enter Password",
            object: DialogInputCaptureCallback {
                override fun onTextCaptured(text: String) {
                    FirebaseAuth.getInstance()
                        .signInWithEmailAndPassword(EMAIL, text)
                        .addOnCompleteListener {
                            if(it.isSuccessful){
                                printLogD("MainActivity",
                                    "Signing in to Firebase: ${it.result}")
                                subscribeObservers()
                            }
                        }
                }
            }
        )
    }

    private fun subscribeObservers(){
        viewModel.hasSyncBeenExecuted().observe(viewLifecycleOwner, Observer { hasSyncBeenExecuted ->

            if(hasSyncBeenExecuted){
                navNoteListFragment()
            }
        })
    }

    private fun navNoteListFragment(){
        findNavController(this).navigate(R.id.action_splashFragment_to_noteListFragment)
    }

    override fun inject() {

    }

}