package com.notesync.notes.framework.presentation.auth

import com.notesync.notes.business.domain.model.User
import com.notesync.notes.business.domain.state.*
import com.notesync.notes.business.domain.util.Validators
import com.notesync.notes.business.domain.util.validateEmailAddress
import com.notesync.notes.business.domain.util.validatePassword
import com.notesync.notes.business.interactors.auth.AuthInteractors
import com.notesync.notes.framework.presentation.auth.state.*
import com.notesync.notes.framework.presentation.common.BaseViewModel
import com.notesync.notes.framework.presentation.splash.NoteNetworkSyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.flow.*


@FlowPreview
@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class AuthViewModel constructor(
    private val authInteractors: AuthInteractors,
    private val noteNetworkSyncManager: NoteNetworkSyncManager
) : BaseViewModel<AuthViewState>() {

    override fun handleNewData(data: AuthViewState) {
        data.user?.let {
            setUser(it)
        }

    }

    fun hasSyncBeenExecuted() = noteNetworkSyncManager.hasSyncBeenExecuted

    private val _loginEmail = MutableStateFlow("")

    private val _loginPassword = MutableStateFlow("")

    private val _registerEmail = MutableStateFlow("")
    private val _registerPassword = MutableStateFlow("")

    private val _forgotPasswordEmail = MutableStateFlow("")


    val forgotPasswordEmail: StateFlow<String>
        get() = _forgotPasswordEmail


    fun syncCacheWithNetwork(scope: CoroutineScope) {
        noteNetworkSyncManager.executeDataSync(scope)
    }

    val isRegisterButtonEnabled: Flow<Boolean> =
        combine(_registerEmail, _registerPassword) { email, password ->
            val isEmailCorrect = validateEmail(email) == null
            val isPasswordValid = validatePasswords(password) == null
            return@combine isEmailCorrect and isPasswordValid
        }

    val isLoginEnabled = combine(_loginEmail, _loginPassword) { email, password ->
        val isEmailCorrect = validateEmail(email) == null
        val isPasswordValid = validatePasswords(password) == null
        return@combine isEmailCorrect and isPasswordValid
    }


    override fun setStateEvent(stateEvent: StateEvent) {

        val job = when (stateEvent) {
            is AuthStateEvent.LoginAttemptEvent -> {

                authInteractors.login.login(stateEvent.email, stateEvent.password, stateEvent)

            }
            is AuthStateEvent.RegisterAttemptEvent -> {
                authInteractors.register.register(stateEvent.email, stateEvent.password, stateEvent)
            }
            is AuthStateEvent.CreateStateMessageEvent -> {
                emitStateMessageEvent(
                    stateMessage = stateEvent.stateMessage,
                    stateEvent = stateEvent
                )
            }

            is AuthStateEvent.ForgotPasswordEvent -> {
                authInteractors.forgotPassword.forgotPassword(stateEvent.email, stateEvent)
            }

            is AuthStateEvent.CheckPreviousAuthUser -> {
                authInteractors.checkAuthenticatedUser.checkPreviousAuthUser(stateEvent)
            }

            else -> {
                emitInvalidStateEvent(stateEvent)
            }
        }

        launchJob(stateEvent, job)
    }

    override fun initNewViewState(): AuthViewState {
        return AuthViewState()
    }

    fun setRegistrationFields(registrationFields: RegistrationFields) {
        val update = getCurrentViewStateOrNew()
        if (update.registrationFields == registrationFields) {
            return
        }
        update.registrationFields = registrationFields
        setViewState(update)
    }

    fun setLoginFields(loginFields: LoginFields) {
        val update = getCurrentViewStateOrNew()
        if (update.loginFields == loginFields) {
            return
        }
        update.loginFields = loginFields
        setViewState(update)
    }

    fun setForgotPasswordFields(forgotPasswordFields: ForgotPasswordFields) {
        val update = getCurrentViewStateOrNew()
        if (update.forgotPasswordFields == forgotPasswordFields) {
            return
        }
        update.forgotPasswordFields = forgotPasswordFields
        setViewState(update)
    }

    private fun setUser(user: User) {
        val update = getCurrentViewStateOrNew()
        if (update.user == user) {
            return
        }
        update.user = user
        setViewState(update)
    }



    fun getLoginEmail(): String? = getCurrentViewStateOrNew().loginFields?.login_email
    fun getRegisterEmail(): String? =
        getCurrentViewStateOrNew().registrationFields?.registration_email

    fun getLoginPassword(): String? = getCurrentViewStateOrNew().loginFields?.login_password
    fun getRegisterPassword(): String? =
        getCurrentViewStateOrNew().registrationFields?.registration_password

    fun setLoginEmail(email: String) {
        _loginEmail.update {
            email
        }
    }


    fun setLoginPassword(password: String) {
        _loginPassword.update {
            password
        }
    }

    fun setRegisterEmail(email: String) {
        _registerEmail.update { email }
    }

    fun setRegisterPassword(password: String) {
        _registerPassword.update { password }
    }

    fun setForgotPasswordEmail(email: String) {
        _forgotPasswordEmail.update { email }
    }

    fun validateEmail(email: String): String? {
        return when (val validators = validateEmailAddress(email)) {
            is Validators.Success -> null
            is Validators.ValueFailure -> validators.data

        }
    }

    fun validatePasswords(password: String): String? {
        return when (val validators = validatePassword(password)) {
            is Validators.Success -> null
            is Validators.ValueFailure -> validators.data
        }
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }

}
