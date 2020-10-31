package dvr.network_layer;

import dvr.model.EndDevice;
import dvr.model.IPAddress;
import dvr.model.RoutingTableEntry;
import dvr.model.response.EndDeviceListResponse;
import dvr.model.response.PacketResponse;
import dvr.model.response.PacketResultResponse;
import dvr.model.response.SingleEndDeviceResponse;
import util.NetworkUtility;
import dvr.model.Packet;
import util.kotlinutils.KtUtils;

import java.util.ArrayList;

class ServerThread implements Runnable {

    NetworkUtility networkUtility;
    EndDevice endDevice;
    Router defaultGateway;
    ArrayList<Integer> latestPacketDeliveryRoute;


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
                Packet packet = KtUtils.GsonUtil.INSTANCE.fromJson(s, PacketResponse.class).data;
                System.out.println("Received Packet: " + packet.getMessage());
                deliverPacket(packet);
            }
        }
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

    private Boolean deliverPacket(Packet packet) {

        Router destination = KtUtils.INSTANCE.findRouterInTheNetwork(
            packet.getDestinationIP(),
            NetworkLayerServer.routers
        );

        if (destination == null) {
            try {
                throw new Exception("Could Not Find Destination Router for packet destination ip:" + packet.getDestinationIP());
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        System.out.println("Source Router: " + defaultGateway.routerId + " " + defaultGateway.interfaceAddresses.get(0).getNetworkAddress());
        System.out.println("Destination Router: " + destination.routerId + " " + destination.interfaceAddresses.get(0).getNetworkAddress());

        latestPacketDeliveryRoute.clear();
        PacketResultResponse result = forward(destination, defaultGateway , packet);
        System.out.println("Packet Result:");
        System.out.println(result);
        latestPacketDeliveryRoute.add(defaultGateway.routerId);
        System.out.println(latestPacketDeliveryRoute);

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


        NetworkUtility destNu = getDestinationNetworkUtility(packet.getDestinationIP());
        destNu.write(packet.getMessage());

        return false;

    }

    private PacketResultResponse forward(Router destination, Router nextHop, Packet packet) {

        RoutingTableEntry nextHopRTE = KtUtils.INSTANCE.searchRoutingTable(destination.routerId, nextHop.routingTable);

        if (!nextHop.state || nextHopRTE == null || nextHopRTE.getGatewayRouterId() == -1) {
            NetworkLayerServer.simpleDVR(nextHop.routerId);
            return new PacketResultResponse(false, destination.routerId + "DOWN", packet);
        }

        else if (nextHop.routerId == destination.routerId) {
            // latestPacketDeliveryRoute.add(nextHop.routerId);
            return new PacketResultResponse(true, "Packet Sent Successful", packet);
        }

        // TODO: handle 3(b)

        Router nextNextHop = KtUtils.INSTANCE.findRouter(nextHopRTE.getGatewayRouterId(), NetworkLayerServer.routers);

        latestPacketDeliveryRoute.add(nextHopRTE.getGatewayRouterId());

        return forward(destination, nextNextHop, packet);

    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
