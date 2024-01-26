package com.ahmet.mapsforgeapp;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.Activity;

import android.content.Intent;

import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import android.provider.Settings;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Toast;


import com.ahmet.mapsforgeapp.databinding.ActivityMainBinding;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.layer.overlay.Marker;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import com.ahmet.mapsforgeapp.gps.FakeGpsService;
import com.ahmet.mapsforgeapp.gps.GpsListener;
import com.ahmet.mapsforgeapp.gps.TabletLocationHelper;
import com.ahmet.mapsforgeapp.map.MapUtils;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;

public class MainActivity extends AppCompatActivity implements GpsListener {

    private static final String TAG = "MainActivity";
    private static final int PICK_MAP_REQUEST = 1;
    private static final int LOAD_DEFAULT_MAP_REQUEST = 2;
    private TabletLocationHelper tabletLocationHelper;

    private static final String MAP_PREF_ID = "MapPref";
    private static final String MAP_LIST_KEY = "MapListKey";
    SharedPreferencesManager preferencesManager;
    List<String> mapList = new ArrayList<>();
    private ActivityMainBinding binding;
    private MapViewController mapViewController;
    private Marker robotMarker;
    private Marker deviceMarker;

    private boolean autoCenter;
    FakeGpsService fakeGpsService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeComponents();

        binding.selectMapButton.setOnClickListener(view -> pickMapFile());
        binding.resetMapButton.setOnClickListener(view -> resetMapSource());

        binding.gpsServiceCheckBox.setOnCheckedChangeListener(this::onGpsServiceCheckedChanged);

        binding.autoCenterCheckBox.setOnCheckedChangeListener(this::onAutoCenterCheckedChanged);

        fakeGpsService = new FakeGpsService(this);

        setupMapView();

        tabletLocationHelper = new TabletLocationHelper(this, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                updateDeviceGpsLocation(new LatLong(locationResult.getLastLocation().getLatitude()  ,locationResult.getLastLocation().getLongitude()));

            }
        });

        tabletLocationHelper.startLocationUpdates();

    }

    private void initializeComponents() {
        mapViewController = new MapViewController(this);
        AndroidGraphicFactory.createInstance(this.getApplication());
        autoCenter = binding.autoCenterCheckBox.isChecked();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void setupMapView() {
        mapViewController.setMapView(findViewById(R.id.mapView));

        mapViewController.getMapView().setZoomLevel((byte) 15);


        robotMarker = MapUtils.createMarker(getDrawable(R.drawable.gps));

        deviceMarker = MapUtils.createMarker(getDrawable(R.drawable.gps_nav));

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


    private void updateRobotGpsLocation(LatLong latLong) {
        mapViewController.updateMarkerLatLong(robotMarker, latLong);

        if (autoCenter) {
            mapViewController.getMapView().setCenter(robotMarker.getLatLong());
        }



        //TODO state change
        mapViewController.addMarker(robotMarker);

        binding.gpsLocationTextView.setText(String.format("%s%s", getString(R.string.gps_location), robotMarker.getLatLong().toString()));

        Log.d(TAG, "Robot Lat Long Test Link: " + MapUtils.generateLatLongMapsLink(robotMarker.getLatLong().latitude, robotMarker.getLatLong().longitude));

    }
    private void updateDeviceGpsLocation(LatLong latLong) {
        mapViewController.updateMarkerLatLong(deviceMarker, latLong);


        //TODO state change
        mapViewController.addMarker(deviceMarker);

        Log.d(TAG, "Device Lat Long Test Link: " + MapUtils.generateLatLongMapsLink(robotMarker.getLatLong().latitude, robotMarker.getLatLong().longitude));

    }


    private void pickMapFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // Tüm dosya türlerini seçmek için
        startActivityForResult(Intent.createChooser(intent, "Harita Dosyasını Seç"), PICK_MAP_REQUEST);
    }


    private void resetMapSource(){

        mapList.clear();
        preferencesManager.saveStringList(MAP_LIST_KEY , mapList);

        mapViewController.clearMap();

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_MAP_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null) {

                FileInputStream fis = null;
                try {
                    fis = (FileInputStream) getApplicationContext().getContentResolver().openInputStream(Objects.requireNonNull(data.getData()));

                    mapList.add(data.getData().toString());

                    preferencesManager.saveStringList(MAP_LIST_KEY, mapList);

                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }

                Log.d(TAG, "onActivityResult: " +  Objects.requireNonNull(data.getData()));
                mapViewController.addMapTile(fis);
                mapViewController.addMarker(robotMarker);  // Todo fix marker z offset
            }
        }
    }
    @Override
    protected void onStart() {
        super.onStart();


        // Build SharedPreferencesManager with custom preferences
        preferencesManager = SharedPreferencesManager
                .with(this)
                .setPrefName(MAP_PREF_ID)
                .build();


        if (checkStoragePermissions()){
            mapList = preferencesManager.getStringList(MAP_LIST_KEY);
            mapViewController.addMapTileWithUriList(mapList);
        }
        else{
            requestForStoragePermissions();
        }

        tabletLocationHelper.startLocationUpdates();


    }
    public boolean checkStoragePermissions(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            //Android is 11 (R) or above
            return Environment.isExternalStorageManager();
        }else {
            //Below android 11
            int write = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int read = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE);

            return read == PackageManager.PERMISSION_GRANTED && write == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestForStoragePermissions() {
        //Android is 11 (R) or above
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            try {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", this.getPackageName(), null);
                intent.setData(uri);
                storageActivityResultLauncher.launch(intent);
            }catch (Exception e){
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                storageActivityResultLauncher.launch(intent);
            }
        }else{
            //Below android 11
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE
                    },
                    LOAD_DEFAULT_MAP_REQUEST
            );
        }

    }

    private final ActivityResultLauncher<Intent> storageActivityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>(){

                        @Override
                        public void onActivityResult(ActivityResult o) {
                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                                //Android is 11 (R) or above
                                if(Environment.isExternalStorageManager()){
                                    //Manage External Storage Permissions Granted
                                    Log.d(TAG, "onActivityResult: Manage External Storage Permissions Granted");
                                }else{
                                    Toast.makeText(MainActivity.this, "Storage Permissions Denied", Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                //Below android 11

                            }
                        }
                    });




    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == LOAD_DEFAULT_MAP_REQUEST){
            if(grantResults.length > 0){
                boolean write = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean read = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                if(read && write){
                    Toast.makeText(MainActivity.this, "Storage Permissions Granted", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(MainActivity.this, "Storage Permissions Denied", Toast.LENGTH_SHORT).show();
                }
            }
        }

        tabletLocationHelper.onRequestPermissionsResult(requestCode, grantResults);

    }

    @Override
    protected void onDestroy() {
        try {
            mapViewController.getMapView().destroyAll();
            AndroidGraphicFactory.clearResourceMemoryCache();
            AndroidGraphicFactory.clearResourceFileCache();
            tabletLocationHelper.stopLocationUpdates();
            super.onDestroy();
        } catch (Exception e) {
            Log.e("MainActivity", "Error in onDestroy", e);
        }
    }

    @Override
    public void onLocationUpdate(LatLong latLong) {
        updateRobotGpsLocation(latLong);
    }

}