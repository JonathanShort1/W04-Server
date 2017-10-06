import java.io.*;
import java.net.*;
import java.util.*;

class WebServer {

    private static final int PORT_MIN = 5000;
    private static final int PORT_MAX = 60000;
    private static final int DEFAULT_PORT = 45689;

    private static final String HOME_PAGE = "index.html";
    private static final String LOG_FILE = "logs/AccessLog.log";

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
            String status;
            int numOfBytes;

            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            outToClient = new DataOutputStream(connectionSocket.getOutputStream());

            StringTokenizer tokenisedLine = new StringTokenizer("");
            try {
                requestMessageLine = inFromClient.readLine();
                tokenisedLine = new StringTokenizer(requestMessageLine);
            } catch (NullPointerException e) {
                requestMessageLine = "";
                System.out.println("Null pointer at request message!");
                e.printStackTrace();
            }

            String host = getHost(inFromClient);

            if (tokenisedLine.hasMoreTokens() && tokenisedLine.nextToken().equals("GET")) {
                fileName = tokenisedLine.nextToken();
                if (fileName.startsWith("/") && fileName.length() > 1) {
                    fileName = fileName.substring(1);
                } else if (fileName.equals("/")) {
                    fileName = HOME_PAGE;
                }
                try {
                    File file = new File(fileName);
                    numOfBytes = (int)file.length();
                    FileInputStream inFile = new FileInputStream(fileName);
                    byte[] fileInBytes = new byte[numOfBytes];
                    inFile.read(fileInBytes);
                    status = "200";
                    outToClient.writeBytes("HTTP/1.0 " + status + " Document Follows\r\n");
                    if (fileName.endsWith(".jpg")) {
                        outToClient.writeBytes("Content-Type:image/jpeg\r\n");
                    }
                    if (fileName.endsWith(".gif")) {
                        outToClient.writeBytes("Content-Type:image/gif\r\n");
                    }
                    outToClient.writeBytes("Content-Length: " + numOfBytes + "\r\n");
                    outToClient.writeBytes("\r\n");
                    //sendInitialTuple(outToClient, connectionSocket);
                    //outToClient.writeBytes("\r\n");
                    outToClient.write(fileInBytes, 0, numOfBytes);
                } catch (FileNotFoundException e) {
                    status = "404";
                    String errorFile = "error404.html";
                    File file = new File(errorFile);
                    numOfBytes = (int)file.length();
                    FileInputStream inFile = new FileInputStream(errorFile);
                    byte[] fileInBytes = new byte[numOfBytes];
                    inFile.read(fileInBytes);
                    outToClient.writeBytes("HTTP/1.0 " + status + " Not Found\r\n");
                    outToClient.writeBytes(date);
                    outToClient.writeBytes("Content-Length: + " + numOfBytes + "\r\n");
                    outToClient.writeBytes("\r\n");
                    outToClient.write(fileInBytes, 0, numOfBytes);
                }
                logAccess(writer, host, requestMessageLine, date, status, numOfBytes);
                connectionSocket.close();
            } else {
                System.out.println("Bad Request Message, not a GET request");
            }

        } catch (FileNotFoundException e) {
            System.out.println("File does not exist - favicon.ico not found");
      
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getHost(BufferedReader reader) throws IOException {
        String hostHeader = reader.readLine();
        String host = hostHeader.split(" ")[1];
        host = host.substring(host.length() - 4);
        return host;
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
        try {
            InetAddress address = InetAddress.getByName(host);
            writer.println(address.getHostAddress() + " [" + date + "]" + " " + "\"" + requestLine + "\" " + status + " " + numBytes);
            writer.flush();
        } catch (UnknownHostException e) {
            System.out.println("Cannot write to access log file, host name incorrect");
            e.printStackTrace();
        }
    }
}


