package dvr.network_layer;

import dvr.model.Constants;
import util.kotlinutils.KtUtils;
import util.*;
import dvr.model.EndDevice;
import dvr.model.IPAddress;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

//Work needed
class NetworkLayerServer {

    static ArrayList<Router> routers = new ArrayList<>();

    static int clientCount = 0;
    static RouterStateChanger stateChanger = null;
    static Map<IPAddress, Integer> clientInterfaces = new HashMap<>(); //Each map entry represents number of client end devices connected to the interface
    static Map<IPAddress, EndDevice> endDeviceMap = new HashMap<>();
    static ArrayList<EndDevice> endDevices = new ArrayList<>();
    static Map<Integer, Integer> deviceIDtoRouterID = new HashMap<>();
    static Map<IPAddress, Integer> interfacetoRouterID = new HashMap<>();
    static Map<Integer, Router> routerMap = new HashMap<>();
    static Map<Integer, NetworkUtility> deviceIdToNetworkUtil = new HashMap<>();

    static synchronized void applyDVR(int startingRouterId) {
        if (Constants.DVR_MODE) DVR(startingRouterId);
        else simpleDVR(startingRouterId);
    }

    static synchronized void DVR(int startingRouterId) {

        if (stateChanger != null) stateChanger.isStopped = true;
        try { Thread.sleep(1000); } catch (InterruptedException e) { }

        System.out.println("DVR(" + startingRouterId + ") \nDOWN Routers:");
        for (Router r : routers) {
            if (!r.state) {
                System.out.print(r.routerId + "" + ',');
            }
        }
        System.out.println();

        Router start = routerMap.get(startingRouterId);

        boolean convergence = false;
        while (!convergence) {

            boolean isChanged = updateSingleRouter(start, true);
            for (Router r : routers) {
                // ArrayList<RoutingTableEntry> T = r.routingTable
                if (r.routerId == startingRouterId) continue;
                isChanged |= updateSingleRouter(r, true);
            }
            convergence = !isChanged;
        }

        System.out.println("DVR: Done");

        for (Router r : routers) {
            r.printRoutingTable();
        }

        stateChanger = new RouterStateChanger();

    }

    static synchronized void simpleDVR(int startingRouterId) {

        if (stateChanger != null) stateChanger.isStopped = true;
        try { Thread.sleep(1000); } catch (InterruptedException e) { }

        System.out.println("SIMPLEDVR(" + startingRouterId + ") \nDOWN Routers:");
        for (Router r : routers) {
            if (!r.state) {
                System.out.print(r.routerId + "" + ',');
            }
        }
        System.out.println();

        Router start = routerMap.get(startingRouterId);

        boolean convergence = false;
        while (!convergence) {

            boolean isChanged = updateSingleRouter(start, false);
            for (Router r : routers) {
                // ArrayList<RoutingTableEntry> T = r.routingTable
                if (r.routerId == startingRouterId) continue;
                isChanged |= updateSingleRouter(r, false);
            }
            convergence = !isChanged;
        }

        System.out.println("DVR: Done");

        for (Router r : routers) {
            r.printRoutingTable();
        }

        stateChanger = new RouterStateChanger();


    }

    private static void establishEndDeviceConnection(Socket socket) {
        EndDevice endDevice = getClientDeviceSetup();
        if (endDevice == null) return;
        endDevices.add(endDevice);
        endDeviceMap.put(endDevice.getIpAddress(), endDevice);
        NetworkUtility nu = new NetworkUtility(socket);
        deviceIdToNetworkUtil.put(endDevice.getDeviceID(), nu);
        new ServerThread(nu, endDevice);
        clientCount++;
    }

    private static void disconnectEndDeviceConnection(EndDevice toDelete) {
        int val = clientInterfaces.get(toDelete.getDefaultGateway());
        clientInterfaces.put(toDelete.getDefaultGateway(), --val);
        endDeviceMap.put(toDelete.getIpAddress(), null);
        deviceIDtoRouterID.put(toDelete.getDeviceID(), null);
        endDevices.remove(toDelete);
    }

    private static void initRoutingTables() {
        for (Router router : routers) {
            router.initiateRoutingTable();
        }
    }

