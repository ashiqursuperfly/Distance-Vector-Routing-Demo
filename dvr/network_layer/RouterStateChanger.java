package dvr.network_layer;
import dvr.network_layer.NetworkLayerServer;
import dvr.model.Constants;

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
        int id = random.nextInt(NetworkLayerServer.routers.size());
        Router r = NetworkLayerServer.routers.get(id);
        r.revertState();
        System.out.println("State Changed; Router ID: "+ NetworkLayerServer.routers.get(id).routerId + " " + r.state);

    }
}
