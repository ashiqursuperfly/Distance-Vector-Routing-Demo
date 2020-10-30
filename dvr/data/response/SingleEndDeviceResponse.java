package dvr.data.response;

import dvr.data.EndDevice;
import util.kotlinutils.KtUtils;

public class SingleEndDeviceResponse implements Response {
    public EndDevice data;

    public SingleEndDeviceResponse(EndDevice data) {
        this.data = data;
    }

    @Override
    public String toJson() {
        return KtUtils.GsonUtil.INSTANCE.toJson(this);
    }
}
