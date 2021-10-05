package com.notesync.notes.framework.presentation.auth.state

import android.os.Parcelable
import androidx.constraintlayout.motion.utils.ViewState
import com.notesync.notes.business.domain.model.User
import kotlinx.android.parcel.Parcelize


data class AuthViewState(
    var loginFields: LoginFields? = null,
    var registrationFields: RegistrationFields? = null,
    var user: User? = null,
    var forgotPasswordFields: ForgotPasswordFields? = null
) : com.notesync.notes.business.domain.state.ViewState

@Parcelize
data class RegistrationFields(
    var registration_email: String? = null,

    var registration_password: String? = null,

    ) : Parcelable {

    class RegistrationError {
        companion object {

            fun mustFillAllFields(): String {
                return "All fields are required."
            }

            fun passwordsDoNotMatch(): String {
                return "Passwords must match."
            }

            fun none(): String {
                return "None"
            }

        }
    }

    fun isValidForRegistration(): String {
        if (registration_email.isNullOrEmpty()

            || registration_password.isNullOrEmpty()

        ) {
            return RegistrationError.mustFillAllFields()
        }


        return RegistrationError.none()
    }
}

@Parcelize
data class LoginFields(
    var login_email: String? = null,
    var login_password: String? = null
) : Parcelable {
    class LoginError {

        companion object {

            fun mustFillAllFields(): String {
                return "You can't login without an email and password."
            }

            fun none(): String {
                return "None"
            }

        }
    }

    fun isValidForLogin(): String {

        if (login_email.isNullOrEmpty()
            || login_password.isNullOrEmpty()
        ) {

            return LoginError.mustFillAllFields()
        }
        return LoginError.none()
    }

    override fun toString(): String {
        return "LoginState(email=$login_email, password=$login_password)"
    }


}

@Parcelize
data class ForgotPasswordFields(
    var email: String? = null
) : Parcelable