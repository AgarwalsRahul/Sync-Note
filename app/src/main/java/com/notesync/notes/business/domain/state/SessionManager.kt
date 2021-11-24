package com.notesync.notes.business.domain.state

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.notesync.notes.business.data.cache.abstraction.AuthCacheDataSource
import com.notesync.notes.business.domain.model.User
import com.notesync.notes.framework.dataSource.cache.database.NoteDatabase
import com.notesync.notes.framework.presentation.BaseApplication
import com.notesync.notes.util.printLogD
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
@ExperimentalCoroutinesApi
@DelicateCoroutinesApi
@FlowPreview
@ObsoleteCoroutinesApi
class SessionManager
@Inject constructor(
    private val authCacheDataSource: AuthCacheDataSource,
    private val noteDatabase: NoteDatabase,
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
                    noteDatabase.clearAllTables()
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
                printLogD("SessionManager","Setting value to null")
                _cachedUser.value = newValue
            }

        }
    }

}