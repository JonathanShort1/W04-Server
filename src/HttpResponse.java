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

    private String status;
    private String httpVersion;
    private String reasonPhrase;
    private String contentType;
    private int numOfBytes;
    private File file;
    private Socket connectionSocket;
    private PrintWriter writer;

    private byte[] fileInBytes;

    HttpResponse(PrintWriter writer, Socket socket) {
        this.connectionSocket = socket;
        this.writer = writer;
    }

    @Override
    public void run() {
        String requestMessageLine;
        String fileName;
        DataOutputStream outToClient;

        try{
            String date = new Date().toString();
            File file;

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
                if (fileName.startsWith("/") && fileName.length() > 1) {
                    fileName = fileName.substring(1);
                } else if (fileName.equals("/")) {
                    fileName = HOME_PAGE;
                }

                try {
                    file = new File(fileName);
                    if (file.exists()) {
                        this.setFile(file);
                        this.setStatus("200");
                        this.setReasonPhrase("Ok");
                        this.respond(outToClient, date, connectionSocket);
                    } else {
                        file = new File(ERROR_404);
                        this.setFile(file);
                        this.setStatus("404");
                        this.setReasonPhrase("Not Found");
                        this.respond(outToClient, date,connectionSocket);
                    }
                } catch (FileNotFoundException e) {
                    System.out.println("File requested not found!");
                    e.printStackTrace();
                }
            } else {
                file = new File(ERROR_400);
                this.setFile(file);
                this.setStatus("400");
                this.setReasonPhrase("Bad Request");
                this.respond(outToClient, date,connectionSocket);
            }
            logAccess(writer, connectionSocket.getInetAddress().getHostAddress(), requestMessageLine, date, this.getStatus(), this.getNumOfBytes());
            connectionSocket.close();
        } catch (FileNotFoundException e) {
            System.out.println("File does not exist - favicon.ico not found");

            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void logAccess(PrintWriter writer, String host, String requestLine, String date, String status, int numBytes) throws IOException {
        writer.println(host + " [" + date + "]" + " " + "\"" + requestLine + "\" " + status + " " + numBytes);
        writer.flush();
    }

    public void start() {
        if (thread == null) {
            thread = new Thread(this, "Connection");
            thread.start();
        }
    }

    public void setupResponse() throws IOException {
        this.numOfBytes = (int)this.file.length();
        FileInputStream fileInputStream = new FileInputStream(file.getPath());
        this.fileInBytes = new byte[numOfBytes];
        fileInputStream.read(fileInBytes);

    }

    public void respond(DataOutputStream outToClient, String date, Socket connectionSocket) throws IOException {
        this.setupResponse();
        outToClient.writeBytes("HTTP/1.0 " + this.status + " " + this.reasonPhrase + "\r\n");
        outToClient.writeBytes(date + "\r\n");
        outToClient.writeBytes("Content-Length: + " + this.numOfBytes + "\r\n");
        if (!(this.getContentType() == null)) {
            outToClient.writeBytes(this.getContentType());
        }
        outToClient.writeBytes("\r\n");
        outToClient.write(this.fileInBytes, 0, this.numOfBytes);
        outToClient.writeBytes("Host address: " + connectionSocket.getInetAddress().getHostAddress() + " port: " + connectionSocket.getPort() +
                "\nLocal address: " + connectionSocket.getLocalAddress() + " local port: " + connectionSocket.getLocalPort() + "\n");

    }

    public String getContentType(File file) {
        String content = "";
        if (file.getName().endsWith(".jpg")) {
            content =  "Content-Type:image/jpeg\r\n";
        } else if (file.getName().endsWith(".gif")) {
            content = "Content-Type:image/gif\r\n";
        } else if (file.getName().endsWith(".html")) {
            content = "Content-Type:text/html\r\n";
        } else if (file.getName().endsWith(".css")) {
            content = "Content-Type:text/css\r\n";
        } else if (file.getName().endsWith(".ico")) {
            content = "Content-Type:image/x-icon\r\n";
        }
        return content;
    }

    public byte[] getFileInBytes() {
        return fileInBytes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public void setHttpVersion(String httpVersion) {
        this.httpVersion = httpVersion;
    }

    public String getReasonPhrase() {
        return this.reasonPhrase;
    }

    public void setReasonPhrase(String reasonPhrase) {
        this.reasonPhrase = reasonPhrase;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public int getNumOfBytes() {
        return numOfBytes;
    }

    public void setNumOfBytes(int numOfBytes) {
        this.numOfBytes = numOfBytes;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }


}
