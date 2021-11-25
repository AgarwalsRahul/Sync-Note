package com.notesync.notes.framework.presentation.changePassword

import com.notesync.notes.business.domain.state.StateEvent
import com.notesync.notes.business.domain.util.Validators
import com.notesync.notes.business.domain.util.validatePassword
import com.notesync.notes.business.interactors.auth.AuthInteractors
import com.notesync.notes.business.interactors.auth.ChangePassword
import com.notesync.notes.framework.presentation.changePassword.state.ChangePasswordEvent
import com.notesync.notes.framework.presentation.changePassword.state.ChangePasswordViewState
import com.notesync.notes.framework.presentation.common.BaseViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
@FlowPreview
class ChangePasswordViewModel(private val changePassword: ChangePassword) :
    BaseViewModel<ChangePasswordViewState>() {

    private val _oldPassword = MutableStateFlow("")
    private val _newPassword = MutableStateFlow("")

    var oldPasswordFocusInitial=true
    var newPasswordFocusInitial=true


    val isSubmitButtonEnabled: Flow<Boolean> =
        combine(_oldPassword, _newPassword) { oldPassword, newPassword ->
            val isOldPasswordValid = validatePasswords(oldPassword) == null
            val isNewPasswordValid = validatePasswords(newPassword) == null
            return@combine isOldPasswordValid and isNewPasswordValid
        }

    override fun handleNewData(data: ChangePasswordViewState) {

    }

    override fun setStateEvent(stateEvent: StateEvent) {
        val job = when (stateEvent) {
            is ChangePasswordEvent -> {
                changePassword.changePassword(
                    stateEvent.oldPassword,
                    stateEvent.newPassword,
                    stateEvent
                )
            }
            else -> {
                emitInvalidStateEvent(stateEvent)
            }
        }
        launchJob(stateEvent, job)
    }

    override fun initNewViewState(): ChangePasswordViewState {
        return ChangePasswordViewState()
    }

    fun setOldPassword(password: String) {
        _oldPassword.update {
            password
        }
    }

    fun setChangePasswordFields(oldPassword: String, newPassword: String) {
        val update = getCurrentViewStateOrNew()
        if (update.oldPassword == oldPassword && update.newPassword == newPassword) {
            return
        }
        update.oldPassword = oldPassword
        update.newPassword = newPassword
        setViewState(update)
    }

    fun setNewPassword(password: String) {
        _newPassword.update {
            password
        }
    }

    fun validatePasswords(password: String): String? {
        return when (val validators = validatePassword(password)) {
            is Validators.Success -> null
            is Validators.ValueFailure -> validators.data
        }
    }
}