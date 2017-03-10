package com.testpoc.distance;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private AppCompatAutoCompleteTextView source, destination;
    private PlacesAutoCompleteAdapter Padapter;
    private Toolbar toolbar;
    private final int PERMISSION_ACCESS_COARSE_LOCATION = 200;
    private GoogleApiClient googleApiClient;
    private long UPDATE_INTERVAL = 10 * 1000;
    private long FASTEST_INTERVAL = 5000;
    private LocationRequest mLocationRequest;
    private SupportMapFragment mapFragment;
    private GoogleMap googleMap;
    private ArrayList<LatLng> list = new ArrayList<>();
    private boolean isInitial = true;
    private Polyline polyline;
    private Marker m1, m2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        toolbar = (Toolbar) findViewById(R.id.toolbar_family_details);
        source = (AppCompatAutoCompleteTextView) findViewById(R.id.source);
        destination = (AppCompatAutoCompleteTextView) findViewById(R.id.dest);

        source.setThreshold(3);
        destination.setThreshold(3);

        setSupportActionBar(toolbar);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);

        findViewById(R.id.linear_footer_container).setVisibility(View.GONE);
        Padapter = new PlacesAutoCompleteAdapter(this, android.R.layout.simple_list_item_1);
        source.setAdapter(Padapter);
        destination.setAdapter(Padapter);

        destination.setText(getIntent().getStringExtra("DeliveryPlace"));
        destination.setFocusable(false);
        destination.setClickable(false);

        source.setFocusable(false);
        source.setClickable(false);

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        findViewById(R.id.floating_btn_direction).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(source.getText()) && !TextUtils.isEmpty(destination.getText())) {
                    try {
                        final String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + URLEncoder.encode(source.getText().toString(), "utf8") + "&destination=" + URLEncoder.encode(destination.getText().toString(), "utf8") + "&sensor=false";

                        new AsyncTask<String, Void, JSONObject>() {
                            @Override
                            protected JSONObject doInBackground(String... strings) {
                                return WebServiceHelper.runService(url, null, WebServiceHelper.RequestType.GET);

                            }

                            @Override
                            protected void onPostExecute(JSONObject jsonObject) {
                                super.onPostExecute(jsonObject);
                                try {

                                    JSONArray routeArray = jsonObject.getJSONArray("routes");
                                    JSONObject routes = routeArray.getJSONObject(0);

                                    JSONObject overviewPolylines = routes
                                            .getJSONObject("overview_polyline");
                                    String encodedString = overviewPolylines.getString("points");
                                    list = decodePoly(encodedString);

                                    JSONArray legsJsonArray = routeArray.getJSONObject(0).getJSONArray("legs");
                                    // Extract the Place descriptions from the results


                                    findViewById(R.id.linear_footer_container).setVisibility(View.VISIBLE);
                                    // store Killometer in Distance
                                    JSONObject path = legsJsonArray.getJSONObject(0);

                                    JSONObject location = path.getJSONObject("start_location");
                                    JSONObject endLocation = path.getJSONObject("end_location");
                                    LatLng current = new LatLng(Double.parseDouble(location.getString("lat")), Double.parseDouble(location.getString("lng")));
                                    LatLng end = new LatLng(Double.parseDouble(endLocation.getString("lat")), Double.parseDouble(endLocation.getString("lng")));
                                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(current, 6);
                                    googleMap.animateCamera(cameraUpdate);
                                    isInitial = false;

                                    JSONObject distance = path.getJSONObject("distance");
                                    JSONObject duration = path.getJSONObject("duration");

                                    ((TextView) findViewById(R.id.txt_distance)).setText(distance.getString("text"));
                                    ((TextView) findViewById(R.id.txt_durarion)).setText(duration.getString("text"));

                                    // Polylines are useful for marking paths and routes on the map.

                                    if (m1 != null) {
                                        m1.remove();
                                    }

                                    if (m2 != null) {
                                        m2.remove();
                                    }

                                    if (polyline != null) {
                                        polyline.remove();
                                    }

                                    m1 = googleMap.addMarker(new MarkerOptions()
                                            .position(current)
                                            .title("Your Location")
                                            .draggable(true).visible(true));

                                    m2 = googleMap.addMarker(new MarkerOptions()
                                            .position(end)
                                            .title("Delivery Location")
                                            .draggable(true).visible(true));

                                    polyline = googleMap.addPolyline(new PolylineOptions().geodesic(true).color(Color.BLUE)
                                            .addAll(list));

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }.execute();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private boolean checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            return true;
        else
            return false;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Connect the client.
        googleApiClient.connect();
    }

    @Override
    public void onStop() {
        // Disconnecting the client invalidates it.
        if (googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }
        super.onStop();
    }

    public void onPause() {
        if (googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }
        super.onPause();
    }


    @Override
    public void onLocationChanged(Location location) {
        if (isInitial) {
            LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(current, 6);
            googleMap.animateCamera(cameraUpdate);

            isInitial = false;

            if (checkLocationPermission()) {
                Location location1 = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

                try {
                    Geocoder geo = new Geocoder(MainActivity.this, Locale.getDefault());
                    List<android.location.Address> addresses = geo.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (addresses.isEmpty()) {

                    } else {
                        if (addresses.size() > 0) {
                            source.setText(addresses.get(0).getFeatureName() + ", " + addresses.get(0).getLocality() + ", " + addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryName());
                            //Toast.makeText(getApplicationContext(), "Address:- " + addresses.get(0).getFeatureName() + addresses.get(0).getAdminArea() + addresses.get(0).getLocality(), Toast.LENGTH_LONG).show();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace(); // getFromLocation() may sometimes fail
                }

                findViewById(R.id.floating_btn_direction).performClick();

            }
        }

    }


    private void turnOnGPSDevice() {

        if (checkLocationPermission()) {

            mLocationRequest = LocationRequest.create();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setInterval(UPDATE_INTERVAL);
            mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(mLocationRequest);

            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,
                    mLocationRequest, MainActivity.this);

            PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi
                    .checkLocationSettings(googleApiClient, builder.build());

            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    MainActivity.this.googleMap = googleMap;

                }
            });

            Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

            try {
                Geocoder geo = new Geocoder(MainActivity.this, Locale.getDefault());
                List<android.location.Address> addresses = geo.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                if (addresses.isEmpty()) {

                } else {
                    if (addresses.size() > 0) {
                        source.setText(addresses.get(0).getFeatureName() + ", " + addresses.get(0).getLocality() + ", " + addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryName());
                        //Toast.makeText(getApplicationContext(), "Address:- " + addresses.get(0).getFeatureName() + addresses.get(0).getAdminArea() + addresses.get(0).getLocality(), Toast.LENGTH_LONG).show();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace(); // getFromLocation() may sometimes fail
            }

            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    final LocationSettingsStates state = result.getLocationSettingsStates();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            // All location settings are satisfied. The client can
                            // initialize location
                            // requests here.
                            googleApiClient.connect();
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be
                            // fixed by showing the user
                            // a dialog.
                            try {
                                // Show the dialog by calling
                                // startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(MainActivity.this, 1000);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have
                            // no way to fix the
                            // settings so we won't show the dialog.
                            break;
                    }
                }
            });
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_ACCESS_COARSE_LOCATION);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        turnOnGPSDevice();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private ArrayList<LatLng> decodePoly(String encoded) {

        ArrayList<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            googleApiClient.connect();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("TAG", "onActivityResult() called with: " + "requestCode = [" + requestCode + "], resultCode = [" + resultCode + "], data = [" + data + "]");
        switch (requestCode) {

            case 1000:
                googleApiClient.connect();
                break;

        }
    }
}
