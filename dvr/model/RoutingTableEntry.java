package dvr.model;

//Done!
public class RoutingTableEntry {

    private int destinationRouterId;
    private double distance;
    private int gatewayRouterId;

    public RoutingTableEntry(int destinationRouterId, double distance, int gatewayRouterId) {
        this.destinationRouterId = destinationRouterId;
        this.distance = distance;
        this.gatewayRouterId = gatewayRouterId;
    }

    public int getDestinationRouterId() {
        return destinationRouterId;
    }

    public void setDestinationRouterId(int destinationRouterId) {
        this.destinationRouterId = destinationRouterId;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getGatewayRouterId() {
        return gatewayRouterId;
    }

    public void setGatewayRouterId(int gatewayRouterId) {
        this.gatewayRouterId = gatewayRouterId;
    }

    @Override
    public String toString() {
        return "RoutingTableEntry{" +
                "routerId=" + destinationRouterId +
                ", distance=" + distance +
                ", gatewayRouterId=" + gatewayRouterId +
                '}';
    }
}
