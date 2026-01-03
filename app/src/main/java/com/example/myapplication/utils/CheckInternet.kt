package com.example.myapplication.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object CheckInternet{
    fun isNetworkConnected(context: Context): Boolean{
        var connectivityManager: ConnectivityManager = context.applicationContext.
        getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        var network = connectivityManager.activeNetwork ?: return false
        var networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        var realIntenet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

        var hasEthenet = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        var hasWifi = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        var hasCellular = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)

        return realIntenet && (hasEthenet || hasWifi || hasCellular)

    }
}