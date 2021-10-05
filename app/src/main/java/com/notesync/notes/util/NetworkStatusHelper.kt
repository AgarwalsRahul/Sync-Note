package com.notesync.notes.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.LiveData
import com.google.android.gms.common.config.GservicesValue.value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author
 * https://betterprogramming.pub/how-to-monitor-internet-connection-in-android-using-kotlin-and-livedata-135de9447796
 */

class NetworkStatusHelper(private val context: Context) :
    LiveData<NetworkStatus>() {

    var connectivityManager: ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val validNetworkConnections: MutableSet<Network> = HashSet()

    private lateinit var connectivityManagerCallback: ConnectivityManager.NetworkCallback



    fun announceStatus() {
        postValue(
            if (validNetworkConnections.isNotEmpty()) {
                NetworkStatus.Available
            } else {
                NetworkStatus.Unavailable
            }
        )
    }

    private fun getConnectivityManagerCallback() =
        object : ConnectivityManager.NetworkCallback() {

            override fun onUnavailable() {
                super.onUnavailable()
                printLogD("NetworkStatusHelper", "onUnavailable")
            }

            override fun onLosing(network: Network, maxMsToLive: Int) {
                super.onLosing(network, maxMsToLive)
                printLogD("NetworkStatusHelper", "onLosing")
            }

            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                val networkCapability = connectivityManager.getNetworkCapabilities(network)
                val hasNetworkConnection =
                    networkCapability?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        ?: false
                printLogD("NetworkStatusHelper", "onAvailable")
                if (hasNetworkConnection) {
                    determineInternetAccess(network)

                }
                announceStatus()
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                printLogD("NetworkStatusHelper", "onLost")
                validNetworkConnections.remove(network)
                announceStatus()
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                printLogD("NetworkStatusHelper", "onCapabilitiesChanged")
                if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                    determineInternetAccess(network)
                    announceStatus()

                } else {
                    validNetworkConnections.remove(network)
                    announceStatus()
                }

                super.onCapabilitiesChanged(network, networkCapabilities)
            }

        }


    private fun determineInternetAccess(network: Network) {
        CoroutineScope(IO).launch {
            if (InternetAvailability.check()) {
                withContext(Main) {
                    validNetworkConnections.add(network)

                }
            }
        }
    }

    override fun onActive() {
        super.onActive()
        printLogD("NetworkStatusHelper", "Active")
        connectivityManagerCallback = getConnectivityManagerCallback()
        val networkRequest = NetworkRequest
            .Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        printLogD("networkStatusHelper", "$networkRequest")
        connectivityManager.registerNetworkCallback(
            networkRequest,
            connectivityManagerCallback
        )
    }

    override fun onInactive() {
        super.onInactive()
        printLogD("NetworkStatusHelper", "Inactive unregistering the connectivity")
        connectivityManager.unregisterNetworkCallback(connectivityManagerCallback)
    }
}