import java.io.*;
import java.net.*;

class WebServer {

    private static final int PORT_MIN = 5000;
    private static final int PORT_MAX = 60000;
    private static final int DEFAULT_PORT = 45689;

    private static final String DEFAULT_LOG_FILE = "logs/AccessLog.log";
    private static final String EXCEPTION_LOG_FILE = "logs/exceptionLog.log";

    public static void main(String argv[]) {
        PrintWriter writerAccessLog = null;
        PrintWriter writerExceptionLog = null;
        int portNumber = DEFAULT_PORT;
        String fileName = DEFAULT_LOG_FILE;

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
                fileName = argv[1];
            }
        }

        try {
            writerAccessLog = new PrintWriter(new FileWriter(fileName,true));
            writerExceptionLog = new PrintWriter(new FileWriter(EXCEPTION_LOG_FILE, true));
        } catch (IOException e) {
            System.out.println("Connecting writer(s) to log file(s) has failed - possibly wrong path given");
            e.printStackTrace();
        }

        ServerSocket listenSocket;
        try {
            listenSocket = new ServerSocket(portNumber);
            boolean running = true;
            while(running){
                try {
                    Socket socket = listenSocket.accept();
                    HttpResponse response = new HttpResponse(writerAccessLog, writerExceptionLog, socket);
                    response.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            writerAccessLog.close();
            writerExceptionLog.close();
        } catch (IOException e) {
            System.out.println("Server socket creation has failed");
            e.printStackTrace();
        }
    }
}


