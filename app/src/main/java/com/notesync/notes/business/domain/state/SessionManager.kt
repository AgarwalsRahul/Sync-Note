package com.notesync.notes.business.domain.state

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.notesync.notes.business.data.cache.abstraction.AuthCacheDataSource
import com.notesync.notes.business.domain.model.User
import com.notesync.notes.framework.presentation.BaseApplication
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class SessionManager @ExperimentalCoroutinesApi
@Inject constructor(
    private val authCacheDataSource: AuthCacheDataSource,
    val application: BaseApplication
) {

    companion object {
        private const val TAG = "AppDebug"
    }

    private val _cachedUser = MutableLiveData<User?>()
    val cachedUser: LiveData<User?>
        get() = _cachedUser

    fun login(newValue: User) {
        setValue(newValue)
    }

    @DelicateCoroutinesApi
    fun logout() {
        Log.d(TAG, "LOGOUT....")

        GlobalScope.launch(Dispatchers.IO) {
            var errorMessage: String? = null
            try {
                _cachedUser.value?.let {
                    authCacheDataSource.deleteUser(it.id)
                }
            } catch (e: CancellationException) {
                Log.e(TAG, "LOGOUT: ${e.message}")
                errorMessage = e.message
            } catch (e: Exception) {
                Log.e(TAG, "LOGOUT: ${e.message}")
                errorMessage = errorMessage + "\n" + e.message
            } finally {
                errorMessage?.let {
                    Log.e(TAG, "LOGOUT: $it")
                }
                Log.d(TAG, "LOGOUT: Finally....")
                setValue(null)
            }
        }
    }

    fun setValue(newValue: User?) {
        GlobalScope.launch(Dispatchers.Main) {
            if (_cachedUser.value != newValue) {
                _cachedUser.value = newValue
            }
        }
    }

    fun checkNetworkConnection(): Boolean {
        val cm = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        try {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val network = cm.activeNetwork;

                val networkCapabilities = cm.getNetworkCapabilities(network);

                val isInternetSuspended =
                    !networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_SUSPENDED)!!;
                (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                        && !isInternetSuspended);
            } else {
                cm.activeNetworkInfo!!.isConnected
            }

        } catch (e: Exception) {
            Log.e(TAG, "IsNetworkConnected : ${e.message}")
        }
        return false
    }
}