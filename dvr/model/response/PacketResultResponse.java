package dvr.model.response;

import dvr.model.Packet;
import dvr.network_layer.Router;
import util.kotlinutils.KtUtils;

import java.util.ArrayList;

public class PacketResultResponse implements Response {

    public boolean isSuccess;
    public String message;
    public Packet packet;
    public ArrayList<Router> path;

    public PacketResultResponse(boolean isSuccess, String message, Packet packet) {
        this.isSuccess = isSuccess;
        this.message = message;
        this.packet = packet;
        this.path = new ArrayList<>();
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

    public String getPath() {
        StringBuilder sb = new StringBuilder();

        sb.append("Path:");
        for (Router r:
             path) {
            sb.append(r.routerId).append("-");
        }

        return sb.toString();
    }
}
