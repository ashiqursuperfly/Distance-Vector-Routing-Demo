package dvr.data;

import java.io.Serializable;
import java.util.Objects;

public class EndDevice implements Serializable {

    private IPAddress ipAddress;
    private IPAddress defaultGateway;
    private int deviceID;

    public EndDevice(IPAddress ipAddress, IPAddress defaultGateway, int deviceID) throws Exception {
        this.ipAddress = ipAddress;
        this.defaultGateway = defaultGateway;
        this.deviceID = deviceID;

        if (!isValid(ipAddress, defaultGateway)) {
            throw new Exception("Invalid gateway for endDevice: " + toString());
        }

    }

    public IPAddress getIpAddress() {
        return ipAddress;
    }

    public IPAddress getDefaultGateway() { return defaultGateway; }

    public Integer getDeviceID() { return deviceID; }

    private boolean isValid(IPAddress ipAddress, IPAddress defaultGateway) {
        for (int i=0; i < 3; i++) {
            if (ipAddress.getBytes()[i] != defaultGateway.getBytes()[i]) return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "\nDeviceID: " + deviceID + "\nIP: " + ipAddress + "\nDefault Gateway: " + defaultGateway;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EndDevice)) return false;
        EndDevice endDevice = (EndDevice) o;
        return deviceID == endDevice.deviceID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceID);
    }
}
