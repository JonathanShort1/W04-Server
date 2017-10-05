public class TCPTuple {

    private String hostAddress;
    private String localAddress;
    private int localPortNumber;
    private int hostPortNumber;

    public TCPTuple(String hostAddress, int hostPortNumber, String localAddress, int localPortNumber) {
        this.hostAddress = hostAddress;
        this.localAddress = localAddress;
        this.localPortNumber = localPortNumber;
        this.hostPortNumber = hostPortNumber;
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public String getLocalAddress() {
        return localAddress;
    }

    public int getLocalPortNumber() {
        return localPortNumber;
    }

    public int getHostPortNumber() {
        return hostPortNumber;
    }

}
