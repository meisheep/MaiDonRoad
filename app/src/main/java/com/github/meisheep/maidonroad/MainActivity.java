package com.github.meisheep.maidonroad;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final int MY_PERMISSION_ACCESS_COARSE_LOCATION = 11;
    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 12;

    private List<Location> locations = new ArrayList<>();
    private int badgeNum = 0;
    private double lastLat = 25.0479019;
    private double lastLng = 121.5165127;

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private android.location.Location mLastLocation;
    private TextView badgeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.toolbar_title);

        ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);

        if(mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        new FetchTask().execute();
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  }, MY_PERMISSION_ACCESS_COARSE_LOCATION);
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            lastLat = mLastLocation.getLatitude();
            lastLng = mLastLocation.getLongitude();
        }

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLat, lastLng), 14.0f));
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem badge = menu.findItem(R.id.action_badge);
        MenuItemCompat.setActionView(badge, R.layout.badge_main);
        badgeText = (TextView) MenuItemCompat.getActionView(badge);
        badgeText.setText(String.valueOf(badgeNum));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_details) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class FetchTask extends AsyncTask<Void, Void, String> {
        Snackbar snackbar;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if(snackbar == null) {
                snackbar = Snackbar
                        .make(getWindow().getDecorView().findViewById(R.id.content_main), R.string.prompt_fetching, Snackbar.LENGTH_LONG);
                snackbar.show();
            }

        }

        @Override
        protected String doInBackground(Void... params) {
            String rawJson = "{}";
            try {
                URL requestUrl = new URL("http://data.taipei/opendata/datalist/apiAccess?scope=resourceAquire&rid=201d8ae8-dffc-4d17-ae1f-e58d8a95b162");
                URLConnection conn = requestUrl.openConnection();
                BufferedReader buffer = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                int cp;
                while((cp = buffer.read()) != -1) {
                    sb.append((char) cp);
                }

                rawJson = sb.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                JSONObject json = new JSONObject(rawJson).getJSONObject("result");
                badgeNum = json.getInt("count");
                JSONArray results = json.getJSONArray("results");

                for(int i = 0; i < results.length(); i++) {
                    JSONObject item = results.getJSONObject(i);
                    locations.add(Location.calcTWD97toWSG84(item.getDouble("X"), item.getDouble("Y")));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            badgeText.setText(String.valueOf(badgeNum));

            MarkerOptions markerOpt = new MarkerOptions();
            for(Location l : locations) {
                mMap.addMarker(new MarkerOptions().position(new LatLng(l.getLat(), l.getLng())));
            }

            if(snackbar != null) {
                snackbar.dismiss();
            }
        }
    }
}
