import java.io.*;
import java.util.Arrays;
import java.util.Date;

public class HttpResponse {

    private String status;
    private String httpVersion;
    private String reasonPhrase;
    private String contentType;
    private int numOfBytes;
    private File file;



    private byte[] fileInBytes;

    public HttpResponse(String httpVersion) {
        this.httpVersion = httpVersion;

    }

    public void setupResponse() throws IOException {
        this.numOfBytes = (int)this.file.length();
        FileInputStream fileInputStream = new FileInputStream(file.getPath());
        this.fileInBytes = new byte[numOfBytes];
        fileInputStream.read(fileInBytes);

    }

    public void respond(DataOutputStream outToClient, String date) throws IOException {
        outToClient.writeBytes("HTTP/1.0 " + this.status + " Not Found\r\n");
        outToClient.writeBytes(date);
        outToClient.writeBytes("Content-Length: + " + this.numOfBytes + "\r\n");
        outToClient.writeBytes("\r\n");
        outToClient.write(this.fileInBytes, 0, this.numOfBytes);
    }


    public String getContentType(File file) {
        String content = "";
        if (file.getName().endsWith(".jpg")) {
            content =  "Content-Type:image/jpeg\r\n";
        } else if (file.getName().endsWith(".gif")) {
            content = "Content-Type:image/gif\r\n";
        } else if (file.getName().endsWith(".html")) {
            content = "Content-Type:text/html\r\n";
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
