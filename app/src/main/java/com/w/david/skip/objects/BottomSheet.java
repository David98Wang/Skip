package com.w.david.skip.objects;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.w.david.skip.R;
import com.w.david.skip.activities.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by David on 2/28/2018.
 */

public class BottomSheet extends LinearLayout {
    public static final String LOGTAG = "BottomSheet";
    /**
     * These constants match the ones from BottomSheetBehavior
     */
    public static final int STATE_DRAGGING = 1;
    public static final int STATE_SETTLING = 2;
    public static final int STATE_EXPANDED = 3;
    public static final int STATE_COLLAPSED = 4;
    public static final int STATE_HIDDEN = 5;

    BottomSheetBehavior mBehavior;
    Resort mResort;
    Marker mMarker;
    GoogleMap mMap;
    AppCompatActivity mParentActivity;
    int mDistance = -1;
    String mDuration = "-1min";

    public BottomSheet(Context context) {
        super(context);
    }

    public BottomSheet(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BottomSheet(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public BottomSheet(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setBehavior(BottomSheetBehavior bottomSheetBehavior) {
        mBehavior = bottomSheetBehavior;;
    }
    public void setMap(GoogleMap map)
    {
        mMap = map;
    }
    public BottomSheetBehavior getBehavior()
    {
        return mBehavior;
    }
    public void collapse() {


        View v1 = this.findViewById(R.id.resort_detail_name);
        View v2 = this.findViewById(R.id.resort_detail_summary_layout);
        final int height = v1.getHeight()+v2.getHeight();
        mBehavior.setPeekHeight(v1.getHeight()+v2.getHeight());
        mBehavior.setState(STATE_COLLAPSED);
        if(mMap!=null)
        {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    mMap.setPadding(0,0,0,height);
                }
            });
        }
    }
    public void hide()
    {
        mBehavior.setState(STATE_HIDDEN);
    }
    private void updateInformation() {
        TextView tvName = findViewById(R.id.resort_detail_name);
        tvName.setText(mResort.getName());

        TextView tvDifficultyNumber = findViewById(R.id.resort_detail_difficulty_number);
        tvDifficultyNumber.setText(String.valueOf(mResort.difficulty));

    }

    public void onMarkerClick(Marker marker) {
        mResort = MainActivity.markerResortMap.get(marker);
        mMarker = marker;
        new GetDistanceMatrix().execute();
        new GetWeather().execute();
        updateInformation();
        collapse();
    }

    public void setParentActivity(AppCompatActivity activity) {
        mParentActivity = activity;
    }


    @SuppressLint("MissingPermission")
    private String getMyLocationLatLng() {
        LocationManager locationManager = (LocationManager)
                mParentActivity.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        Location location = locationManager.getLastKnownLocation(locationManager
                .getBestProvider(criteria, false));
        return (String.valueOf(location.getLatitude()) + "," + String.valueOf(location.getLongitude()));
    }

    class GetDistanceMatrix extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            TextView tvDuration = findViewById(R.id.resort_detail_duration);
            tvDuration.setText("Loading...");
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            String baseEndPointUrl = "https://maps.googleapis.com/maps/api/distancematrix/json?key=" + mParentActivity.getString(R.string.google_api_key);
            baseEndPointUrl += "&destinations=" + mResort.getAddress().getLocationString();

