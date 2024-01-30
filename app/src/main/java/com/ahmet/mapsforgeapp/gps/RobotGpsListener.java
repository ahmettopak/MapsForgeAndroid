package com.ahmet.mapsforgeapp.gps;

import android.content.Context;
import android.util.Log;

import com.ahmet.mapsforgeapp.MapViewController;
import com.ahmet.mapsforgeapp.R;
import com.ahmet.mapsforgeapp.map.MapUtils;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.layer.overlay.Marker;

/**
 * Description of RobotGpsListener
 *
 * @author Ahmet TOPAK
 * @version 1.0
 * @since 1/30/2024
 */

public class RobotGpsListener extends LocationListener{
    private static String TAG = "Robot Gps Listener";
    Marker robotMarker;
    public RobotGpsListener(Context context , MapViewController mapViewController){
        super(context , mapViewController);
        robotMarker = MapUtils.createMarker(context.getDrawable(R.drawable.gps));
    }

    @Override
    public void onLocationUpdate(LatLong latLong) {
        mapViewController.updateMarkerLatLong(robotMarker, latLong);

        //TODO state change
        mapViewController.addMarker(robotMarker);

        Log.d(TAG, "Robot Lat Long Test Link: " + MapUtils.generateLatLongMapsLink(robotMarker.getLatLong().latitude, robotMarker.getLatLong().longitude));
    }
}
