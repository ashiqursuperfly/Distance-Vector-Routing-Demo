package dvr.model.response;

import dvr.model.Packet;
import util.kotlinutils.KtUtils;

public class PacketResponse implements Response {
    public Packet data;

    public PacketResponse(Packet data) {
        this.data = data;
    }

    @Override
    public String toJson() {
        return KtUtils.GsonUtil.INSTANCE.toJson(this);
    }
}
