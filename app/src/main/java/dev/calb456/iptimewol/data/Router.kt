package dev.calb456.iptimewol.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routers")
data class Router(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val ipAddress: String,
    val managementPort: Int,
    val loginId: String,
    val password: String
)
