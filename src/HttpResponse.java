import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;

/**
 * This class implements Runnable - making it run in a single thread spawned from the WebServer class.
 */

public class HttpResponse implements Runnable{

    private static final String HOME_PAGE = "index.html";
    private static final String ERROR_404 = "error404.html";
    private static final String ERROR_400 = "error400.html";
    private static final String HTTP_V = "HTTP/1.0";

    private String documentRoot;
    private Thread thread;
    private Socket connectionSocket;
    private PrintWriter writerAccessLog;
    private PrintWriter writerExceptionLog;
    private PrintWriter writerMetaData;

    private String status;
    private String reasonPhrase;
    private String date;
    private int numOfBytes;
    private File file;
    private byte[] fileInBytes;
    private String contentType;
    private String location;
    private String request = "GET";

    /**
     * This constructor builds the HttpResponse.
     * @param accessLog the path to the access log.
     * @param exceptionLog the path to the exceptions log.
     * @param metaData the path to the meta data log.
     * @param socket the socket that has connected to the server.
     * @throws IOException - if wrong path names are given.
     */

    HttpResponse(String documentRoot, String accessLog, String exceptionLog, String metaData, Socket socket) throws IOException {
        this.documentRoot = documentRoot;
        this.connectionSocket = socket;
        this.writerAccessLog = new PrintWriter(new FileWriter(documentRoot + accessLog, true));
        this.writerExceptionLog = new PrintWriter(new FileWriter(documentRoot + exceptionLog, true));
        this.writerMetaData = new PrintWriter(new FileWriter(documentRoot + metaData, true));
        this.date = new Date().toString();

    }

    /**
     * This method start the thread.
     */

    public void start() {
        if (this.thread == null) {
            this.thread = new Thread(this, "Connection");
            this.thread.start();
        }
    }

    /**
     * This method makes sure the thread will stop.
     */

    public void stop(){
        if (this.thread.isAlive()) {
            this.thread.interrupt();
        }
    }

