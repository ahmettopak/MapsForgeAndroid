package com.ahmet.mapsforgeapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;

import android.content.Intent;

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

public class MainActivity extends AppCompatActivity {

    private static String TAG = "MainActivity";
    ActivityMainBinding binding;

    private static final int PICK_MAP_REQUEST = 1;
    private static final int AUTO_LOAD_MAP_REQUEST = 2;

    double ankaraLatitude = 39.9334;
    double ankaraLongitude = 32.8597;
    double konyaLatitude = 37.8714;
    double konyaLongitude = 32.4846;
    Marker robotMarker;
    private Timer fakeGpsTimer;

    MapViewController mapViewController;

    boolean autoCenter = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mapViewController = new MapViewController(this);

        AndroidGraphicFactory.createInstance(this.getApplication());

        autoCenter = binding.autoCenterCheckBox.isChecked();

        binding.selectMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickMapFile();
            }
        });

        binding.gpsServiceCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    startFakeGpsTimer(1000);
                }
                else{
                    stopFakeGpsTimer();
                }
            }
        });

        binding.autoCenterCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                autoCenter = b;
            }
        });


        mapViewController.setMapView(findViewById(R.id.mapView));
        robotMarker =  mapViewController.createMarker(new LatLong(ankaraLatitude,ankaraLongitude) , getDrawable(android.R.drawable.ic_menu_mylocation) );
        mapViewController.getMapView().setCenter(robotMarker.getLatLong());
        mapViewController.getMapView().setZoomLevel((byte) 15);
        mapViewController.addMarker(robotMarker);


    }

    private void startFakeGpsTimer(int period) {

        fakeGpsTimer = new Timer();

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                // Zamanlayıcı görevi burada çalıştırılır
                // Örneğin, UI güncellemesi yapmak için Handler kullanabilirsiniz
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        mapViewController.updateMarkerLatLong(robotMarker,mapViewController.generateRandomLocation(new LatLong(ankaraLatitude,ankaraLongitude) , 0.02));

                        if (autoCenter){
                            mapViewController.getMapView().setCenter(robotMarker.getLatLong());
                        }
                        mapViewController.addMarker(robotMarker);

                        binding.gpsLocationTextView.setText(String.format("%s%s", getString(R.string.gps_location), robotMarker.getLatLong().toString()));

                        Log.d(TAG,"Lat Long Test Link: " + generateLatLongMapsLink(robotMarker.getLatLong().latitude , robotMarker.getLatLong().longitude));


                    }
                });
            }
        };

        // Zamanlayıcıyı belirli bir süre sonra başlat (örneğin, 1000 milisaniye sonra, ardından her 1000 milisaniyede bir)
        fakeGpsTimer.schedule(timerTask, 0, period);
    }

    private void stopFakeGpsTimer() {
        fakeGpsTimer.cancel();
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
        String url = builder.build().toString();
        return url;
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


                //Todo fix marker z offset
                mapViewController.addMarker(robotMarker);

            }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

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
}