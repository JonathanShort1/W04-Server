import java.io.*;
import java.net.*;
import java.util.*;

class WebServer {

    private static final int PORT_MIN = 5000;
    private static final int PORT_MAX = 60000;
    private static final int DEFAULT_PORT = 45689;



    private static final String LOG_FILE = "logs/AccessLog.log";

    public static void main(String argv[]) {
        PrintWriter writer = null;
        int portNumber = DEFAULT_PORT;
        String fileName = LOG_FILE;

        String command = "";

        if (argv.length > 0) {
            command = argv[0];
            if (argv.length > 1) {
                try {
                    int input = Integer.parseInt(argv[1]);
                    if (input >= PORT_MIN && input <= PORT_MAX) {
                        portNumber = input;
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
                if (argv.length > 2) {
                    fileName = argv[2];
                }
            }
        }

        try {
            writer = new PrintWriter(new FileWriter(fileName,true));
        } catch (IOException e) {
            System.out.println("Connecting writer to log file has failed - possibly wrong path given");
            e.printStackTrace();
        }

        ServerSocket listenSocket;
        try {
            listenSocket = new ServerSocket(portNumber);
            boolean running = true;
            while(running){
                try {
                    Socket socket = listenSocket.accept();
                    HttpResponse response = new HttpResponse(writer, socket);
                    response.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            writer.close();
        } catch (IOException e) {
            System.out.println("Server socket creation has failed");
            e.printStackTrace();
        }
    }



}


