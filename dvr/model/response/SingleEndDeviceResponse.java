package dvr.model.response;

import dvr.model.EndDevice;
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
