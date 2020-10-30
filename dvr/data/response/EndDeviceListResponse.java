package dvr.data.response;

import dvr.data.EndDevice;
import util.kotlinutils.KtUtils;

import java.util.ArrayList;

public class EndDeviceListResponse implements Response {

    public ArrayList<EndDevice> data;

    public EndDeviceListResponse(ArrayList<EndDevice> data) {
        this.data = data;
    }

    @Override
    public String toJson() {
        return KtUtils.GsonUtil.INSTANCE.toJson(this);
    }
}
