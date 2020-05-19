package tw.kaneshih.connectivityutil

import android.annotation.SuppressLint
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        networkState.text = "isConnected? ${ConnectivityUtil.isConnected}" +
                "\nisWiFi? ${ConnectivityUtil.isWiFiConnected}" +
                "\nisMobile? ${ConnectivityUtil.isMobileConnected}"
        ipAddr.text = ConnectivityUtil.localIpAddress

        ConnectivityUtil.getNetworkState().observe(
            this,
            Observer { state ->
                networkState.text =
                    "isConnected? ${state?.isConnected()}" +
                            "\nisWiFi? ${state?.isWiFiConnected()}" +
                            "\nisMobile? ${state?.isMobileConnected()}"
                ipAddr.text = ConnectivityUtil.localIpAddress
            })

        apiLevel.text = "API ${Build.VERSION.SDK_INT}"
    }
}
