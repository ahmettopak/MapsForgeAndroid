package com.ahmet.mapsforgeapp.map;

import android.graphics.drawable.Drawable;
import android.net.Uri;

import androidx.annotation.NonNull;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.layer.overlay.Marker;

/**
 * Author: Ahmet TOPAK
 * Since: 1/20/2024
 */
public class MapUtils {
    private static final double DEFAULT_LATITUDE = 39.721148566384485;
    private static final double DEFAULT_LONGITUDE = 32.84021947057348;

    public static LatLong generateRandomLocation(LatLong destLatLong, double radius) {
        double angle = Math.random() * 2 * Math.PI;
        double distance = Math.random() * radius;

        double lat = destLatLong.getLatitude() + distance * Math.cos(angle);
        double lon = destLatLong.getLongitude() + distance * Math.sin(angle);

        return new LatLong(lat, lon);
    }

    public static LatLong generateRandomLocation(LatLong destLatLong, double tolerance, double radius) {
        double angle = Math.random() * 2 * Math.PI;
        double distance = Math.random() * tolerance;

        double lat = destLatLong.getLatitude() + distance * Math.cos(angle);
        double lon = destLatLong.getLongitude() + distance * Math.sin(angle);

        return new LatLong(lat, lon);
    }
    @NonNull
    public static String generateLatLongMapsLink(double latitude, double longitude) {
        Uri.Builder builder = Uri.parse("https://www.latlong.net/c/").buildUpon();
        builder.appendQueryParameter("lat", String.valueOf(latitude));
        builder.appendQueryParameter("long", String.valueOf(longitude));
        return builder.build().toString();
    }

    public static Marker createMarker(LatLong latLong, Drawable markerIcon) {
        return new Marker(latLong, AndroidGraphicFactory.convertToBitmap(markerIcon), 0, 0);
    }

    public static Marker createMarker(Drawable markerIcon) {
        return new Marker(new LatLong(DEFAULT_LATITUDE,DEFAULT_LONGITUDE), AndroidGraphicFactory.convertToBitmap(markerIcon), 0, 0);
    }
}
