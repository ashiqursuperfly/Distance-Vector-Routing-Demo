//Done!

package dvr.model;

public interface Constants {

    int INFINITY = 10;
    double LAMBDA = 0.10;
    double initialDownRouterChance = 0.9;

    public interface MessageType {

        int END_DEVICE_CONFIG = 1;
        int ACTIVE_CLIENT_LIST = 2;

    }

}