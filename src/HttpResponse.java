import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;

public class HttpResponse implements Runnable{

    private static final String HOME_PAGE = "index.html";
    private static final String ERROR_404 = "error404.html";
    private static final String ERROR_400 = "error400.html";
    private static final String HTTP_V = "HTTP/1.0";

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

    HttpResponse(String accessLog, String exceptionLog, String metaData, Socket socket) throws IOException {
        this.connectionSocket = socket;
        this.writerAccessLog = new PrintWriter(new FileWriter(accessLog, true));
        this.writerExceptionLog = new PrintWriter(new FileWriter(exceptionLog, true));
        this.writerMetaData = new PrintWriter(new FileWriter(metaData, true));
        this.date = new Date().toString();
    }

    public void start() {
        if (thread == null) {
            thread = new Thread(this, "Connection");
            thread.start();
        }
    }

    @Override
    public void run() {
        String requestMessageLine;
        String fileName;
        DataOutputStream outToClient;
        File file;

        try {
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(this.connectionSocket.getInputStream()));
            outToClient = new DataOutputStream(this.connectionSocket.getOutputStream());

            StringTokenizer tokenisedLine;
            try {
                requestMessageLine = inFromClient.readLine();
                if (requestMessageLine != null) {
                    tokenisedLine = new StringTokenizer(requestMessageLine);
                    if (tokenisedLine.hasMoreTokens() && tokenisedLine.nextToken().equals("GET")) {
                        fileName = tokenisedLine.nextToken();
                        if (fileName.startsWith("/") && fileName.length() > 1) {
                            fileName = fileName.substring(1);
                            fileName = fileName.split("\\?")[0];
                        } else if (fileName.equals("/")) {
                            fileName = HOME_PAGE;
                        }
                        try {
                            file = new File(fileName);
                            if (file.exists()) {
                                this.buildResponse(file, "200", "Ok");
                                this.respond(outToClient);
                            } else {
                                file = new File(ERROR_404);
                                this.buildResponse(file, "404", "Not Found");
                                this.respond(outToClient);
                            }
                        } catch (FileNotFoundException e) {
                            this.writerExceptionLog.println("File requested not found!" + " File name: " + fileName);
                            this.writerExceptionLog.flush();
                            e.printStackTrace();
                        }
                    } else {
                        file = new File(ERROR_400);
                        this.buildResponse(file, "400", "Bad Request");
                        this.respond(outToClient);
                    }
                } else {
                    file = new File(ERROR_400);
                    this.buildResponse(file, "400", "Bad Request");
                    this.respond(outToClient);
                }
            } catch(NullPointerException e) {
                requestMessageLine = "";
                this.writerExceptionLog.println("Null pointer at request message! - likely a problem with a missing file");
                this.writerExceptionLog.flush();
                e.printStackTrace();
            }
            logAccess(requestMessageLine);
            logMetaData();
            this.connectionSocket.close();
        } catch (IOException e) {
            this.writerExceptionLog.println("Failed to create Input and output readers from socket streams OR not found ERROR_400 file");
            this.writerExceptionLog.flush();
            e.printStackTrace();
        }
        this.writerAccessLog.close();
        this.writerExceptionLog.close();
        this.writerMetaData.close();
    }

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

    private void logAccess(String requestLine) throws IOException {
        this.writerAccessLog.println(this.connectionSocket.getInetAddress().getHostAddress() + " [" + this.date + "]" + " " + "\"" + requestLine + "\" " + this.status + " " + this.numOfBytes);
        this.writerAccessLog.flush();
    }

    public void logMetaData() {
        this.writerMetaData.println("Location: " + getGeoLocation() + "; " + tcpConnection());
    }

    public void respond(DataOutputStream outToClient) throws IOException {
        outToClient.writeBytes(HTTP_V + " " + this.status + " " + this.reasonPhrase + "\r\n");
        outToClient.writeBytes(this.date + "\r\n");
        outToClient.writeBytes("Content-Length: + " + this.numOfBytes + "\r\n");
        if (!(this.contentType == null)) {
            outToClient.writeBytes(this.contentType);
        }
        outToClient.writeBytes("\r\n");
        outToClient.write(this.fileInBytes, 0, this.numOfBytes);
        outToClient.writeBytes(tcpConnection());
    }

    public String tcpConnection(){
        String tcpConnection;
        tcpConnection = "Host address: " + this.connectionSocket.getInetAddress().getHostAddress() + ","
                + " Port: " + this.connectionSocket.getPort() + ";"
                + " Local address: " + this.connectionSocket.getLocalAddress() + ","
                + " Local port: " + this.connectionSocket.getLocalPort();
        return tcpConnection;
    }

    public String getGeoLocation() {
        String location;
        GeoApiResult geoApiResult = new GeoApiResult(this.connectionSocket.getInetAddress().getHostAddress());
        location = geoApiResult.getLocation();
        System.out.println(location);
        return location;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setReasonPhrase(String reasonPhrase) {
        this.reasonPhrase = reasonPhrase;
    }

    public String findContentType(String fileName) {
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

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setFile(File file) {
        this.file = file;
    }

}
