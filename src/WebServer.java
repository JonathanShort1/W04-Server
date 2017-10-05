import java.io.*;
import java.net.*;
import java.util.*;

class WebServer {

    public static void main(String argv[]) throws Exception {

        PrintWriter writer = new PrintWriter(new FileWriter("AccessLog.log",true));
        int portNumber = 64578;
        if (argv.length > 0) {
            try {
                portNumber = Integer.parseInt(argv[0]);
            } catch (IllegalArgumentException e) {
                System.out.println("please provide a correct port number");
                e.printStackTrace();
            }
        }
        ServerSocket listenSocket = new ServerSocket(portNumber);
        boolean running = true;
        while(running){
            process(listenSocket, writer);
        }
    }

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


            if (tokenisedLine.hasMoreTokens() && tokenisedLine.nextToken().equals("GET")) {
                fileName = tokenisedLine.nextToken();
                if (fileName.startsWith("/")) {
                    fileName = fileName.substring(1);
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
                logAccess(writer, inFromClient, requestMessageLine, date, status, numOfBytes);
                connectionSocket.close();
            } else {
                System.out.println("Bad Request Message, not a GET request");
            }

        } catch (FileNotFoundException e) {
            System.out.println("File does not exist");
      
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void logAccess(PrintWriter writer, BufferedReader inFromClient, String requestLine, String date, String status, int numBytes) throws IOException {
        String hostHeader = inFromClient.readLine();
        String host = hostHeader.split(" ")[1];
        host = host.substring(host.length() - 4);
        try {
            InetAddress address = InetAddress.getByName(host);
            writer.println(address.getHostAddress() + " [" + date + "]" + " " + "\"" + requestLine + "\" " + status + " " + numBytes);
            writer.flush();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}


