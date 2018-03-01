package com.w.david.skip.listeners;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.w.david.skip.R;
import com.w.david.skip.objects.BottomSheet;


/**
 * Created by whcda on 2/28/2018.
 */

public class MainActivityListener extends BottomSheetBehavior.BottomSheetCallback implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMapClickListener {

    public static final String LOGTAG = "MainActivityListener";
    GoogleMap mMap;
    Marker currentOnClickMarker;
    AppCompatActivity mParentActivity;

    private BottomSheet mBottomSheet;

    public MainActivityListener(AppCompatActivity parentActivity, BottomSheetBehavior bottomSheetBehavior,
                                BottomSheet bottomSheet) {
        this.mParentActivity = parentActivity;
        mBottomSheet = bottomSheet;
        bottomSheetBehavior.setBottomSheetCallback(this);
        mBottomSheet.setBehavior(bottomSheetBehavior);

        mBottomSheet.setParentActivity(parentActivity);

    }

    public GoogleMap getMap() {
        return mMap;

    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mBottomSheet.setMap(mMap);
        mMap.setPadding(0,0,0,50);
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(mParentActivity, R.raw.google_map_style));
        mMap.setOnMarkerClickListener(this);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        if (ActivityCompat.checkSelfPermission(mParentActivity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mParentActivity, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(mParentActivity,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1
            );
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setOnMapClickListener(this);


    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (currentOnClickMarker == null) return;
        setMarkerAsDefault(currentOnClickMarker);
        currentOnClickMarker = null;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        setMarkerAsOnClick(marker);
        mBottomSheet.onMarkerClick(marker);
        if (currentOnClickMarker != null) {
            setMarkerAsDefault(currentOnClickMarker);
        }
        currentOnClickMarker = marker;
        //mBottomSheet.getBehavior().setHideable(false);
        return false;
    }

    private void setMarkerAsOnClick(Marker marker) {
        if (marker == null) return;
        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.on_click_resort_location_icon));
    }

    private void setMarkerAsDefault(Marker marker) {
        if (marker == null) return;
        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable
                .default_resort_location_icon));
    }

    @Override
    public void onStateChanged(@NonNull View bottomSheet, int newState) {
        Log.d(LOGTAG, "onStateChanged: ");
        final int height;

        if (newState == BottomSheetBehavior.STATE_HIDDEN)
        {
            height = 0;
            setMarkerAsDefault(currentOnClickMarker);
        }
        else if(newState == BottomSheetBehavior.STATE_EXPANDED)
        {
            height = bottomSheet.getHeight();
        }
        else
        {
            View v1 = mParentActivity.findViewById(R.id.resort_detail_name);
            View v2 = mParentActivity.findViewById(R.id.resort_detail_summary_layout);
            height = v1.getHeight()+v2.getHeight();
        }

        if(mMap!=null)
        {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    mMap.setPadding(10,0,0,height);
                }
            });
        }
    }

    @Override
    public void onSlide(@NonNull View bottomSheet, float slideOffset) {

    }
}
