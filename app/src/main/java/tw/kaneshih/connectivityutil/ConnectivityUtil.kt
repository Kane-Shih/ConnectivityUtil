package tw.kaneshih.connectivityutil

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.util.Log
import androidx.annotation.MainThread
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.net.NetworkInterface

private const val TAG = "ConnectivityUtil"

object ConnectivityUtil {
    private var isInit = false

    private val networkStateLiveData = MutableLiveData<NetworkState>()

    private lateinit var cm: ConnectivityManager

    @JvmStatic
    fun getNetworkState(): LiveData<NetworkState> {
        return networkStateLiveData
    }

    @JvmStatic
    @MainThread
    fun init(context: Context, isDebug: Boolean) {
        if (!isInit) {
            isInit = true
            cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                initFor23()
            } else {
                initFor21(context)
            }

            if (isDebug) {
                networkStateLiveData.observeForever {
                    Log.d(
                        TAG,
                        "onNetworkChanged: isConnected:${it.isConnected()} / WiFi:${it.isWiFiConnected()} / Mobile:${it.isMobileConnected()}"
                    )
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @MainThread
    private fun initFor23(): NetworkState {
        val state = NetworkStateImpl23()
        cm.registerNetworkCallback(
            NetworkRequest.Builder().build(),
            object : NetworkCallback() {
                override fun onAvailable(network: Network) {
                    Log.d(TAG, "onA $network")
                    state.networkList.add(0, network)
                    val capabilities = cm.getNetworkCapabilities(network)
                    state.capabilitiesMap[network] = capabilities
                    if (capabilities != null) {
                        networkStateLiveData.postValue(state)
                    }
                }

                override fun onLost(network: Network) {
                    Log.d(TAG, "onLost $network")
                    state.networkList.remove(network)
                    state.capabilitiesMap[network] = null
                    networkStateLiveData.postValue(state)
                }

                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                    Log.d(TAG, "onCapCh $network")
                    if (network !in state.networkList
                        && networkCapabilities.hasInternet()
                    ) {
                        state.networkList.add(0, network)
                    }
                    state.capabilitiesMap[network] = networkCapabilities
                    networkStateLiveData.postValue(state)
                }
            })
        updateStateAndNotify(state)
        return state
    }

    private fun NetworkCapabilities.hasWiFiTransport() =
        this.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)

    private fun NetworkCapabilities.hasMobileTransport() =
        this.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)

    @RequiresApi(Build.VERSION_CODES.M)
    private fun NetworkCapabilities.hasInternet() =
        this.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                && this.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

    @Suppress("DEPRECATION")
    @MainThread
    private fun initFor21(context: Context): NetworkState {
        val state = NetworkStateImpl21()
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        context.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (ConnectivityManager.CONNECTIVITY_ACTION == intent.action) {
                    updateStateAndNotify(state)
                }
            }
        }, intentFilter)
        updateStateAndNotify(state)
        return state
    }

    @Suppress("DEPRECATION")
    @MainThread
    private fun updateStateAndNotify(
        stateImpl21: NetworkStateImpl21
    ) {
        val activeNetwork = cm.activeNetworkInfo
        if (activeNetwork != null) {
            stateImpl21.isConnect = activeNetwork.isConnected
            val type = activeNetwork.type
            stateImpl21.isWiFi = type == ConnectivityManager.TYPE_WIFI
            stateImpl21.isMobile = type == ConnectivityManager.TYPE_MOBILE
        } else {
            stateImpl21.isConnect = false
            stateImpl21.isWiFi = false
            stateImpl21.isMobile = false
        }
        networkStateLiveData.value = stateImpl21
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @MainThread
    private fun updateStateAndNotify(
        stateImpl23: NetworkStateImpl23
    ) {
        val network = cm.activeNetwork
        if (network != null) {
            stateImpl23.networkList.add(0, network)
            stateImpl23.capabilitiesMap[network] = cm.getNetworkCapabilities(network)
        }
        networkStateLiveData.value = stateImpl23
    }

    @JvmStatic
    val isConnected: Boolean
        get() = networkStateLiveData.value?.isConnected() == true

    @JvmStatic
    val isWiFiConnected: Boolean
        get() = networkStateLiveData.value?.isWiFiConnected() == true

    @JvmStatic
    val isMobileConnected: Boolean
        get() = networkStateLiveData.value?.isMobileConnected() == true

    // ipv6
    @JvmStatic
    val localIpAddress: String
        get() {
            try {
                val en = NetworkInterface.getNetworkInterfaces()
                while (en.hasMoreElements()) {
                    val intf = en.nextElement()
                    val enumIpAddr = intf.inetAddresses
                    while (enumIpAddr.hasMoreElements()) {
                        val inetAddress = enumIpAddr.nextElement()
                        if (!inetAddress.isLoopbackAddress) {
                            val ipAddr = inetAddress.hostAddress
                            if (ipAddr.indexOf(':') > 0) { // ipv6
                                val p = ipAddr.indexOf('%')
                                return if (p < 0) ipAddr else ipAddr.substring(0, p)
                            }
                            return inetAddress.hostAddress
                        }
                    }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            return "127.0.0.1"
        }

    interface NetworkState {
        fun isWiFiConnected(): Boolean
        fun isMobileConnected(): Boolean
        fun isConnected(): Boolean
    }

    private class NetworkStateImpl21 : NetworkState {
        var isConnect = false
        var isWiFi = false
        var isMobile = false

        override fun isWiFiConnected(): Boolean {
            return this.isConnect && this.isWiFi
        }

        override fun isMobileConnected(): Boolean {
            return this.isConnect && this.isMobile
        }

        override fun isConnected(): Boolean {
            return this.isConnect
        }
    }

    private class NetworkStateImpl23 : NetworkState {
        var networkList: MutableList<Network> = mutableListOf()
        var capabilitiesMap: MutableMap<Network, NetworkCapabilities?> = mutableMapOf()

        override fun isWiFiConnected(): Boolean {
            return networkList.firstOrNull()?.let {
                capabilitiesMap[it]?.hasWiFiTransport()
            } ?: false
        }

        override fun isMobileConnected(): Boolean {
            return networkList.firstOrNull()?.let {
                capabilitiesMap[it]?.hasMobileTransport()
            } ?: false
        }

        @RequiresApi(Build.VERSION_CODES.M)
        override fun isConnected(): Boolean {
            return networkList.firstOrNull()?.let {
                capabilitiesMap[it]?.hasInternet()
            } ?: false
        }
    }
}