package dvr.data.response;

import dvr.data.EndDevice;
import util.kotlinutils.KtUtils;

public class EndDeviceConfigResponse {
    public EndDevice data;

    public EndDeviceConfigResponse(EndDevice data) {
        this.data = data;
    }

    public String toJson() {
        return KtUtils.GsonUtil.INSTANCE.toJson(this);
    }
}
