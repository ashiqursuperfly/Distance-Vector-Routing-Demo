package util.kotlinutils

import com.google.gson.Gson
import dvr.model.IPAddress
import dvr.network_layer.Router
import dvr.model.RoutingTableEntry
import java.util.*


object KtUtils {

    fun searchRoutingTable(searchedRouterID: Int, routingTable: ArrayList<RoutingTableEntry>): RoutingTableEntry? {
        return routingTable.find {
            it.destinationRouterId == searchedRouterID
        }
    }

    fun getActiveNeighbourRouters(neighbourIds: ArrayList<Int>, routers: ArrayList<Router>): ArrayList<Router> {
        return ArrayList(routers.filter {
            neighbourIds.contains(it.routerId) && it.state
        })
    }

    fun findRouter(searchedRouterID: Int, routers: ArrayList<Router>): Router? {
        return routers.find {
            it.routerId == searchedRouterID
        }
    }

    fun findRouterInTheNetwork(demoIPinNetwork: IPAddress, routers: ArrayList<Router>): Router? {
        return routers.find {
            it.interfaceAddresses.find { it2 ->
                it2.networkAddress == demoIPinNetwork.networkAddress
            } != null
        }
    }

    object GsonUtil {
        fun toJson(obj: Any): String {
            return Gson().toJson(obj)
        }

        fun<T> fromJson(jsonStr: String, any: Class<T>): T {
            return Gson().fromJson(jsonStr, any)
        }
    }


}