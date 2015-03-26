package com.mhacks.wakyowl.wakyowl;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.X509Certificate;


public class SearchActivity extends Activity {
    private static final String APIKEY = "AIzaSyAdNPvQQsNLHgT4Vwv-QxVLF16_bI1iO-U";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        GPSTracker gpsTracker = new GPSTracker(getApplicationContext());
        gpsTracker.getLocation();
        double lat = gpsTracker.getLatitude();
        double lon = gpsTracker.getLongitude();
        Utilities.currentLat = lat;
        Utilities.currentLon = lon;
        EditText editText = (EditText) findViewById(R.id.fromEditTextId);
        editText.setText("Longitude is:" + lon + " Latitidue is:" + lat);
    }

    public void search(View v) throws IOException, JSONException {
//        Thread thread = new Thread(new Runnable(){
//            @Override
//            public void run() {

//            }
//        });
//
//        thread.start();
        new MyTask().execute();

    }




    //https://maps.googleapis.com/maps/api/geocode/json?address=1600+Amphitheatre+Parkway,+Mountain+View,+CA&key=API_KEY
    public String makeJSONQuery(StringBuilder urlBuilder) throws IOException {
        HttpsURLConnection client = null;
        try {
            HostnameVerifier hostnameVerifier = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    HostnameVerifier hv =
                            HttpsURLConnection.getDefaultHostnameVerifier();
                    return hv.verify("example.com", session);
                }
            };

//            disableSslVerification();
            URL url = new URL("https://" + urlBuilder.toString());
            client = (HttpsURLConnection) url.openConnection();
//            client.setHostnameVerifier(hostnameVerifier);
            client.setRequestProperty("accept", "application/json");
            client.setConnectTimeout(30000);
            client.setReadTimeout(30000);

            client.connect();
            BufferedReader br;
            InputStream err = client.getErrorStream();
            if( err != null )
                br = new BufferedReader(new InputStreamReader(err));
            else {
                InputStream in = client.getInputStream();
                br = new BufferedReader(new InputStreamReader(in));
            }
            String returnString = "";
            String line;
            while((line=br.readLine())!=null)
            {
                returnString += line;
            }
            return returnString;
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            return "ERROR";
        } finally {
            if(client != null)
                client.disconnect();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class MyTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                EditText editText = (EditText) findViewById(R.id.toEditTextId);
                String formattedDestination = URLEncoder.encode(editText.getText().toString(), "utf-8");

                String queryUri = "maps.googleapis.com/maps/api/geocode/json?address="
                        +formattedDestination
                        +"&key="
                        +APIKEY;
                StringBuilder uriBuilder = new StringBuilder(queryUri);
                String result = makeJSONQuery(uriBuilder);

                JSONObject root = new JSONObject(result);
                JSONArray resultArray = root.getJSONArray("results");
                JSONObject resultObject = resultArray.getJSONObject(0);
                String address = resultObject.getString("formatted_address");
                Utilities.address = address;
                System.out.println(address);
                JSONObject geometryObject = resultObject.getJSONObject("geometry");
                JSONObject locationObject = geometryObject.getJSONObject("location");
                double lat = locationObject.getDouble("lat");
                double lon = locationObject.getDouble("lng");
                Utilities.destLat = lat;
                Utilities.destLon = lon;

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            Intent myIntent = new Intent(SearchActivity.this, MainActivity.class);

            SearchActivity.this.startActivity(myIntent);


        }
    }
}

