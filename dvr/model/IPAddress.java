package dvr.model;

import java.io.Serializable;
import java.util.Objects;

//Done!
public class IPAddress implements Serializable {

    private short bytes[];
    private String string;

    public IPAddress(String string) {
        bytes = new short[4];
        this.string = string;
        String[] temp = string.split("\\.");
        for (int i = 0; i < 4; i++) {
            bytes[i] = Short.parseShort(temp[i]);
        }

    }

    public short[] getBytes()
    {
        return bytes;
    }

    public String getString()
    {
        return string;
    }

    @Override
    public String toString() { return string; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IPAddress)) return false;
        IPAddress ipAddress = (IPAddress) o;
        return Objects.equals(string, ipAddress.string);
    }

    @Override
    public int hashCode() {
        return Objects.hash(string);
    }
}
