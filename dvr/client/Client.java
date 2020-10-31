package dvr.client;

import dvr.model.EndDevice;
import dvr.model.Packet;
import dvr.model.response.EndDeviceListResponse;
import dvr.model.response.PacketResponse;
import dvr.model.response.SingleEndDeviceResponse;
import util.NetworkUtility;
import util.kotlinutils.KtUtils;

import java.util.ArrayList;
import java.util.Random;

//Work needed
public class Client {

    private static EndDevice myConfig;
    private static ArrayList<EndDevice> activeClients;
    private static final NetworkUtility networkUtility = new NetworkUtility("127.0.0.1", 4444);

    public static void main(String[] args) throws InterruptedException {

        System.out.println(":::::::::::::::::::");
        System.out.println("Connected to server");
        String response = "";

        response = (String) networkUtility.read();
        SingleEndDeviceResponse singleEndDeviceResponse = KtUtils.GsonUtil.INSTANCE.fromJson(response , SingleEndDeviceResponse.class);
        myConfig = singleEndDeviceResponse.data;

        response = (String) networkUtility.read();
        EndDeviceListResponse endDeviceListResponse = KtUtils.GsonUtil.INSTANCE.fromJson(response , EndDeviceListResponse.class);
        activeClients = endDeviceListResponse.data;

        System.out.println("MyConfig: " + myConfig.toString());
        System.out.println("ActiveClients: " + activeClients.toString());

        for (int i = 0; i < 3; i++) {

            if (activeClients.size() == 1) break;

            Random random = new Random(System.currentTimeMillis());
            int r = Math.abs(random.nextInt(activeClients.size()));

            EndDevice randomReceiver = activeClients.get(r);

            if (randomReceiver.getDeviceID() == myConfig.getDeviceID()) {
                i--;
                continue;
            }


            Packet message = new Packet();
            message.setSourceIP(myConfig.getIpAddress());
            message.setDestinationIP(randomReceiver.getIpAddress());
            message.setMessage("Hello From " + myConfig.getDeviceID() + " to " + randomReceiver.getDeviceID());

            networkUtility.write((new PacketResponse(message)).toJson());
        }

        while (true) {
            String s = (String) networkUtility.read();
            if (s != null) {
                System.out.println("Received:" + s);
            }
        }

        /*
        * Tasks

        1. Receive EndDevice configuration from server
        2. Receive active client list from server
        3. for(int i=0;i<100;i++)
        4. {
        5.      Generate a random message
        6.      Assign a random receiver from active client list
        7.      if(i==20)
        8.      {
        9.            Send the message and recipient IP address to server and a special request "SHOW_ROUTE"
        10.           Display routing path, hop count and routing table of each router [You need to receive
                            all the required info from the server in response to "SHOW_ROUTE" request]
        11.     }
        12.     else
        13.     {
        14.           Simply send the message and recipient IP address to server.
        15.     }
        16.     If server can successfully send the message, client will get an acknowledgement along with hop count
                    Otherwise, client will get a failure message [dropped packet]
        17. }
        18. Report average number of hops and drop rate
        */
    }
}
