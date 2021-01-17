package ro.razvanz.taxiapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "taxi")
data class Taxi(
    @PrimaryKey var id: Int = 0,
    var name: String = "",
    var status: String = "",
    var size: Int = 0,
    var driver: String = "",
    var color: String = "",
    var capacity: Int = 0
)