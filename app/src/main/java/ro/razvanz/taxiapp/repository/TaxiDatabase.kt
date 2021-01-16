package ro.razvanz.taxiapp.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ro.razvanz.taxiapp.model.Taxi

@Database(entities = arrayOf(Taxi::class), version = 1, exportSchema = false)
public abstract class TaxiDatabase : RoomDatabase() {
    abstract fun taxiDAO(): TaxiDAO

    companion object {
        @Volatile
        private var INSTANCE: TaxiDatabase? = null

        fun getDatabase(context: Context): TaxiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaxiDatabase::class.java,
                    "taxi_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}