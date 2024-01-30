package com.ahmet.mapsforgeapp.gps;

import android.content.Context;
import android.util.Log;

import com.ahmet.mapsforgeapp.MapViewController;
import com.ahmet.mapsforgeapp.R;
import com.ahmet.mapsforgeapp.map.MapUtils;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.layer.overlay.Marker;

/**
 * Description of TabletGpsListener
 *
 * @author Ahmet TOPAK
 * @version 1.0
 * @since 1/30/2024
 */

public class TabletGpsListener extends LocationListener{
    private String TAG = "Tablet Gps Listener";
    Marker marker;
    public TabletGpsListener(Context context , MapViewController mapViewController){
        super(context , mapViewController);
        marker = MapUtils.createMarker(context.getDrawable(R.drawable.gps_nav));
    }
    @Override
    public void onLocationUpdate(LatLong latLong) {
        mapViewController.updateMarkerLatLong(marker, latLong);

        //TODO state change
        mapViewController.addMarker(marker);

        Log.d(TAG, "Device Lat Long Test Link: " + MapUtils.generateLatLongMapsLink(marker.getLatLong().latitude, marker.getLatLong().longitude));

    }
}
