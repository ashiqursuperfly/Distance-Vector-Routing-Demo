package kotlinutils

import dvr.Router
import util.RoutingTableEntry
import java.util.*

object KtUtils {

    fun searchRoutingTable(searchedRouterID: Int, routingTable: ArrayList<RoutingTableEntry>): RoutingTableEntry? {
        return routingTable.find {
            it.routerId == searchedRouterID
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


}