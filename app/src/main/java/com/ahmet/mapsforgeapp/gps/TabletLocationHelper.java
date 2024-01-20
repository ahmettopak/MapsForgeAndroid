package com.ahmet.mapsforgeapp.gps;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Timer;

/**
 * Author: Ahmet TOPAK
 * Since: 1/20/2024
 */
public class TabletLocationHelper {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 3;
    private static final long UPDATE_INTERVAL = 5000; // 5 saniyede bir güncelleme
    private static final long FASTEST_UPDATE_INTERVAL = 2000; // En fazla 2 saniye aralıklarla güncelleme

    private final Activity activity;
    private final FusedLocationProviderClient fusedLocationClient;
    private final LocationCallback locationCallback;

    public TabletLocationHelper(Activity activity, LocationCallback locationCallback) {
        this.activity = activity;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
        this.locationCallback = locationCallback;
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return false;
        }
    }

    public void startLocationUpdates() {
        if (checkLocationPermission()) {
            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setInterval(UPDATE_INTERVAL);
            locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    public void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            }
        }
    }
}