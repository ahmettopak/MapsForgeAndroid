package com.ahmet.mapsforgeapp.gps;

import org.mapsforge.core.model.LatLong;

/**
 * Author: Ahmet TOPAK$
 * Date: 1/20/2024$
 */
public interface GpsListener {

    void onLocationUpdate(LatLong latLong);

}
