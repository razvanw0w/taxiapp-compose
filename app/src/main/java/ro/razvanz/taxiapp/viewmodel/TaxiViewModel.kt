package ro.razvanz.taxiapp.viewmodel

import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import okhttp3.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ro.razvanz.taxiapp.dto.DriverInfo
import ro.razvanz.taxiapp.model.Taxi
import ro.razvanz.taxiapp.network.ConnectionHelper
import ro.razvanz.taxiapp.remote.TaxiRemoteApi
import ro.razvanz.taxiapp.remote.TaxiWebSocket
import ro.razvanz.taxiapp.repository.TaxiRepository

class TaxiViewModel(
    val repository: TaxiRepository,
    val connectionHelper: ConnectionHelper
) : ViewModel() {
    val taxiList: LiveData<List<Taxi>> = repository.taxis
    val _colorList: MutableLiveData<List<String>> = MutableLiveData(emptyList())
    val _cabsForColorList: MutableLiveData<List<Taxi>> = MutableLiveData(emptyList())
    val _reportSize: MutableLiveData<List<Taxi>> = MutableLiveData(emptyList())
    val _reportNumberOfCabs: MutableLiveData<List<DriverInfo>> = MutableLiveData(emptyList())
    val _reportCapacity: MutableLiveData<List<Taxi>> = MutableLiveData(emptyList())
    val _cabsForDriver: MutableLiveData<List<Taxi>> = MutableLiveData(emptyList())
    val broadcastTaxi: MutableLiveData<Taxi> = MutableLiveData(Taxi())
    val loading: MutableState<Boolean> = mutableStateOf(false)

    init {
        broadcastTaxi.observeForever { taxi ->
            if (taxi != Taxi()) {
                Log.d("WEBSOCKET", taxi.toString())
                connectionHelper.toast(taxi.toString())
                Log.d("WEBSOCKET", "kinda showed toast")
            }
        }
        TaxiRemoteApi.client.newWebSocket(
            Request.Builder().url(TaxiRemoteApi.WS_URL).build(),
            TaxiWebSocket(broadcastTaxi)
        )
    }

    fun addTaxi(taxi: Taxi) = viewModelScope.launch {
        if (connectionHelper.isConnectedToInternet()) {
            viewModelScope.launch {
                loading.value = true
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
                        loading.value = false
                    }

                    override fun onFailure(call: Call<Taxi>, t: Throwable) {
                        Toast.makeText(
                            connectionHelper.context,
                            "Could not add taxi to server!",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.d("ADD", "Request failed!")
                        loading.value = false
                    }
                })
            }
        } else {
            loading.value = true
            repository.add(taxi)
            loading.value = false
        }
    }

    fun syncDataFromServer() {
        loading.value = true
        TaxiRemoteApi.service.getTaxis().enqueue(object : Callback<List<Taxi>> {
            override fun onResponse(call: Call<List<Taxi>>, response: Response<List<Taxi>>) {
                response.body()!!.forEach {
                    viewModelScope.launch {
                        repository.add(it)
                    }
                }
                loading.value = false
            }

            override fun onFailure(call: Call<List<Taxi>>, t: Throwable) {
                loading.value = false
            }
        })
    }

    fun syncColorsFromServer() {
        loading.value = true
        TaxiRemoteApi.service.getColors().enqueue(object : Callback<List<String>> {
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                _colorList.value = response.body()!!
                loading.value = false
            }

            override fun onFailure(call: Call<List<String>>, t: Throwable) {
                loading.value = false
            }
        })
    }

    fun syncCabsByColorFromServer(color: String) {
        loading.value = true
        TaxiRemoteApi.service.getCabsByColor(color).enqueue(object : Callback<List<Taxi>> {
            override fun onResponse(call: Call<List<Taxi>>, response: Response<List<Taxi>>) {
                _cabsForColorList.value = response.body()!!
                loading.value = false
            }

            override fun onFailure(call: Call<List<Taxi>>, t: Throwable) {
                loading.value = false
            }
        })
    }

    fun deleteTaxi(id: Int) {
        loading.value = true
        TaxiRemoteApi.service.deleteCab(id).enqueue(object : Callback<Taxi> {
            override fun onResponse(call: Call<Taxi>, response: Response<Taxi>) {
                if (response.isSuccessful) {
                    val taxi = response.body()!!
                    viewModelScope.launch {
                        repository.delete(taxi.id)
                        loading.value = false
                    }
                } else {
                    Log.d("DELETE", "Failed!")
                    loading.value = false
                }
            }

            override fun onFailure(call: Call<Taxi>, t: Throwable) {
                Log.d("DELETE", "Failed!")
                loading.value = false
            }
        })
    }

    fun syncTop10CabsBySize() {
        loading.value = true
        TaxiRemoteApi.service.getTaxis().enqueue(object : Callback<List<Taxi>> {
            override fun onResponse(call: Call<List<Taxi>>, response: Response<List<Taxi>>) {
                _reportSize.value = response.body()!!.sortedByDescending { it.size }.take(10)
                loading.value = false
            }

            override fun onFailure(call: Call<List<Taxi>>, t: Throwable) {
                loading.value = false
            }
        })
    }

    fun syncTop10Drivers() {
        loading.value = true
        TaxiRemoteApi.service.getTaxis().enqueue(object : Callback<List<Taxi>> {
            override fun onResponse(call: Call<List<Taxi>>, response: Response<List<Taxi>>) {
                _reportNumberOfCabs.value =
                    response.body()!!.groupBy { it.driver }
                        .map { DriverInfo(it.key, it.value.size) }
                        .sortedByDescending { it.cabs }
                        .take(10)
                loading.value = false
            }

            override fun onFailure(call: Call<List<Taxi>>, t: Throwable) {
                loading.value = false
            }
        })
    }

    fun syncTop5BiggestCabs() {
        loading.value = true
        TaxiRemoteApi.service.getTaxis().enqueue(object : Callback<List<Taxi>> {
            override fun onResponse(call: Call<List<Taxi>>, response: Response<List<Taxi>>) {
                _reportCapacity.value = response.body()!!.sortedByDescending { it.capacity }.take(5)
                loading.value = false
            }

            override fun onFailure(call: Call<List<Taxi>>, t: Throwable) {
                loading.value = false
            }
        })
    }

    fun syncCabsForDriver(driver: String) {
        loading.value = true
        TaxiRemoteApi.service.getByDriver(driver).enqueue(object : Callback<List<Taxi>> {
            override fun onResponse(call: Call<List<Taxi>>, response: Response<List<Taxi>>) {
                if (response.isSuccessful) {
                    _cabsForDriver.value = response.body()!!
                }
                loading.value = false
            }

            override fun onFailure(call: Call<List<Taxi>>, t: Throwable) {
                loading.value = false
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