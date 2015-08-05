package uk.ac.psc.oauthandroidjava;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.graphics.Bitmap;
import android.view.View;
import android.webkit.WebViewClient;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONException;
import org.json.JSONObject;

public class OAuthActivity extends AppCompatActivity {

    // Replace this with your clientId and secret
    String clientId = "[YOUR-CLIENT-ID]";
    String secret = "[YOUR-SECRET]";

    String redirectUrl = "app://localhost";
    String authUrl = "https://data.psc.ac.uk/oauth/v2/auth";
    String tokenUrl = "https://data.psc.ac.uk/oauth/v2/token";
    String scope = "timetable";

    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        setContentView(webView);
        webView.getSettings().setJavaScriptEnabled(true);

        // Start the login process
        getAccessToken();
    }

    public void getAccessToken() {

        String url = authUrl
                + "?" + "client_id=" + clientId
                + "&" + "response_type=code"
                + "&" + "redirect_uri=" + redirectUrl
                + "&" + "scope=" + scope;

        webView.setVisibility(View.VISIBLE);
        webView.loadUrl(url);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (url.startsWith(redirectUrl)) {
                    if (url.contains("code=")) {
                        String accessCode = url.substring(url.indexOf("code=") + 5);
                        new exchangeCodeForToken().execute(accessCode);
                        webView.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });
    }

    private class exchangeCodeForToken
            extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... params) {
            String query = "client_id=" + clientId
                    + "&" + "client_secret=" + secret
                    + "&" + "grant_type=authorization_code"
                    + "&" + "redirect_uri=" + redirectUrl
                    + "&" + "code=" + params[0];
            try {
                // Set up the connection to the page
                URL tokenAddress = new URL(tokenUrl);
                HttpURLConnection connection =
                        (HttpURLConnection) tokenAddress.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                // Send the post data to the page
                DataOutputStream wr =
                        new DataOutputStream(connection.getOutputStream());
                wr.writeBytes(query);
                wr.flush();
                wr.close();
                connection.connect();

                // Read the page contents
                BufferedReader br =
                        new BufferedReader(
                                new InputStreamReader(connection.getInputStream()));
                // The contents should be just 1 line of JSON data
                JSONObject data = new JSONObject(br.readLine());
                br.close();
                connection.disconnect();
                return data;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject accessToken) {
            Intent data = new Intent();
            data.setData(Uri.parse(accessToken.toString()));
            setResult(RESULT_OK, data);
            finish();
        }
    }
}
