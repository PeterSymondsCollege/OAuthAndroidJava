package uk.ac.psc.oauthandroidjava;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * Created by sking on 05/08/2015.
 */
public class ApiHandler {
    String accessToken;

    // This is where the data is stored
    static final String API_URL = "https://data.psc.ac.uk/api/";

    public ApiHandler(String accessToken) {
        this.accessToken = accessToken;
    }

    // Allow API calls without any parameters
    public JSONObject call(String method) throws IOException {
        return call(method,null);
    }

    public JSONObject call(String method, Map<String,String> parameters)
            throws IOException {
        // Convert the parameter map into a URL query string
        StringBuilder sb = new StringBuilder();
        if (parameters != null) {
            boolean first = true;
            for (Map.Entry<String,String> e : parameters.entrySet()) {
                if (first) {
                    sb.append("?");
                    first = false;
                } else {
                    sb.append("&");
                }
                sb.append(e.getKey()+"="+e.getValue());
            }
        }
        String query = sb.toString();

        // Send the request to the API
        URL address = new URL(API_URL + method + query);
        HttpURLConnection conn = (HttpURLConnection) address.openConnection();
        // Send the access token in the header
        conn.setRequestProperty("Authorization","Bearer " + accessToken);
        conn.connect();

        // Read the data from the API
        BufferedReader br =
                new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
        JSONObject data = new JSONObject();
        try {
            data = new JSONObject(br.readLine());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        br.close();
        conn.disconnect();

        return data;
    }
}
