package ro.razvanz.taxiapp.remote

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import ro.razvanz.taxiapp.model.Taxi

object TaxiRemoteApi {
    private const val URL = "http://192.168.0.190:1957"

    interface Service {
        @GET("/all")
        fun getTaxis(): Call<List<Taxi>>

        @POST("/cab")
        fun addTaxi(@Body taxi: Taxi): Call<Taxi>
    }

    private val interceptor: HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        this.level = HttpLoggingInterceptor.Level.BODY
    }

    private val client: OkHttpClient = OkHttpClient().newBuilder().apply {
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