            //If not location permission is granted
            if (ActivityCompat.checkSelfPermission(mParentActivity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mParentActivity, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                mDistance = -1;
                mDuration = "-1min";
                return false;
            }
            baseEndPointUrl += "&origins=" + getMyLocationLatLng();
            try {
                URL url = new URL(baseEndPointUrl);
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                Log.d(LOGTAG,url.toString());
                if (connection.getResponseCode() == 200) {
                    Log.d(LOGTAG,"DistanceMatrix API connection success");
                    InputStream responseBody = connection.getInputStream();
                    Scanner sc = new Scanner(responseBody).useDelimiter("\\A");
                    String s = sc.next();//This is the JSON string of whatever Google Map API returned
                    Log.d(LOGTAG,s);
                    JSONObject root = new JSONObject(s);
                    JSONObject row = root.getJSONArray("rows").getJSONObject(0);
                    JSONObject element = row.getJSONArray("elements").getJSONObject(0);
                    double distance = element.getJSONObject("distance").getInt("value");
                    mDuration = root.getJSONArray("rows").getJSONObject(0).getJSONArray("elements").getJSONObject(0).getJSONObject("duration").getString("text");
                    mDistance = (int) (distance / 1000.0 + 0.5);
                } else {
                    throw new IOException("Did not get a correct response from google API");
                }
            } catch (java.net.MalformedURLException e) {
                Log.e(LOGTAG, "Invalid URL for distance API call");
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(mParentActivity.getApplicationContext(), "Network error. Check internet connection", Toast.LENGTH_LONG);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            TextView tvDuration = findViewById(R.id.resort_detail_duration);
            tvDuration.setText(String.valueOf(mDuration));
        }
    }
    class GetWeather extends AsyncTask<Void, Void, Boolean> {
        double min,ave,max;
        TextView tvMin;
        TextView tvAve;
        TextView tvMax;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            tvMin = findViewById(R.id.resort_detail_weather_min);
            tvAve = findViewById(R.id.resort_detail_weather_ave);
            tvMax = findViewById(R.id.resort_detail_weather_max);
            tvAve.setText("Loading...");
            tvMax.setText("");
            tvMax.setText("");
        }
        double lat,lng;
        @Override
        protected Boolean doInBackground(Void... voids) {
            String baseEndPointUrl = "https://api.openweathermap.org/data/2.5/forecast?APPID=" + mParentActivity.getString(R.string.open_weather_map_api_key);
            lat = mResort.latitude;
            lng = mResort.longtitude;

            baseEndPointUrl += "&lat=" + String.valueOf(lat);
            baseEndPointUrl += "&lon=" + String.valueOf(lng);
            Log.d(LOGTAG, "doInBackground: "+ baseEndPointUrl);
            try {
                URL url = new URL(baseEndPointUrl);
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                Log.d(LOGTAG,url.toString());
                if (connection.getResponseCode() == 200) {
                    Log.d(LOGTAG,"DistanceMatrix API connection success");
                    InputStream responseBody = connection.getInputStream();
                    Scanner sc = new Scanner(responseBody).useDelimiter("\\A");
                    String s = sc.next();//This is the JSON string of whatever Google Map API returned
                    Log.d(LOGTAG,s);
                    JSONObject root = new JSONObject(s);

                    min = root.getJSONArray("list").getJSONObject(0).getJSONObject("main").getDouble("temp_min");
                    ave = root.getJSONArray("list").getJSONObject(0).getJSONObject("main").getDouble("temp");
                    max = root.getJSONArray("list").getJSONObject(0).getJSONObject("main").getDouble("temp_max");
                } else {
                    throw new IOException("Did not get a correct response from google API");
                }
            } catch (java.net.MalformedURLException e) {
                Log.e(LOGTAG, "Invalid URL for distance API call");
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(mParentActivity.getApplicationContext(), "Network error. Check internet connection", Toast.LENGTH_LONG);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            tvMin = findViewById(R.id.resort_detail_weather_min);
            tvAve = findViewById(R.id.resort_detail_weather_ave);
            tvMax = findViewById(R.id.resort_detail_weather_max);
            min -= 273.15+0.5;
            ave -= 273.15+0.5;
            max -= 273.15+0.5;
            String s = String.valueOf((int)min)+" "+ String.valueOf((int)ave)+" "+String.valueOf((int)max);
            tvMin.setText(String.valueOf((int)min));
            tvAve.setText(String.valueOf((int)ave));
            tvMax.setText(String.valueOf((int)max));
        }
    }
}
