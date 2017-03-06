package com.testpoc.distance;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class PlaceApi extends AsyncTask<String, Void, ArrayList<String>> {

    private static final String LOG_TAG = "ExampleApp";
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";
    //AIzaSyAbbydKTQkig7tkHUhd5sqreULg2XgHj0k
    private static final String API_KEY = "AIzaSyDp_1bCD8aCkXLB1ueyYAVvch0RyhSUM7I";
    private MyPlaceApiAccessor myApi;
    private ArrayList<String> resultList;
    private HttpURLConnection conn;
    private StringBuilder jsonResults;

    public interface MyPlaceApiAccessor {
        public void getPlacesList(ArrayList<String> allPlaces);
    }

    public PlaceApi(PlacesAutoCompleteAdapter placesAutoCompleteAdapter) {
        myApi = (MyPlaceApiAccessor) placesAutoCompleteAdapter;
    }

    @Override
    protected void onPreExecute() {
        resultList = null;
        conn = null;
        jsonResults = new StringBuilder();

    }

    @Override
    protected ArrayList<String> doInBackground(String... inputs) {
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE
                    + TYPE_AUTOCOMPLETE + OUT_JSON);
            sb.append("?key=" + API_KEY);
            sb.append("&components=country:ind");
            sb.append("&input=" + URLEncoder.encode(inputs[0], "utf8"));

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error processing Places API URL", e);
            return resultList;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            // Extract the Place descriptions from the results
            resultList = new ArrayList<String>(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                resultList.add(predsJsonArray.getJSONObject(i).getString(
                        "description"));
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }

        return resultList;

    }

    @Override
    protected void onPostExecute(ArrayList<String> places) {
        myApi.getPlacesList(places);

    }

}