package dvr.model.response;

import dvr.model.Packet;
import util.kotlinutils.KtUtils;

public class PacketResultResponse implements Response {

    public boolean isSuccess;
    public String message;
    public Packet packet;

    public PacketResultResponse(boolean isSuccess, String message, Packet packet) {
        this.isSuccess = isSuccess;
        this.message = message;
        this.packet = packet;
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
