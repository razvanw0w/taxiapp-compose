package ro.razvanz.taxiapp.viewmodel

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ro.razvanz.taxiapp.model.Taxi
import ro.razvanz.taxiapp.network.ConnectionHelper
import ro.razvanz.taxiapp.remote.TaxiRemoteApi
import ro.razvanz.taxiapp.repository.TaxiRepository

class TaxiViewModel(
    val repository: TaxiRepository,
    val connectionHelper: ConnectionHelper
): ViewModel() {
    val taxiList: LiveData<List<Taxi>> = repository.taxis

    fun addTaxi(taxi: Taxi) = viewModelScope.launch {
        if (connectionHelper.isConnectedToInternet()) {
            viewModelScope.launch {
                TaxiRemoteApi.service.addTaxi(taxi).enqueue(object : Callback<Taxi> {
                    override fun onResponse(call: Call<Taxi>, response: Response<Taxi>) {
                        if (response.isSuccessful) {
                            viewModelScope.launch {
                                val newTaxi = response.body()!!
                                Log.d("ADD", response.body()!!.toString())
                                repository.add(newTaxi)
                            }
                            Log.d("ADD", "Request succeeded!")
                        }
                    }

                    override fun onFailure(call: Call<Taxi>, t: Throwable) {
                        Toast.makeText(
                            connectionHelper.context,
                            "Could not add taxi to server!",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.d("ADD", "Request failed!")
                    }
                })
            }
        } else {
            repository.add(taxi)
        }
    }

    fun syncDataFromServer() {
        TaxiRemoteApi.service.getTaxis().enqueue(object: Callback<List<Taxi>> {
            override fun onResponse(call: Call<List<Taxi>>, response: Response<List<Taxi>>) {
                response.body()!!.forEach {
                    viewModelScope.launch {
                        repository.add(it)
                    }
                }
            }

            override fun onFailure(call: Call<List<Taxi>>, t: Throwable) {
                TODO("Not yet implemented")
            }
        })
    }
}

class TaxiViewModelFactory(
    private val repository: TaxiRepository,
    private val connectionHelper: ConnectionHelper
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaxiViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaxiViewModel(repository, connectionHelper) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}