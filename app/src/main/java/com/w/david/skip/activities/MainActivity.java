package com.w.david.skip.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.LinearLayout;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.w.david.skip.listeners.MainActivityListener;
import com.w.david.skip.R;
import com.w.david.skip.objects.Address;
import com.w.david.skip.objects.BottomSheet;
import com.w.david.skip.objects.Resort;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    private static final String LOGTAG = "MainActivity";
    public static Map<Marker,Resort> markerResortMap=new HashMap<>();
    ArrayList<String> resortName = new ArrayList<>();
    ArrayList<Resort> mResorts = new ArrayList<>();
    ArrayList<LatLng> mLatLngs = new ArrayList<>();
    MainActivityListener mMainActivityListener;
    SupportMapFragment mapFragment;
    private GoogleMap mMap;
    private BottomSheetBehavior mBottomSheetBehavior;
    private BottomSheet mLlBottomSheet;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeBottomSheetBehavior();
        mMainActivityListener = new MainActivityListener(this,mBottomSheetBehavior,mLlBottomSheet);


        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(mMainActivityListener);
        getResortData();
    }

    private void initializeBottomSheetBehavior()
    {
        mLlBottomSheet =  findViewById(R.id.resort_detail_bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(mLlBottomSheet);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        mBottomSheetBehavior.setPeekHeight(340);
        mBottomSheetBehavior.setHideable(true);
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

    class GetLatLngs extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            String baseGeocodingEndPoint = "https://maps.googleapis.com/maps/api/geocode/json?key=" + getString(R.string.google_api_key) + "&";
            Log.d(LOGTAG, "Base String: " + baseGeocodingEndPoint);
            try {
                for (Resort resort : mResorts) {
                    String currentGeocodingEndPoint = baseGeocodingEndPoint + "address=" + resort.getAddress().getLocationString();

                    URL currentUrl = new URL(currentGeocodingEndPoint);
                    Log.d(LOGTAG,currentUrl.toString());
                    HttpsURLConnection connection = (HttpsURLConnection) currentUrl.openConnection();
                    if (connection.getResponseCode() == 200) {
                        Log.d(LOGTAG, "Google Map API connection successful");
                        InputStream responseBody = connection.getInputStream();
                        Scanner sc = new Scanner(responseBody).useDelimiter("\\A");
                        String s = sc.next();//This is the JSON string of whatever Google Map API returned

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
        protected void onPostExecute(Boolean success) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mMap = mMainActivityListener.getMap();
                    Log.d(LOGTAG, "LatLng size: " + String.valueOf(mLatLngs.size()));
                    for (int i = 0; i < mLatLngs.size(); i++) {
                        mResorts.get(i).latitude = mLatLngs.get(i).latitude;
                        mResorts.get(i).longtitude = mLatLngs.get(i).longitude;
                        Marker curMarker = mMap.addMarker(new MarkerOptions()
                                .position(mLatLngs.get(i))
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.default_resort_location_icon)));
                        MainActivity.markerResortMap.put(curMarker,mResorts.get(i));
                    }
                }
            });

        }
    }
}
