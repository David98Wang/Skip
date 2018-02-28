package com.w.david.skip;

import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by whcda on 2/28/2018.
 */

public class GoogleMapListener implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        GoogleMap.OnMapClickListener{
    GoogleMap mMap;
    Marker currentOnClickMarker;
    AppCompatActivity parentActivity;
    public GoogleMapListener(AppCompatActivity parentActivity){
        this.parentActivity = parentActivity;
    }
    public GoogleMap getMap()
    {
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
        Integer clickCount = (Integer) marker.getTag();

        // Check if a click count was set, then display the click count.
        if (clickCount != null) {
            //Intent intent = new Intent(this, ResortDetailActivity.class);
            //intent.putExtra("MyResort", mResorts.get(0));
            //startActivity(intent);
            clickCount = clickCount + 1;
            marker.setTag(clickCount);
            setMarkerAsOnClick(marker);
            if (currentOnClickMarker != null) {
                setMarkerAsDefault(currentOnClickMarker);
            }
            currentOnClickMarker = marker;
        }

        return false;
    }
    private void setMarkerAsOnClick(Marker marker) {
        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.on_click_resort_location_icon));
    }

    private void setMarkerAsDefault(Marker marker) {
        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable
                .default_resort_location_icon));
    }
}
