package com.w.david.skip;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.w.david.skip.objects.Address;
import com.w.david.skip.objects.Resort;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
GoogleMap.OnMapClickListener{
    private static final String LOGTAG = "MainActivity";
    private static final LatLng PERTH = new LatLng(-31.952854, 115.857342);
    private static final LatLng SYDNEY = new LatLng(-33.87365, 151.20689);
    private static final LatLng BRISBANE = new LatLng(-27.47093, 153.0235);
    ArrayList<String> resortName = new ArrayList<>();
    ArrayList<Resort> mResorts = new ArrayList<>();
    ArrayList<LatLng> mLatLngs = new ArrayList<>();
    Marker currentOnClickMarker = null;
    SupportMapFragment mapFragment;
    private GoogleMap mMap;
    private Marker mPerth;
    private Marker mSydney;
    private Marker mBrisbane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        getResortData();


    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Integer clickCount = (Integer) marker.getTag();

        // Check if a click count was set, then display the click count.
        if (clickCount != null) {
            //Intent intent = new Intent(this, ResortDetailActivity.class);
            //intent.putExtra("MyResort", mResorts.get(0));
            //startActivity(intent);
            clickCount = clickCount + 1;
            marker.setTag(clickCount);
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            if(currentOnClickMarker!=null)
            {
                currentOnClickMarker.setIcon(BitmapDescriptorFactory.defaultMarker
                        (BitmapDescriptorFactory.HUE_RED));
            }
            currentOnClickMarker = marker;
        }

        return false;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.setOnMarkerClickListener(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},1
                    );
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        mMap.setOnMapClickListener(this);
    }

    @Override
    public boolean onMyLocationButtonClick() {

        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {

    }

    @Override
    public void onMapClick(LatLng latLng) {
        currentOnClickMarker.setIcon(BitmapDescriptorFactory.defaultMarker
                (BitmapDescriptorFactory.HUE_RED));
        currentOnClickMarker=null;
    }


    class GetLatLngs extends AsyncTask<Void,Void,Boolean>{

        @Override
        protected Boolean doInBackground(Void... voids) {
            String baseGeocodingEndPoint = "https://maps.googleapis.com/maps/api/geocode/json?key=" + getString(R.string.google_api_key) + "&";
            Log.d(LOGTAG, "Base String: " + baseGeocodingEndPoint);
            try {
                for (Resort resort : mResorts) {
                    String currentGeocodingEndPoint = baseGeocodingEndPoint + "address=" + resort.getAddress().getLocationString();
                    URL currentUrl = new URL(currentGeocodingEndPoint);
                    HttpsURLConnection connection = (HttpsURLConnection) currentUrl.openConnection();
                    if (connection.getResponseCode() == 200) {
                        Log.d(LOGTAG, "Google Map API connection successful");
                        InputStream responseBody = connection.getInputStream();
                        Scanner sc = new Scanner(responseBody).useDelimiter("\\A");
                        String s = sc.next();//This is the string of whatever Google Map API returned

                        JSONObject root = new JSONObject(s);
                        JSONArray results = root.getJSONArray("results");
                        double lat, lng;
                        lat = results.getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                        lng = results.getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getDouble("lng");
                        mLatLngs.add(new LatLng(lat, lng));
                        Log.d(LOGTAG, results.toString());
                        Log.d(LOGTAG, "LAT: " + String.valueOf(lat) + ", LNG: " + String.valueOf(lng));
                    } else {
                        Log.e(LOGTAG, "Google MAP API connection ERROR: " + String.valueOf(connection.getResponseCode()));
                    }
                }

            } catch (java.net.MalformedURLException e) {
                Log.e(LOGTAG, "Google Map API end point is not a correct URL. This is literally inpossible");
            } catch (java.io.IOException e) {
                Log.e(LOGTAG, "Unable to connect to Google Map API");
            } catch (org.json.JSONException e) {
                Log.e(LOGTAG, "Invalid JSON string from Google Map API");
            }
            return true;
        }
        @Override
        protected  void onPostExecute(Boolean success){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(LOGTAG, "LatLng size: " + String.valueOf(mLatLngs.size()));
                    for (int i = 0; i < mLatLngs.size(); i++) {
                        Marker tempMarker = mMap.addMarker(new MarkerOptions()
                                .position(mLatLngs.get(i))
                                .title(mResorts.get(i).getName()));
                        tempMarker.setTag(0);
                    }
                    mPerth = mMap.addMarker(new MarkerOptions()
                            .position(PERTH)
                            .title("Perth"));
                    mPerth.setTag(0);

                    mSydney = mMap.addMarker(new MarkerOptions()
                            .position(SYDNEY)
                            .title("Sydney"));
                    mSydney.setTag(0);

                    mBrisbane = mMap.addMarker(new MarkerOptions()
                            .position(BRISBANE)
                            .title("Brisbane"));
                    mBrisbane.setTag(0);
                }
            });

        }
    }
    private void getResortData() {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference resortsReference = database.getReference("Resorts");
        Log.d(LOGTAG, resortsReference.getKey());
        resortsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(LOGTAG, "Successfully downloaded resort data");
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Log.d(LOGTAG, "Current Resort: " + snapshot.getKey());
                    String name = snapshot.getKey();
                    DataSnapshot dsAddress = snapshot.child(name).child("Address");
                    Address address = dsAddress.getValue(Address.class);
                    Resort resort = snapshot.getValue(Resort.class);
                    //resort.setAddress(address);
                    Log.d(LOGTAG, resort.getAddress().getLocationString());
                    mResorts.add(resort);
                    resortName.add(name);
                }
                Log.d(LOGTAG, Integer.toString(resortName.size()));
                for (int i = 0; i < resortName.size(); i++) {
                    Log.d(LOGTAG, resortName.get(i));
                }
                convertToLatLong();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return;
    }
    private void convertToLatLong() {
        new GetLatLngs().execute();
    }
}
