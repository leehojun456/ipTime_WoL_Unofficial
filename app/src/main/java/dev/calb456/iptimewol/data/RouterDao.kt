package dev.calb456.iptimewol.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RouterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRouter(router: Router)

    @Query("SELECT * FROM routers ORDER BY name ASC")
    fun getAllRouters(): Flow<List<Router>>

    @Query("DELETE FROM routers WHERE id = :routerId")
    suspend fun deleteRouterById(routerId: Int)
}
