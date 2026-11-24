package com.example.pw3.pz12

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/**
 * Перевірка наявності активного підключення до Інтернету перед виконанням мережевого запиту.
 */
fun isInternetAvailable(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}