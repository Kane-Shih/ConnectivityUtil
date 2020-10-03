package tw.kaneshih.connectivityutil

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_main.*
import tw.kaneshih.connectivity.ConnectivityUtil

class MainActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        networkState.text = "isConnected? ${ConnectivityUtil.isConnected}" +
                "\nisWiFi? ${ConnectivityUtil.isWiFiConnected}" +
                "\nisMobile? ${ConnectivityUtil.isMobileConnected}"

        ConnectivityUtil.getNetworkState().observe(
            this,
            Observer { state ->
                networkState.text =
                    "isConnected? ${state?.isConnected()}" +
                            "\nisWiFi? ${state?.isWiFiConnected()}" +
                            "\nisMobile? ${state?.isMobileConnected()}"
            })

        apiLevel.text = "API ${Build.VERSION.SDK_INT}"
    }
}
