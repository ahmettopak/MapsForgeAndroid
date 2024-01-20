package com.ahmet.mapsforgeapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.Activity;

import android.content.Intent;

import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;


import com.ahmet.mapsforgeapp.databinding.ActivityMainBinding;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.layer.overlay.Marker;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.ahmet.mapsforgeapp.databinding.ActivityMainBinding;
import com.ahmet.mapsforgeapp.gps.FakeGpsService;
import com.ahmet.mapsforgeapp.gps.GpsListener;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.layer.overlay.Marker;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements GpsListener {

    private static final String TAG = "MainActivity";
    private static final int PICK_MAP_REQUEST = 1;
    private ActivityMainBinding binding;
    private MapViewController mapViewController;
    private Marker robotMarker;
    private boolean autoCenter;
    FakeGpsService fakeGpsService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeComponents();

        binding.selectMapButton.setOnClickListener(view -> pickMapFile());

        binding.gpsServiceCheckBox.setOnCheckedChangeListener(this::onGpsServiceCheckedChanged);

        binding.autoCenterCheckBox.setOnCheckedChangeListener(this::onAutoCenterCheckedChanged);

        fakeGpsService = new FakeGpsService(this);

        setupMapView();
    }

    private void initializeComponents() {
        mapViewController = new MapViewController(this);
        AndroidGraphicFactory.createInstance(this.getApplication());
        autoCenter = binding.autoCenterCheckBox.isChecked();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void setupMapView() {
        mapViewController.setMapView(findViewById(R.id.mapView));
        robotMarker = mapViewController.createMarker(getDrawable(android.R.drawable.ic_menu_mylocation));

        mapViewController.getMapView().setZoomLevel((byte) 15);

    }

    private void onGpsServiceCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        if (isChecked) {
            fakeGpsService.startFakeGpsTimer();
        } else {
            fakeGpsService.stopFakeGpsTimer();
        }
    }

    private void onAutoCenterCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        autoCenter = isChecked;
    }



    private void updateGpsLocation(LatLong latLong) {
        mapViewController.updateMarkerLatLong(robotMarker, latLong);

        if (autoCenter) {
            mapViewController.getMapView().setCenter(robotMarker.getLatLong());
        }

        mapViewController.addMarker(robotMarker);

        binding.gpsLocationTextView.setText(String.format("%s%s", getString(R.string.gps_location), robotMarker.getLatLong().toString()));

        Log.d(TAG, "Lat Long Test Link: " + generateLatLongMapsLink(robotMarker.getLatLong().latitude, robotMarker.getLatLong().longitude));

    }



    private void pickMapFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // Tüm dosya türlerini seçmek için
        startActivityForResult(Intent.createChooser(intent, "Harita Dosyasını Seç"), PICK_MAP_REQUEST);
    }

    @NonNull
    private static String generateLatLongMapsLink(double latitude, double longitude) {
        Uri.Builder builder = Uri.parse("https://www.latlong.net/c/").buildUpon();
        builder.appendQueryParameter("lat", String.valueOf(latitude));
        builder.appendQueryParameter("long", String.valueOf(longitude));
        return builder.build().toString();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_MAP_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri selectedMapUri = Uri.parse("content://0@media/external/file/60");

                FileInputStream fis = null;
                try {
                    fis = (FileInputStream) getContentResolver().openInputStream(Objects.requireNonNull(data.getData()));
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }

                mapViewController.addMapTile(fis);
                mapViewController.addMarker(robotMarker);  // Todo fix marker z offset
            }
        }
    }

    @Override
    protected void onDestroy() {
        try {
            mapViewController.getMapView().destroyAll();
            AndroidGraphicFactory.clearResourceMemoryCache();
            AndroidGraphicFactory.clearResourceFileCache();

            super.onDestroy();
        } catch (Exception e) {
            Log.e("MainActivity", "Error in onDestroy", e);
        }
    }

    @Override
    public void onLocationUpdate(LatLong latLong) {
        updateGpsLocation(latLong);
    }
}