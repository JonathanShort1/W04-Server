import java.io.*;
import java.net.*;

class WebServer {

    private static final int PORT_MIN = 5000;
    private static final int PORT_MAX = 60000;
    private static final int DEFAULT_PORT = 45689;

    private static final String DEFAULT_LOG_FILE = "Server/logs/AccessLog.log";
    private static final String EXCEPTION_LOG_FILE = "Server/logs/exceptionLog.log";
    private static final String METADATA_LOG_FILE = "Server/logs/metaDataLog.log";
    private static final String DOCUMENT_ROOT = System.setProperty("my.base.dir","Server"); //for paths in the html files

    public static void main(String argv[]) {
        int portNumber = DEFAULT_PORT;
        String accessLogFileName = DEFAULT_LOG_FILE;
        String command = "start";

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
                    accessLogFileName = argv[2];
                    System.out.println(accessLogFileName);
                }
            }
        }

        PrintWriter writer = null;
        File runFile = null;
        switch (command) {
            case "start" :
                try {
                    System.out.println("starting server");
                    Runtime.getRuntime().exec("java WebServer run &");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "stop" :
                System.out.println("Stopping server");
                if ((runFile = new File("running.txt")).exists()) {
                    runFile.delete();
                }
                break;
            case "run" :
                try {
                    writer = new PrintWriter("running.txt");
                    runFile = new File("running.txt");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                run(portNumber, accessLogFileName, runFile);
                break;
            default:
                System.out.println("please enter start,stop or forward");
                System.exit(0);
        }
    }

    public static void run(int portNumber, String accessLogFileName, File runFile) {
        ServerSocket listenSocket;
        try {
            listenSocket = new ServerSocket(portNumber);
            while(runFile.exists()){
                try {
                    Socket socket = listenSocket.accept();
                    HttpResponse response = new HttpResponse(DOCUMENT_ROOT, accessLogFileName, EXCEPTION_LOG_FILE, METADATA_LOG_FILE, socket);
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


