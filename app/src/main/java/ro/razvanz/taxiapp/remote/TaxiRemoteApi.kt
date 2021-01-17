package ro.razvanz.taxiapp.remote

import androidx.lifecycle.MutableLiveData
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import ro.razvanz.taxiapp.model.Taxi

object TaxiRemoteApi {
    const val URL = "http://192.168.0.190:1957"
    const val WS_URL = "ws://192.168.0.190:1957"

    interface Service {
        @GET("/all")
        fun getTaxis(): Call<List<Taxi>>

        @POST("/cab")
        fun addTaxi(@Body taxi: Taxi): Call<Taxi>

        @GET("/colors")
        fun getColors(): Call<List<String>>

        @GET("/cabs/{color}")
        fun getCabsByColor(@Path("color") color: String): Call<List<Taxi>>

        @DELETE("/cab/{id}")
        fun deleteCab(@Path("id") id: Int): Call<Taxi>

        @GET("/my/{driver}")
        fun getByDriver(@Path("driver") driver: String): Call<List<Taxi>>
    }

    private val interceptor: HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        this.level = HttpLoggingInterceptor.Level.BODY
    }

    val client: OkHttpClient = OkHttpClient().newBuilder().apply {
        this.addInterceptor(interceptor)
    }.build()

    var gson = GsonBuilder().setLenient().create()

    private val retrofit = Retrofit.Builder()
        .baseUrl(URL)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(client)
        .build()

    val service: Service = retrofit.create(Service::class.java)
}

class TaxiWebSocket(private val broadcastTaxi: MutableLiveData<Taxi>) : WebSocketListener() {
    override fun onMessage(webSocket: WebSocket, text: String) {
        val taxi = TaxiRemoteApi.gson.fromJson(text, Taxi::class.java)
        broadcastTaxi.postValue(taxi)
    }
}



