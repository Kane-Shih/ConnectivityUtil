### ConnectivityUtil

1. Include

```
implementation 'tw.kaneshih.connectivity:lib:1.0.0'
```

2. Initialization

```
ConnectivityUtil.init(context, isDebug)
```

3. Observe or Retrieve

```
ConnectivityUtil.getNetworkState()
            .observe(this,
                     Observer<ConnectivityUtil.NetworkState> { state ->
                         val isConnected = state.isConnected()
                         val isWiFi = state.isWiFiConnected()
                         val isMobile = state.isMobileConnected()
                     }
            )
```

```
val isConnected = ConnectivityUtil.isConnected
val isWiFi = ConnectivityUtil.isWiFiConnected
val isMobile = ConnectivityUtil.isMobileConnected
```


