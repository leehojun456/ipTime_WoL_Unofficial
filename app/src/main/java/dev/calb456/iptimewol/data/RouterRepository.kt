package dev.calb456.iptimewol.data

import kotlinx.coroutines.flow.Flow

class RouterRepository(private val routerDao: RouterDao) {

    val allRouters: Flow<List<Router>> = routerDao.getAllRouters()

    suspend fun insert(router: Router) {
        routerDao.insertRouter(router)
    }

    suspend fun deleteRouterById(routerId: Int) {
        routerDao.deleteRouterById(routerId)
    }
}