    @Override
    public void run() {
        String requestMessageLine;
        File file;

        try {
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(this.connectionSocket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(this.connectionSocket.getOutputStream());
            try {
                requestMessageLine = inFromClient.readLine();
                if (requestMessageLine != null) {
                    StringTokenizer tokenisedLine = new StringTokenizer(requestMessageLine);
                    String request = tokenisedLine.nextToken();
                    if (request.equals("GET") || request.equals("HEAD")) {
                        if (request.equals("HEAD")) {
                            this.request = "HEAD";
                        }
                        String fileName = tokenisedLine.nextToken();
                        if (fileName.startsWith("/") && fileName.length() > 1) {
                            fileName = fileName.substring(1);
                            fileName = fileName.split("\\?")[0];
                        } else if (fileName.equals("/")) {
                            fileName = HOME_PAGE;
                        }

                        findGeoLocation();

                        try {
                            file = new File(documentRoot + fileName);
                            if (file.exists() && !file.isDirectory()) {
                                this.buildResponse(file, HTTP_STATUS.SUCCESS.toString(), HTTP_PHRASE.OK.toString());
                                this.respond(outToClient);
                            } else {
                                file = new File( documentRoot + ERROR_404);
                                this.buildResponse(file, HTTP_STATUS.FILE_NOT_FOUND.toString(), HTTP_PHRASE.NOT_FOUND.toString());
                                this.respond(outToClient);
                            }
                        } catch (FileNotFoundException e) {
                            logException("File requested not found! File name: " + fileName);
                            e.printStackTrace();
                        }
                    } else {
                        file = new File(documentRoot + ERROR_400);
                        this.buildResponse(file, HTTP_STATUS.BAD_REQUEST.toString(), HTTP_PHRASE.BAD_REQUEST.toString());
                        this.respond(outToClient);
                    }
                } else {
                    file = new File(documentRoot + ERROR_400);
                    this.buildResponse(file, HTTP_STATUS.BAD_REQUEST.toString(), HTTP_PHRASE.BAD_REQUEST.toString());
                    this.respond(outToClient);
                }
                outToClient.close();
                inFromClient.close();
            } catch(NullPointerException e) {
                requestMessageLine = "";
                logException("Null pointer at request message! - likely a problem with a missing file");
                e.printStackTrace();
            }
            logAccess(requestMessageLine);
            logMetaData();
            this.connectionSocket.close();
        } catch (IOException e) {
            logException("Failed to create Input and output readers from socket streams OR not found ERROR_400 file");
            e.printStackTrace();
        }
        this.writerAccessLog.close();
        this.writerExceptionLog.close();
        this.writerMetaData.close();
        stop();
    }

    /**
     * This method sets other parts of the HttpResponse Object once some processing has been done in run().
     * @param file sets the file that will the send in response body.
     * @param status the status of the response e.g. 200 means successful request
     * @param reasonPhrase the human-readable phrase that accompanies the status
     * @throws IOException - if the file does not exist.
     */

    private void buildResponse(File file, String status, String reasonPhrase) throws IOException {
        this.setFile(file);
        this.setStatus(status);
        this.setReasonPhrase(reasonPhrase);
        this.numOfBytes = (int)this.file.length();
        FileInputStream fileInputStream = new FileInputStream(file.getPath());
        this.fileInBytes = new byte[numOfBytes];
        fileInputStream.read(fileInBytes);
        this.setContentType(this.findContentType(file.getName()));
    }

    /**
     * This method writes out the http response including the body.
     * A line is written out after the file with the content of the meta data log in it.
     * @param outToClient - the dataOutputStream connected to the socket.
     * @throws IOException - if the outputStream is not instantiated properly.
     */

    private void respond(DataOutputStream outToClient) throws IOException {
        outToClient.writeBytes(HTTP_V + " " + this.status + " " + this.reasonPhrase + "\r\n");
        outToClient.writeBytes(this.date + "\r\n");
        outToClient.writeBytes("Content-Length: + " + this.numOfBytes + "\r\n");
        if (!(this.contentType == null)) {
            outToClient.writeBytes(this.contentType);
        }
        outToClient.writeBytes("\r\n");
        if (this.request.equals("GET")){
            outToClient.write(this.fileInBytes, 0, this.numOfBytes);
        }
        outToClient.writeBytes("Location: " + getLocation() + "; " + tcpConnection());
    }

    /**
     * This method logs all access request.
     * The log closely but not strictly follows the Common Log Format.
     * @param requestLine - the request header from the http request.
     * @throws IOException - if the writer is not properly connected to the access log.
     * @see <a href="https://en.wikipedia.org/wiki/Common_Log_Format">Common Log format</a>
     */

    private void logAccess(String requestLine) throws IOException {
        this.writerAccessLog.println(this.connectionSocket.getInetAddress().getHostAddress() + " [" + this.date + "]" + " " + "\"" + requestLine + "\" " + this.status + " " + this.numOfBytes);
        this.writerAccessLog.flush();
    }

    /**
     * This method logs any Exceptions that are thrown in this class.
     * @param message - reason for exception being thrown
     */

    private void logException(String message) {
        this.writerExceptionLog.println(this.date + " Exception: " + message);
        this.writerExceptionLog.flush();
    }

    /**
     * This method logs the host and local address and port respectively and the location of the host.
     */

    private void logMetaData() {
        this.writerMetaData.println("Date: " + this.date + tcpConnection() + "; " + "Location: " + getLocation());
    }


    /**
     * This method build a string of the host and local address and port respectively.
     * @return a string of the host and local address and ports
     */

    private String tcpConnection(){
        String tcpConnection;
        tcpConnection = "Host address: " + this.connectionSocket.getInetAddress().getHostAddress() + ","
                + " Port: " + this.connectionSocket.getPort() + ";"
                + " Local address: " + this.connectionSocket.getLocalAddress() + ","
                + " Local port: " + this.connectionSocket.getLocalPort();
        return tcpConnection;
    }

    /**
     * This method instantiates the GeoApiResult object to find the location of a given IP address.
     * @return the location of a given IP address.
     */

    private void findGeoLocation() {
        GeoApiResult geoApiResult = new GeoApiResult(this.connectionSocket.getInetAddress().getHostAddress());
        this.location = geoApiResult.findLocation();
    }

    /**
     * This method sets the status of the http response.
     * @param status of the http response.
     */

    private void setStatus(String status) {
        this.status = status;
    }

    /**
     * This method sets the human readable phrase that is equivalent to the status.
     * @param reasonPhrase the reason phrase of the http response.
     */

    private void setReasonPhrase(String reasonPhrase) {
        this.reasonPhrase = reasonPhrase;
    }

    /**
     * This method determines and returns the type of file being requested.
     * @param fileName the file being requested.
     * @return the messages used in the http response head.
     */

    private String findContentType(String fileName) {
        String content = "";
        if (fileName.endsWith(".jpg"))
            content = "Content-Type:image/jpeg\r\n";
        if (fileName.endsWith(".gif"))
            content = "Content-Type:image/gif\r\n";
        if (fileName.endsWith(".html"))
            content = "Content-Type:text/html\r\n";
        if (fileName.endsWith(".css"))
            content = "Content-Type:text/css\r\n";
        if (fileName.endsWith(".ico"))
            content = "Content-Type:image/x-icon\r\n";
        return content;
    }

    /**
     * This method sets the content type of the http response object.
     * @param contentType
     */

    private void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * This method sets the file of the httpResponse object.
     * @param file the file used by the response
     */

    private void setFile(File file) {
        this.file = file;
    }

    /**
     * This method returns the location of the host that sent a request.
     * @return - the location of the host
     */

    private String getLocation() {
        return location;
    }

}
