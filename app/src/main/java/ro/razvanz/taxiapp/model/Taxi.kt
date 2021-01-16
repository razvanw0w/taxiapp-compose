package ro.razvanz.taxiapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "taxi")
data class Taxi(
    @PrimaryKey var id: Int,
    var name: String,
    var status: String,
    var size: Int,
    var driver: String,
    var color: String,
    var capacity: Int
)