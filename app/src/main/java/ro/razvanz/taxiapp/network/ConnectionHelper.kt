package ro.razvanz.taxiapp.network

import android.content.Context
import android.net.ConnectivityManager
import android.widget.Toast

class ConnectionHelper(val context: Context) {
    fun isConnectedToInternet(): Boolean {
        val connectivityManager =
            context.getSystemService(ConnectivityManager::class.java) as ConnectivityManager
        connectivityManager.activeNetwork ?: return false
        return true
    }

    fun toast(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }
}