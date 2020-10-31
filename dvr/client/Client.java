package dvr.client;

import dvr.model.EndDevice;
import dvr.model.Packet;
import dvr.model.response.EndDeviceListResponse;
import dvr.model.response.PacketResponse;
import dvr.model.response.PacketResultResponse;
import dvr.model.response.SingleEndDeviceResponse;
import util.NetworkUtility;
import util.kotlinutils.KtUtils;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

//Work needed
public class Client {

    private static final int TOTAL_PACKETS_TO_SEND = 100;
    private static EndDevice myConfig;
    private static ArrayList<EndDevice> activeClients;
    private static final NetworkUtility networkUtility = new NetworkUtility("127.0.0.1", 4444);
    private static ArrayList<PacketResultResponse> packetResultResponses = new ArrayList<>();

    public static void main(String[] args) throws InterruptedException {

        System.out.println(":::::::::::::::::::");
        System.out.println("Connected to server");
        String response = "";

        response = (String) networkUtility.read();
        SingleEndDeviceResponse singleEndDeviceResponse = KtUtils.GsonUtil.INSTANCE.fromJson(response, SingleEndDeviceResponse.class);
        myConfig = singleEndDeviceResponse.data;

        response = (String) networkUtility.read();
        EndDeviceListResponse endDeviceListResponse = KtUtils.GsonUtil.INSTANCE.fromJson(response, EndDeviceListResponse.class);
        activeClients = endDeviceListResponse.data;

        System.out.println("MyConfig: " + myConfig.toString());
        System.out.println("ActiveClients: " + activeClients.toString());

        Random random = new Random(System.currentTimeMillis());

        for (int i = 0; i < TOTAL_PACKETS_TO_SEND ; i++) {

            if (activeClients.size() <= 3) break;

            /*Scanner sc = new Scanner(System.in);
            System.out.println("Press Y to send a random packet");
            String input = sc.nextLine();
            if (!input.trim().equals("Y")) {
                i--;
                continue;
            }*/

            int r = random.nextInt(activeClients.size());
            EndDevice randomReceiver = activeClients.get(r);

            if (randomReceiver.getDeviceID() == myConfig.getDeviceID() || randomReceiver.getDefaultGateway().getNetworkAddress().equals(myConfig.getDefaultGateway().getNetworkAddress())) {
                i--;
                continue;
            }

            Packet message = new Packet();
            message.setSourceIP(myConfig.getIpAddress());
            message.setDestinationIP(randomReceiver.getIpAddress());
            message.setMessage("Hello From " + myConfig.getDeviceID() + " to " + randomReceiver.getDeviceID());

            networkUtility.write((new PacketResponse(message)).toJson());
        }

        for (int i = 0; i < TOTAL_PACKETS_TO_SEND ; i++) {
            String s = (String) networkUtility.read();
            if (s != null) {
                PacketResultResponse packetResultResponse = KtUtils.GsonUtil.INSTANCE.fromJson(s, PacketResultResponse.class);
                packetResultResponses.add(packetResultResponse);
            }
        }

        printStats();

        while (true) {

        }



        /*
        * Tasks

        1. Receive EndDevice configuration from server
        2. Receive active client list from server
        3. for(int i=0; i<100; i++)
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

    private static void printStats() {
        int totalDrops = 0;
        int totalHops = 0;

        System.out.println("Stats");
        for (PacketResultResponse r: packetResultResponses) {
            if (r.isSuccess) totalHops += r.path.size();
            else totalDrops += 1;
            System.out.println(r.packet.getSourceIP() + "-->" + r.packet.getDestinationIP());
            System.out.println(r.path);
        }
        System.out.println("Avg Hops: " + (totalHops*100/TOTAL_PACKETS_TO_SEND));
        System.out.println("Avg Drops: " + (totalDrops*100/TOTAL_PACKETS_TO_SEND));

    }
}
