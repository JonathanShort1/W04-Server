import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * This class connects to http://ip-api.com/docs/api:csv and retrieves data about an IP.
 * Specifically the location of the host.
 */

public class GeoApiResult {

    private static final String GEO_API_URL = "http://ip-api.com/csv/";

    private String url;
    private BufferedReader reader;

    /**]
     * This constructor takes an ip address and finds the location of the address.
     * @param ip - the ip used to find location for the host
     */

    GeoApiResult(String ip){
        this.url = GEO_API_URL + ip;
    }

    /**
     * This method makes the connection th the url, builds the bufferedReader and returns the location of a host.
     * @return the location of the host.
     */

    public String getLocation() {
        String location = "";
        String input;
        try {
            URL geoApiUrl = new URL(this.url);
            URLConnection connection = geoApiUrl.openConnection();
            connection.connect();
            this.reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            input = reader.readLine();
            System.out.println(input);
            String result[] = input.split(",");
            location = result[5];
            System.out.println(location);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return location;
    }
}
