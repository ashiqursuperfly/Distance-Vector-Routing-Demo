package dvr;//Work needed
import util.kotlinutils.KtUtils;
import dvr.data.Constants;
import dvr.data.IPAddress;
import dvr.data.RoutingTableEntry;

import java.util.ArrayList;
import java.util.Map;

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
            /* Random random = new Random();
            double p = random.nextDouble();
            if (p < 0.80) state = true;
            else state = false;*/
            ArrayList<Integer> selected = new ArrayList<>();
            selected.add(6);
            selected.add(7);
            selected.add(11);

            if (selected.contains(routerId)) state = false;
            else state = true;


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
    * for itself, distance=0; for any connected router with state=true, distance=1; otherwise distance=dvr.data.Constants.INFTY;
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
            Router dest = KtUtils.INSTANCE.findRouter(destId, NetworkLayerServer.routers);

            String _t = destId + "";
            if (!dest.state) _t += '*';

            System.out.print(_t);
            System.out.println();
            return;
        }


        if (next.getGatewayRouterId() == -1){
            System.out.println();
            return;
        }

        Router nextHop = KtUtils.INSTANCE.findRouter(next.getGatewayRouterId(), NetworkLayerServer.routers);


        System.out.print(next.getGatewayRouterId() + "-");
        RoutingTableEntry r = KtUtils.INSTANCE.searchRoutingTable(destId, nextHop.routingTable);
        printPath(destId, r);

    }

    public String strRoutingTable() {
        StringBuilder string = new StringBuilder("Router" + routerId + "\n");
        string.append("DestID Distance Nexthop\n");
        for (RoutingTableEntry routingTableEntry : routingTable) {
            string.append(routingTableEntry.getRouterId())
                  .append(" ")
                  .append(routingTableEntry.getDistance())
                  .append(" ")
                  .append(routingTableEntry.getGatewayRouterId())
                  .append("\n");
        }

        string.append("-----------------------\n");
        return string.toString();
    }

}
