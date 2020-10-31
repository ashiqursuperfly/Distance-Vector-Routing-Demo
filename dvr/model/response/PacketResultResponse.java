package dvr.model.response;

import dvr.model.Packet;
import util.kotlinutils.KtUtils;

import java.util.ArrayList;

public class PacketResultResponse implements Response {

    public boolean isSuccess;
    public String message;
    public Packet packet;
    public ArrayList<Integer> path;

    public PacketResultResponse(boolean isSuccess, String message, Packet packet) {
        this.isSuccess = isSuccess;
        this.message = message;
        this.packet = packet;
        path = new ArrayList<>();
    }

    @Override
    public String toJson() {
        return KtUtils.GsonUtil.INSTANCE.toJson(this);
    }

    @Override
    public String toString() {
        return "PacketResultResponse{" +
                "isSuccess=" + isSuccess +
                ", message='" + message + '\'' +
                '}';
    }
}
