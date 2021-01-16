package ro.razvanz.taxiapp.network

import android.content.Context
import android.net.ConnectivityManager

class ConnectionHelper(val context: Context) {
    fun isConnectedToInternet(): Boolean {
        val connectivityManager =
            context.getSystemService(ConnectivityManager::class.java) as ConnectivityManager
        connectivityManager.activeNetwork ?: return false
        return true
    }
}