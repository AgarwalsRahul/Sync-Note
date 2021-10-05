package com.notesync.notes.framework.presentation.common

import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.notesync.notes.di.auth.AuthScope
import com.notesync.notes.framework.presentation.auth.ForgotPasswordFragment
import com.notesync.notes.framework.presentation.auth.LoginFragment
import com.notesync.notes.framework.presentation.auth.RegisterFragment
import com.notesync.notes.framework.presentation.notedetail.NoteDetailFragment
import com.notesync.notes.framework.presentation.splash.SplashFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
@ObsoleteCoroutinesApi
@AuthScope
class AuthFragmentFactory
@Inject
constructor(
    private val viewModelFactory: ViewModelProvider.Factory,
) : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String) =

        when (className) {

            LoginFragment::class.java.name -> {
                val fragment = LoginFragment(viewModelFactory)
                fragment
            }

            RegisterFragment::class.java.name -> {
                val fragment = RegisterFragment(viewModelFactory as AuthViewModelFactory)
                fragment
            }

            ForgotPasswordFragment::class.java.name -> {
                ForgotPasswordFragment(viewModelFactory)
            }

            SplashFragment::class.java.name -> {
                SplashFragment(viewModelFactory)
            }


            else -> {
                super.instantiate(classLoader, className)
            }
        }
}