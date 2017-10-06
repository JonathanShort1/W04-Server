import java.io.*;
import java.net.*;
import java.util.*;

class WebServer {

    private static final int PORT_MIN = 5000;
    private static final int PORT_MAX = 60000;
    private static final int DEFAULT_PORT = 45689;

    private static final String HOME_PAGE = "index.html";
    private static final String LOG_FILE = "logs/AccessLog.log";
    private static final String HTTP_V = "HTTP/1.0";

    public static void main(String argv[]) {

        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new FileWriter(LOG_FILE,true));
        } catch (IOException e) {
            System.out.println("Connecting writer to log file has failed - possibly wrong path given");
            e.printStackTrace();
        }
        int portNumber = DEFAULT_PORT;
        if (argv.length > 0) {
            try {
                int input = Integer.parseInt(argv[0]);
                if (input >= PORT_MIN && input <= PORT_MAX) {
                    portNumber = input;
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        ServerSocket listenSocket = null;
        try {
            listenSocket = new ServerSocket(portNumber);
        } catch (IOException e) {
            System.out.println("Server socket creation has failed");
            e.printStackTrace();
        }
        boolean running = true;
        while(running){
            process(listenSocket, writer);
        }
    }

    //TODO NEED TO SPLIT THIS INTO MULTIPLE METHODS
    //Don't Repeat Yo-self

    private static void process(ServerSocket listenSocket, PrintWriter writer) {
        String requestMessageLine;
        String fileName;
        DataOutputStream outToClient;

        try{
            Socket connectionSocket = listenSocket.accept();
            String date = new Date().toString();
            HttpResponse httpResponse = new HttpResponse( HTTP_V);
            File file;

            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            outToClient = new DataOutputStream(connectionSocket.getOutputStream());

            StringTokenizer tokenisedLine = new StringTokenizer("");
            String host = "";
            try {
                requestMessageLine = inFromClient.readLine();
                tokenisedLine = new StringTokenizer(requestMessageLine);
                //host = getHost(inFromClient);
            } catch (NullPointerException e) {
                requestMessageLine = "";
                System.out.println("Null pointer at request message!");
                e.printStackTrace();
            }

            if (tokenisedLine.hasMoreTokens() && tokenisedLine.nextToken().equals("GET")) {
                fileName = tokenisedLine.nextToken();
                if (fileName.startsWith("/") && fileName.length() > 1) {
                    fileName = fileName.substring(1);
                } else if (fileName.equals("/")) {
                    fileName = HOME_PAGE;
                }

                try {
                    file = new File(fileName);
                    if (file.exists()) {
                        httpResponse.setFile(file);
                        httpResponse.setStatus("200");
                        httpResponse.setReasonPhrase("Ok");
                        httpResponse.setupResponse();
                        httpResponse.respond(outToClient, date);
                    } else {
                        String errorFile = "error404.html";
                        file = new File(errorFile);
                        httpResponse.setFile(file);
                        httpResponse.setStatus("404");
                        httpResponse.setReasonPhrase("Not Found");
                        httpResponse.setupResponse();
                        httpResponse.respond(outToClient, date);
                    }
                } catch (FileNotFoundException e) {
                    System.out.println("File requested not found!");
                    e.printStackTrace();
                }
            } else {
                String errorFile = "error400.html";
                file = new File(errorFile);
                httpResponse.setFile(file);
                httpResponse.setStatus("400");
                httpResponse.setReasonPhrase("Bad Request");
                httpResponse.setupResponse();
                httpResponse.respond(outToClient, date);
            }
            logAccess(writer, connectionSocket.getInetAddress().getHostAddress(), requestMessageLine, date, httpResponse.getStatus(), httpResponse.getNumOfBytes());
            connectionSocket.close();
        } catch (FileNotFoundException e) {
            System.out.println("File does not exist - favicon.ico not found");
      
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendInitialTuple(DataOutputStream outputStream, Socket socket) throws IOException {
        try {
            TCPTuple tuple = new TCPTuple(socket.getInetAddress().getHostAddress(),socket.getPort(), socket.getLocalAddress().getHostAddress(), socket.getLocalPort());
            outputStream.writeBytes("Host: " + tuple.getHostAddress()
                    + " Host port: " + tuple.getHostPortNumber()
                    + "\nLocal: " + tuple.getLocalAddress()
                    + " Local port: " + tuple.getLocalPortNumber());
        } catch (UnknownHostException e) {
            System.out.println("Cannot send initial TCP tuple, Host name was not correct");
            e.printStackTrace();
        }
    }

    private static void logAccess(PrintWriter writer, String host, String requestLine, String date, String status, int numBytes) throws IOException {
        writer.println(host + " [" + date + "]" + " " + "\"" + requestLine + "\" " + status + " " + numBytes);
        writer.flush();
    }
}


