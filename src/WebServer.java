import java.io.*;
import java.net.*;

class WebServer {

    private static final int PORT_MIN = 5000;
    private static final int PORT_MAX = 60000;
    private static final int DEFAULT_PORT = 45689;

    private static final String DEFAULT_LOG_FILE = "logs/AccessLog.log";
    private static final String EXCEPTION_LOG_FILE = "logs/exceptionLog.log";
    private static final String METADATA_LOG_FILE = "logs/metaDataLog.log";
    private static final String RUN_FILE = "running.txt";
    private static final String DOCUMENT_ROOT = "Server/"; //for paths in the html files

    /**
     * This is the main method that is runnable from the terminal.
     * @param argv - standard parameter for main method.
     */

    public static void main(String argv[]) {
        int portNumber = DEFAULT_PORT;
        String accessLogFileName = DEFAULT_LOG_FILE;
        String documentRoot = DOCUMENT_ROOT;
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
            }
            if (argv.length > 2) {
                accessLogFileName = argv[2];
            }
            if (argv.length > 3) {
                if (argv[3].endsWith("/")) {
                    documentRoot = argv[3];
                }
            }
        }

        chooseCommand(command, portNumber, accessLogFileName,documentRoot);
    }

    /**
     * This method controls what happens given an input.
     * @param command the command given
     * @param portNumber the port number of the server socket
     * @param documentRoot - the directory holding all the server files.
     * @param accessLogFileName the location of the access log
     */

    public static void chooseCommand(String command, int portNumber, String accessLogFileName, String documentRoot) {
        File runFile;
        switch (command) {
            case "start" :
                try {
                    System.out.println("starting server");
                    String comm = "java WebServer run " + portNumber + " " + accessLogFileName + " " + documentRoot + " &";
                    System.out.println(comm);
                    Runtime.getRuntime().exec(comm);
                    Runtime.getRuntime().exec("touch " + RUN_FILE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "stop" :
                System.out.println("Stopping server");
                if ((runFile = new File(RUN_FILE)).exists()) {
                    runFile.delete();
                }
                break;
            case "run" :
                run(portNumber, accessLogFileName, documentRoot);
                break;
            default:
                System.out.println("please enter start or stop");
                System.exit(0);
        }
    }

    /**
     * This method handles the server.
     * A new thread is spawned every time a connection is made.
     * This loop runs while the RUN_FILE exists.
     * @param portNumber - port number for the server socket
     * @param accessLogFileName - location of access file
     * @param documentRoot - the directory holding all the html and server files.
     */

    public static void run(int portNumber, String accessLogFileName,String documentRoot) {
        ServerSocket listenSocket;
        try {
            File runFile = new File(RUN_FILE);
            listenSocket = new ServerSocket(portNumber);
            while(runFile.exists()){
                try {
                    Socket socket = listenSocket.accept();
                    HttpResponse response = new HttpResponse(documentRoot, accessLogFileName, EXCEPTION_LOG_FILE, METADATA_LOG_FILE, socket);
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
        System.out.println("Stopping server");
        System.exit(0);
    }
}


