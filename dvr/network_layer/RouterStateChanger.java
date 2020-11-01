package dvr.network_layer;
import dvr.model.Constants;
import util.kotlinutils.KtUtils;

import java.util.ArrayList;
import java.util.Random;

class RouterStateChanger implements Runnable {

    public Thread thread = null;
    public static boolean islocked = false;
    public static Boolean msg = true;
    public boolean isStopped = false;


    public RouterStateChanger() {
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        Random random = new Random(System.currentTimeMillis());
        while (true) {
            if (isStopped) break;

            double d = random.nextDouble();
            if (d < Constants.LAMBDA) {
                revertRandomRouter();
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void revertRandomRouter() {
        /*
        * Randomly select a router and revert its state
        */

        Random random = new Random(System.currentTimeMillis());

        ArrayList<Router> activeList = KtUtils.INSTANCE.getRoutersOfState(true, NetworkLayerServer.routers);
        ArrayList<Router> downList = KtUtils.INSTANCE.getRoutersOfState(false, NetworkLayerServer.routers);

        float percentageUp = activeList.size() / ( 1.0f * NetworkLayerServer.routers.size());

        Router r;
        if (percentageUp < 0.6) {
            int id = random.nextInt(downList.size());
            r = downList.get(id);
        }
        else if (percentageUp > 0.9) {
            int id = random.nextInt(activeList.size());
            r = activeList.get(id);
        }
        else {
            int id = random.nextInt(NetworkLayerServer.routers.size());
            r = NetworkLayerServer.routers.get(id);
        }
        r.revertState();
        System.out.println("State Changed; Router ID: "+ r.routerId + " " + r.state);

    }
}
