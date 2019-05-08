package com.codelabs.bloodbank;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    FloatingActionButton locationButton;
    List<MarkerList> markerListList;
    LocationManager locationManager;
    double latitude, longitude;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    Location location;
    Marker mCurrLocationMarker;
    private GoogleMap mMap;
    private ProgressDialog dialog;
    private int PROXIMITY_RADIUS = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        boolean isPermissionGranted = checkForPermission();

        locationButton = (FloatingActionButton) findViewById(R.id.location);
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation();

                String Hospital = "hospital";
                String url = getUrl(latitude, longitude, Hospital);
                Object[] DataTransfer = new Object[2];
                DataTransfer[0] = mMap;
                DataTransfer[1] = url;
                Log.d("onClick", url);
                GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
                Toast.makeText(MapsActivity.this, "Fetching hospitals", Toast.LENGTH_SHORT).show();
                getNearbyPlacesData.execute(DataTransfer);
            }
        });

        if (!isPermissionGranted) {
            locationButton.setEnabled(false);
        }
    }

    private boolean checkForPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private void getLocation() {

        locationManager = (LocationManager) MapsActivity.this.getSystemService(Context.LOCATION_SERVICE);
        boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        AlertDialog dialog = new AlertDialog.Builder(MapsActivity.this).
                setTitle("GPS Off").
                setMessage("Please enable gps.").
                setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setAction(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                }).create();

        if (enabled == false)
            dialog.show();


        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        }


        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location != null) {
            Log.e("TAG", "GPS is on");
            latitude = location.getLatitude();
            longitude = location.getLongitude();

            LatLng curPosition = new LatLng(latitude, longitude);
            mMap.addMarker(new MarkerOptions().position(curPosition).title("My Location").icon(
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(curPosition));
            Toast.makeText(MapsActivity.this, "Location: " + latitude + "," + longitude, Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(MapsActivity.this, "Location not found", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(4));

        getLocation();
        new SetMarkers().execute();

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final com.google.android.gms.maps.model.Marker marker) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        LatLng origin = new LatLng(location.getLatitude(), location.getLongitude());
                        LatLng dest = marker.getPosition();

                        String url = getUrl(origin, dest);
                        new FetchUrl().execute(url);
                    }
                });

                String name = marker.getTitle();
                for (MarkerList mark : markerListList) {
                    if (mark.getName().equals(name)) {
                        //open dialog to show data.
                        final Dialog dialog = new Dialog(MapsActivity.this);
                        dialog.setContentView(R.layout.dialog);

                        TextView nameText = (TextView) dialog.findViewById(R.id.nameTextView);
                        TextView numberText = (TextView) dialog.findViewById(R.id.numberTextView);
                        TextView emailText = (TextView) dialog.findViewById(R.id.emailTextView);
                        TextView addressText = (TextView) dialog.findViewById(R.id.addressTextView);

                        nameText.setText(mark.getName());
                        numberText.setText(mark.getNumber());
                        emailText.setText(mark.getMail());
                        addressText.setText(mark.getAddress());
                        dialog.show();
                    }
                }

                final Dialog dialog = new Dialog(MapsActivity.this);
                dialog.setContentView(R.layout.hospital_dialog);

                TextView nameText = (TextView) dialog.findViewById(R.id.nameHospTextView);
                TextView addressText = (TextView) dialog.findViewById(R.id.addressHospTextView);

                String[] tokens = name.split(":");
                nameText.setText(tokens[0]);
                addressText.setText(tokens[1]);
                dialog.show();

                return false;
            }
        });
    }

    private String getUrl(LatLng origin, LatLng dest) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String sensor = "sensor=false";
        String parameters = str_origin + "&" + str_dest + "&" + sensor;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        mCurrLocationMarker = mMap.addMarker(markerOptions);

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(5));
        Toast.makeText(MapsActivity.this, "Your Current Location", Toast.LENGTH_LONG).show();

        Log.d("onLocationChanged", String.format("latitude:%.3f longitude:%.3f", latitude, longitude));

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            Log.d("onLocationChanged", "Removing Location Updates");
        }
        Log.d("onLocationChanged", "Exit");
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(3000);
            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            Log.d("downloadUrl", data.toString());
            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
            Toast.makeText(MapsActivity.this, "Failed to fetcg donors:\\", Toast.LENGTH_SHORT).show();
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private String getUrl(double latitude, double longitude, String nearbyPlace) {

        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=" + latitude + "," + longitude);
        googlePlacesUrl.append("&radius=" + PROXIMITY_RADIUS);
        googlePlacesUrl.append("&type=" + nearbyPlace);
        googlePlacesUrl.append("&sensor=true");
        googlePlacesUrl.append("&key=" + "AIzaSyATuUiZUkEc_UgHuqsBJa1oqaODI-3mLs0");
        Log.d("getUrl", googlePlacesUrl.toString());
        return (googlePlacesUrl.toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_register:
                Intent intent = new Intent(MapsActivity.this, Register.class);
                startActivity(intent);
                return true;
            case R.id.menu_help:
                Intent help = new Intent(MapsActivity.this, Help.class);
                startActivity(help);
                return true;
            case R.id.menu_exit:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class SetMarkers extends AsyncTask<String, String, String> {
        String response;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(MapsActivity.this);
            dialog.setMessage("Fetching donors... Please wait.");
            dialog.setIndeterminate(false);
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            final String URL = "http://192.168.0.18/bank/v1/tasks";
            JSONParser parser = new JSONParser();

            JSONObject jsonObj = parser.getUsers(URL);
            if (jsonObj != null) {
                try {
                    response = jsonObj.getString("users");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            markerListList = new ArrayList<>();
            LatLng position;
            try {
                String[] data = response.split(";");
                for (int i = 0; i < data.length; i++) {
                    if (data[i] != null) {
                        MarkerList mark = new MarkerList();
                        Log.d("Response", data[i]);
                        String[] tokens = data[i].split(",");

                        mark.setName(tokens[0]);
                        mark.setAddress(getAddress(tokens[3], tokens[4]));
                        mark.setNumber(tokens[2]);
                        mark.setMail(tokens[1]);
                        position = new LatLng(Double.parseDouble(tokens[3]), Double.parseDouble(tokens[4]));

                        mMap.addMarker(new MarkerOptions().position(position).title(tokens[0]).icon(
                                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                        ));
                        markerListList.add(mark);
                    }
                }
            } catch (Exception e) {
                Toast.makeText(MapsActivity.this, "Failed to fetch donors :\\", Toast.LENGTH_SHORT).show();
            }

            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        }

        private String getAddress(String token, String token1) {
            List<Address> addressList = null;
            String addressString;

            Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
            try {
                if (Double.parseDouble(token) == 0 && Double.parseDouble(token1) == 0) {
                    Toast.makeText(MapsActivity.this, "Unable to get address!", Toast.LENGTH_SHORT).show();
                    Thread.sleep(Toast.LENGTH_SHORT);
                    return null;
                }
                addressList = geocoder.getFromLocation(latitude, longitude, 1);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

            if (addressList == null) {
                Toast.makeText(MapsActivity.this, "Unable to retrieve address", Toast.LENGTH_SHORT).show();
                return null;
            }
            Address address = addressList.get(0);
            addressString = address.getAddressLine(0);
            return addressString;
        }
    }

    private class FetchUrl extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String data = "";
            try {
                // Fetching the data from web service
                data = downloadUrl(params[0]);
                Log.d("Background Task data", data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            ParserTask parserTask = new ParserTask();
            parserTask.execute(s);
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask", jsonData[0].toString());
                DataParserRoute parser = new DataParserRoute();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);
                Log.d("ParserTask", "Executing routes");
                Log.d("ParserTask", routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask", e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            try {
                for (int i = 0; i < result.size(); i++) {
                    points = new ArrayList<>();
                    lineOptions = new PolylineOptions();

                    // Fetching i-th route
                    List<HashMap<String, String>> path = result.get(i);

                    // Fetching all the points in i-th route
                    for (int j = 0; j < path.size(); j++) {
                        HashMap<String, String> point = path.get(j);

                        double lat = Double.parseDouble(point.get("lat"));
                        double lng = Double.parseDouble(point.get("lng"));
                        LatLng position = new LatLng(lat, lng);

                        points.add(position);
                    }

                    // Adding all the points in the route to LineOptions
                    lineOptions.addAll(points);
                    lineOptions.width(10);
                    lineOptions.color(Color.RED);

                    Log.d("onPostExecute", "onPostExecute lineoptions decoded");

                }
            } catch (NumberFormatException | NullPointerException e) {
                e.printStackTrace();
            }

            // Drawing polyline in the Google Map for the i-th route
            if (lineOptions != null) {
                mMap.addPolyline(lineOptions);
            } else {
                Log.d("onPostExecute", "without Polylines drawn");
            }
        }
    }
}
