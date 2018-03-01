package com.w.david.skip.listeners;

import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.w.david.skip.R;

/**
 * Created by whcda on 2/28/2018.
 */

public class MainActivityListener extends BottomSheetBehavior.BottomSheetCallback implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        GoogleMap.OnMapClickListener {
    GoogleMap mMap;
    Marker currentOnClickMarker;
    AppCompatActivity parentActivity;
    private BottomSheetBehavior mBottomSheetBehavior;
    private LinearLayout mLlBottomSheet;

    public MainActivityListener(AppCompatActivity parentActivity, BottomSheetBehavior bottomSheetBehavior,
                                LinearLayout llBottomSheet) {
        this.parentActivity = parentActivity;
        mBottomSheetBehavior = bottomSheetBehavior;
        mLlBottomSheet = llBottomSheet;
    }

    public GoogleMap getMap() {
        return mMap;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(parentActivity, R.raw.google_map_style));
        mMap.setOnMarkerClickListener(this);
        if (ActivityCompat.checkSelfPermission(parentActivity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(parentActivity, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(parentActivity,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1
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
        if (currentOnClickMarker == null) return;
        setMarkerAsDefault(currentOnClickMarker);
        currentOnClickMarker = null;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        setMarkerAsOnClick(marker);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        if (currentOnClickMarker != null) {
            setMarkerAsDefault(currentOnClickMarker);
        }
        currentOnClickMarker = marker;

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
        if (newState == BottomSheetBehavior.STATE_HIDDEN)
            setMarkerAsDefault(currentOnClickMarker);
    }

    @Override
    public void onSlide(@NonNull View bottomSheet, float slideOffset) {

    }
}