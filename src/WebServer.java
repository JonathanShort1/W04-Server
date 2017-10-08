import java.io.*;
import java.net.*;

class WebServer {

    private static final int PORT_MIN = 5000;
    private static final int PORT_MAX = 60000;
    private static final int DEFAULT_PORT = 45689;

    private static final String DEFAULT_LOG_FILE = "logs/AccessLog.log";
    private static final String EXCEPTION_LOG_FILE = "logs/exceptionLog.log";

    public static void main(String argv[]) {
        int portNumber = DEFAULT_PORT;
        String accessLogFileName = DEFAULT_LOG_FILE;

        if (argv.length > 0) {
            try {
                int input = Integer.parseInt(argv[0]);
                if (input >= PORT_MIN && input <= PORT_MAX) {
                    portNumber = input;
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            if (argv.length > 1) {
                if (new File(argv[1]).exists()) {
                    accessLogFileName = argv[1];
                }
            }
        }

        ServerSocket listenSocket;
        try {
            listenSocket = new ServerSocket(portNumber);
            boolean running = true;
            while(running){
                try {
                    Socket socket = listenSocket.accept();
                    HttpResponse response = new HttpResponse(accessLogFileName, EXCEPTION_LOG_FILE, socket);
                    response.start();
                } catch (IOException e) {
                    System.out.println("Response creation has failed - wrong file names given for log files");
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.out.println("Server socket creation has failed");
            e.printStackTrace();
        }
    }
}


