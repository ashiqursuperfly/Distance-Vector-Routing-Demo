package dvr;//Work needed
import kotlinutils.KtUtils;
import util.Constants;
import util.IPAddress;
import util.RoutingTableEntry;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class Router {
    public int routerId;
    public int numberOfInterfaces;
    public ArrayList<IPAddress> interfaceAddresses;//list of IP address of all interfaces of the router
    public ArrayList<RoutingTableEntry> routingTable;//used to implement DVR
    public ArrayList<Integer> neighborRouterIDs;//Contains both "UP" and "DOWN" state routers
    public Boolean state;//true represents "UP" state and false is for "DOWN" state
    public Map<Integer, IPAddress> neighbourRouterIDToInterfaceIP;

    public Router(int routerId, ArrayList<Integer> neighborRouters, ArrayList<IPAddress> interfaceAddresses, Map<Integer, IPAddress> neighbourRouterIDToInterfaceIP, boolean randomiseStates) {
        this.routerId = routerId;
        this.interfaceAddresses = interfaceAddresses;
        this.neighborRouterIDs = neighborRouters;
        this.neighbourRouterIDToInterfaceIP = neighbourRouterIDToInterfaceIP;
        routingTable = new ArrayList<>();


        if (randomiseStates) {
            /* 80% Probability that the router is up */
            Random random = new Random();
            double p = random.nextDouble();
            if (p < 0.80) state = true;
            else state = false;
        } else {
            state = true;
        }

        numberOfInterfaces = interfaceAddresses.size();
    }

    @Override
    public String toString() {
        String string = "";
        string += "Router ID: " + routerId + "\n" + "Interfaces: \n";
        for (int i = 0; i < numberOfInterfaces; i++) {
            string += interfaceAddresses.get(i).getString() + "\t";
        }
        string += "\n" + "Neighbor Router IDs: \n";
        for (int i = 0; i < neighborRouterIDs.size(); i++) {
            string += neighborRouterIDs.get(i) + "\t";
        }
        return string;
    }



    /**
    * Initialize the distance(hop count) for each router.
    * for itself, distance=0; for any connected router with state=true, distance=1; otherwise distance=util.Constants.INFTY;
    */
    public void initiateRoutingTable() {

        for (Router other : NetworkLayerServer.routers) {

            double distance = Constants.INFINITY;
            int gateWayId = -1;

            if (routerId == other.routerId) {
                distance = 0.0f;
                gateWayId = routerId;
            }
            else if (neighborRouterIDs.contains(other.routerId) && state) {
                distance = 1;
                gateWayId = other.routerId;
            }

            RoutingTableEntry routingTableEntry = new RoutingTableEntry(other.routerId, distance, gateWayId);
            routingTable.add(routingTableEntry);
        }

    }

    /**
    * Delete all the routingTableEntry
    */
    public void clearRoutingTable() {
        routingTable.clear();
    }

    /**
     * Update the routing table for this router using the entries of Router neighbor
     * @param neighbor
     */
    public boolean updateRoutingTable(Router neighbor) {

        boolean isSuccessful = false;

        double thisRouterToNeighbourDistance = KtUtils.INSTANCE.searchRoutingTable(neighbor.routerId, routingTable).getDistance();

        ArrayList<RoutingTableEntry> neighbourTable = neighbor.routingTable;

        for (RoutingTableEntry entry: routingTable) {

            if (entry.getRouterId() == routerId) continue;

            double neighbourToOtherRouterDistance = KtUtils.INSTANCE.searchRoutingTable(entry.getRouterId(), neighbourTable).getDistance();

            if (entry.getDistance() > thisRouterToNeighbourDistance + neighbourToOtherRouterDistance) {
                //System.out.println(entry.getDistance() + " > " + (thisRouterToNeighbourDistance + neighbourToOtherRouterDistance));

                entry.setDistance(thisRouterToNeighbourDistance + neighbourToOtherRouterDistance);
                entry.setGatewayRouterId(neighbor.routerId);
                isSuccessful = true;
            }

        }

        return isSuccessful;
    }

    public boolean sfupdateRoutingTable(Router neighbor) {
        return false;
    }

    /**
     * If the state was up, down it; if state was down, up it
     */
    public void revertState() {
        state = !state;
        if(state) { initiateRoutingTable(); }
        else { clearRoutingTable(); }
    }

    public void printRoutingTable() {
        System.out.println("Router " + routerId);
        System.out.println("DestID Distance");
        for (RoutingTableEntry routingTableEntry : routingTable) {
            System.out.print(routingTableEntry.getRouterId() + " " + routingTableEntry.getDistance() + " :::: " + routerId+"-");
            printPath(routingTableEntry.getRouterId(), routingTableEntry);
        }



        System.out.println("-----------------------");
    }

    public void printPath(int destId, RoutingTableEntry next) {

        if (next.getGatewayRouterId() == destId) {
            System.out.print(destId);
            System.out.println();
            return;
        }

        System.out.print(next.getGatewayRouterId() + "-");
        Router nextHop = KtUtils.INSTANCE.findRouter(next.getGatewayRouterId(),NetworkLayerServer.routers);
        printPath(destId, KtUtils.INSTANCE.searchRoutingTable(destId, nextHop.routingTable));

    }

    public String strRoutingTable() {
        String string = "Router" + routerId + "\n";
        string += "DestID Distance Nexthop\n";
        for (RoutingTableEntry routingTableEntry : routingTable) {
            string += routingTableEntry.getRouterId() + " " + routingTableEntry.getDistance() + " " + routingTableEntry.getGatewayRouterId() + "\n";
        }

        string += "-----------------------\n";
        return string;
    }

}
