package com.ahmet.mapsforgeapp.gps;

import android.content.Context;

import com.ahmet.mapsforgeapp.MapViewController;

import org.mapsforge.core.model.LatLong;

/**
 * Description of LocationListener
 *
 * @author Ahmet TOPAK
 * @version 1.0
 * @since 1/30/2024
 */

public abstract class LocationListener implements GpsListener {
    public Context context;
    public MapViewController mapViewController;
    public LocationListener(Context context , MapViewController mapViewController){
        this.context = context;
        this.mapViewController = mapViewController;
    }
}
