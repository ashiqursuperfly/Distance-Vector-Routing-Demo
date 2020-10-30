package dvr.data;

import util.kotlinutils.KtUtils;

import java.io.Serializable;
import java.util.Objects;

public class EndDevice implements Serializable {

    private IPAddress ipAddress;
    private IPAddress gateway;
    private int deviceID;

    public EndDevice(IPAddress ipAddress, IPAddress gateway, int deviceID) {
        this.ipAddress = ipAddress;
        this.gateway = gateway;
        this.deviceID = deviceID;
    }

    public IPAddress getIpAddress() {
        return ipAddress;
    }

    public IPAddress getGateway() { return gateway; }

    public Integer getDeviceID() { return deviceID; }

    @Override
    public String toString() {
        return "\nDeviceID: " + deviceID + "\nIP: " + ipAddress + "\nDefault Gateway: " + gateway;
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
