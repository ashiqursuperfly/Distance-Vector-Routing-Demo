package dvr.network_layer;

import dvr.model.*;
import dvr.model.response.EndDeviceListResponse;
import dvr.model.response.PacketResponse;
import dvr.model.response.PacketResultResponse;
import dvr.model.response.SingleEndDeviceResponse;
import util.NetworkUtility;
import util.kotlinutils.KtUtils;

import java.util.ArrayList;

class ServerThread implements Runnable {

    NetworkUtility networkUtility;
    EndDevice endDevice;
    Router defaultGateway;
    ArrayList<Router> latestPacketDeliveryRoute;


    ServerThread(NetworkUtility networkUtility, EndDevice endDevice) {
        this.networkUtility = networkUtility;
        this.endDevice = endDevice;
        defaultGateway = KtUtils.INSTANCE.findRouterInTheNetwork(
                endDevice.getIpAddress(),
                NetworkLayerServer.routers
        );
        latestPacketDeliveryRoute = new ArrayList<>();

        System.out.println("Server Ready for client " + NetworkLayerServer.clientCount);
        new Thread(this).start();
    }

    @Override
    public void run() {
        /*
        * Synchronize actions with client.
        */

        networkUtility.write((new SingleEndDeviceResponse(endDevice)).toJson());
        networkUtility.write((new EndDeviceListResponse(NetworkLayerServer.endDevices)).toJson());

        while (true) {
            String s = (String) networkUtility.read();

            if (s != null) {
                if (s.equals(NetworkUtility.SESSION_END)) break;

                Packet packet = KtUtils.GsonUtil.INSTANCE.fromJson(s, PacketResponse.class).data;
                System.out.println("Received Packet: " + packet.getMessage());
                PacketResultResponse r = deliverPacket(packet);
                networkUtility.write(r.toJson());
            }
        }
        System.out.println("Client with DeviceID:" + endDevice.getDeviceID() + " Left");
        /*
        Tasks:
        1. Upon receiving a packet and recipient, call deliverPacket(packet)
        2. If the packet contains "SHOW_ROUTE" request, then fetch the required information
                and send back to client
        3. Either send acknowledgement with number of hops or send failure message back to client
        */
    }

    private NetworkUtility getDestinationNetworkUtility(IPAddress destinationIp) {
        return NetworkLayerServer.deviceIdToNetworkUtil.get(
                NetworkLayerServer.endDeviceMap.get(
                        destinationIp
                ).getDeviceID()
        );
    }

    private PacketResultResponse deliverPacket(Packet packet) {

        Router destination = KtUtils.INSTANCE.findRouterInTheNetwork(
            packet.getDestinationIP(),
            NetworkLayerServer.routers
        );

        System.out.println("Source Router: " + defaultGateway.routerId + " " + defaultGateway.interfaceAddresses.get(0).getNetworkAddress());
        System.out.println("Destination Router: " + destination.routerId + " " + destination.interfaceAddresses.get(0).getNetworkAddress());

        latestPacketDeliveryRoute.clear();
        PacketResultResponse result = forward(destination,defaultGateway, defaultGateway, packet);
        result.path = latestPacketDeliveryRoute;
        /*System.out.println("Packet Result:");
        System.out.println(result);
        System.out.println(latestPacketDeliveryRoute);*/

        return result;

        /*
        1. Find the router s which has an interface such that the interface and source end device have same network address.
        2. Find the router d which has an interface such that the interface and destination end device have same network address.
        3. Implement forwarding, i.e., s forwards to its gateway router x considering d as the destination.
                similarly, x forwards to the next gateway router y considering d as the destination,
                and eventually the packet reaches to destination router d.

            3(a) If, while forwarding, any gateway x, found from routingTable of router r is in down state[x.state==FALSE]
                    (i) Drop packet
                    (ii) Update the entry with distance Constants.INFTY
                    (iii) Block NetworkLayerServer.stateChanger.t
                    (iv) Apply DVR starting from router r.
                    (v) Resume NetworkLayerServer.stateChanger.t

            3(b) If, while forwarding, a router x receives the packet from router y,
                    but routingTableEntry shows Constants.INFTY distance from x to y,
                    (i) Update the entry with distance 1
                    (ii) Block NetworkLayerServer.stateChanger.t
                    (iii) Apply DVR starting from router x.
                    (iv) Resume NetworkLayerServer.stateChanger.t

        4. If 3(a) occurs at any stage, packet will be dropped,
            otherwise successfully sent to the destination router
        */

        // NetworkUtility destNu = getDestinationNetworkUtility(packet.getDestinationIP());
        // destNu.write(packet.getMessage());

    }

    private PacketResultResponse forward(Router destination, Router previous, Router nextHop, Packet packet) {

        // System.out.println("Forward: " +  destination.routerId + "," + nextHop.routerId);

        RoutingTableEntry nextHopRTE = KtUtils.INSTANCE.searchRoutingTable(destination.routerId, nextHop.routingTable);

        if (!nextHop.state || nextHopRTE.getGatewayRouterId() == -1 || (latestPacketDeliveryRoute.size() > Constants.INFINITY)) {
            if (nextHopRTE != null) nextHopRTE.setDistance(Constants.INFINITY);

            NetworkLayerServer.applyDVR(nextHop.routerId);
            return new PacketResultResponse(false, "Stopped at router:" + nextHop.routerId  , packet);
        }
        else if (nextHop.routerId == destination.routerId) {
            latestPacketDeliveryRoute.add(nextHop);
            return new PacketResultResponse(true, "Packet Sent Successful", packet);
        }

        latestPacketDeliveryRoute.add(nextHop);

        RoutingTableEntry prevToNext = KtUtils.INSTANCE.searchRoutingTable(nextHop.routerId, previous.routingTable);
        if (nextHop.routerId != previous.routerId && prevToNext.getDistance() >= Constants.INFINITY) {
            prevToNext.setDistance(1);
        }

        Router nextNextHop = KtUtils.INSTANCE.findRouter(nextHopRTE.getGatewayRouterId(), NetworkLayerServer.routers);

        return forward(destination, nextHop, nextNextHop, packet);

    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
