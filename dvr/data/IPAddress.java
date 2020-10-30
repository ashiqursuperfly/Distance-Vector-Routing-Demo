package dvr.data;

import java.io.Serializable;

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

}