    private static boolean updateSingleRouter(Router r, boolean isSF) {

        boolean isChanged = false;

        ArrayList<Router> activeNeighbours = KtUtils.INSTANCE.getActiveNeighbourRouters(r.neighborRouterIDs, routers);
        for (Router t : activeNeighbours) {
            isChanged |= (isSF ? t.sfupdateRoutingTable(r) : t.updateRoutingTable(r));
        }

        return isChanged;
    }

    private static EndDevice getClientDeviceSetup() {

        Router r = KtUtils.INSTANCE.getRandomActiveRouter(routers);

        IPAddress gateway = r.interfaceAddresses.get(0);
        Integer numberOfClientsInGateway = clientInterfaces.get(gateway);
        int deviceID = clientCount;

        IPAddress ip = new IPAddress(gateway.getBytes()[0] + "." + gateway.getBytes()[1] + "." + gateway.getBytes()[2] + "." + (numberOfClientsInGateway + 2));
        clientInterfaces.put(gateway, ++numberOfClientsInGateway);
        deviceIDtoRouterID.put(deviceID, interfacetoRouterID.get(gateway));

        try {
            EndDevice device = new EndDevice(ip, gateway, deviceID);
            System.out.println(device);
            return device;
        } catch (Exception e) {
            Logger.getLogger(NetworkLayerServer.class.getName()).log(Level.SEVERE, null, e);
            return null;
        }

    }

    private static void readTopology() {
        Scanner inputFile = null;
        try {
            inputFile = new Scanner(new File("topology.txt"));
            //skip first 27 lines
            int skipLines = 27;
            for (int i = 0; i < skipLines; i++) {
                inputFile.nextLine();
            }

            //start reading contents
            while (inputFile.hasNext()) {
                inputFile.nextLine();
                int routerId;
                ArrayList<Integer> neighborRouters = new ArrayList<>();
                ArrayList<IPAddress> interfaceAddrs = new ArrayList<>();
                Map<Integer, IPAddress> interfaceIDtoIP = new HashMap<>();

                routerId = inputFile.nextInt();

                int count = inputFile.nextInt();
                for (int i = 0; i < count; i++) {
                    neighborRouters.add(inputFile.nextInt());
                }
                count = inputFile.nextInt();
                inputFile.nextLine();

                for (int i = 0; i < count; i++) {
                    String string = inputFile.nextLine();
                    IPAddress ipAddress = new IPAddress(string);
                    interfaceAddrs.add(ipAddress);
                    interfacetoRouterID.put(ipAddress, routerId);

                    /*
                     * First interface is always client interface
                     */
                    if (i == 0) {
                        //client interface is not connected to any end device yet
                        clientInterfaces.put(ipAddress, 0);
                    } else {
                        interfaceIDtoIP.put(neighborRouters.get(i - 1), ipAddress);
                    }
                }
                Router router = new Router(routerId, neighborRouters, interfaceAddrs, interfaceIDtoIP, true);
                routers.add(router);
                routerMap.put(routerId, router);
            }


        } catch (FileNotFoundException ex) {
            Logger.getLogger(NetworkLayerServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void printRouters() {
        for (int i = 0; i < routers.size(); i++) {
            System.out.println("------------------\n" + routers.get(i));
        }
    }

    public static String strrouters() {
        String string = "";
        for (int i = 0; i < routers.size(); i++) {
            string += "\n------------------\n" + routers.get(i).strRoutingTable();
        }
        string += "\n\n";
        return string;
    }

    public static void main(String[] args) {

        //Task: Maintain an active client list

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(4444);
        } catch (IOException ex) {
            Logger.getLogger(NetworkLayerServer.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("Server Ready: " + serverSocket.getInetAddress().getHostAddress());
        System.out.println("Creating router topology");

        readTopology();
        printRouters();

        initRoutingTables(); //Initialize routing tables for all routers

        // DVR(1); //Update routing table using distance vector routing until convergence
        applyDVR(1);
        // stateChanger = new RouterStateChanger();//Starts a new thread which turns on/off routers randomly depending on parameter dvr.data.Constants.LAMBDA

        while (true) {
            try {
                Socket socket = serverSocket.accept();
                System.out.println("Client" + clientCount + " attempted to connect");
                establishEndDeviceConnection(socket);
            } catch (IOException ex) {
                Logger.getLogger(NetworkLayerServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
