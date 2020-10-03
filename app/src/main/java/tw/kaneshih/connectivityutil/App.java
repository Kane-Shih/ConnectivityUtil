package tw.kaneshih.connectivityutil;

import android.app.Application;
import android.os.Build;
import android.util.Log;

import androidx.lifecycle.Observer;
import tw.kaneshih.connectivity.ConnectivityUtil;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Log.d("Kane", "App onCreate");

        Log.d("Kane", "init, API " + Build.VERSION.SDK_INT);
        ConnectivityUtil.init(this, BuildConfig.DEBUG);

        Log.d("Kane", "first ..." +
                      " isConnected?" + ConnectivityUtil.isConnected() +
                      " isWiFi?" + ConnectivityUtil.isWiFiConnected() +
                      " isMobile?" + ConnectivityUtil.isMobileConnected()
        );

        ConnectivityUtil.getNetworkState()
                        .observeForever(new Observer<ConnectivityUtil.NetworkState>() {
                            @Override
                            public void onChanged(ConnectivityUtil.NetworkState networkState) {
                                Log.d("Kane", "onChanged ..." +
                                              " isConnected?" + ConnectivityUtil.isConnected() +
                                              " isWiFi?" + ConnectivityUtil.isWiFiConnected() +
                                              " isMobile?" + ConnectivityUtil.isMobileConnected()
                                );
                            }
                        });

    }
}
