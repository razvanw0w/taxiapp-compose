package ro.razvanz.taxiapp.repository

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ro.razvanz.taxiapp.model.Taxi

@Dao
interface TaxiDAO {
    @Query("select * from taxi")
    fun getAll(): LiveData<List<Taxi>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(taxi: Taxi)
}