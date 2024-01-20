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
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
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
    private static final int LOAD_DEFAULT_MAP_REQUEST = 2;

    List<String> mapList = new ArrayList<>();
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

                FileInputStream fis = null;
                try {
                    fis = (FileInputStream) getApplicationContext().getContentResolver().openInputStream(Objects.requireNonNull(data.getData()));

                    mapList.add(data.getData().toString());

                    SharedPreferencesManager.saveStringList(getApplicationContext(), mapList);

                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }

                Log.d(TAG, "onActivityResult: " +  Objects.requireNonNull(data.getData()));
                mapViewController.addMapTile(fis);
                mapViewController.addMarker(robotMarker);  // Todo fix marker z offset
            }
        }
    }
//    private void saveSelectedMapFile(Uri selectedMapUri) {
//        try {
//            InputStream inputStream = getContentResolver().openInputStream(selectedMapUri);
//
//            // Hedef dizini belirle
//            File destinationDirectory = getDestinationDirectory();
//
//            // Dosya adını seçili dosyanın adıyla aynı yap
//            String fileName = getFileName(selectedMapUri);
//
//            // Hedef dosyanın yolunu oluştur
//            File destinationFile = new File(destinationDirectory, fileName);
//
//            // Dosyayı kopyala
//            copyFile(inputStream, new FileOutputStream(destinationFile));
//
//            Toast.makeText(this, "Dosya başarıyla kaydedildi", Toast.LENGTH_SHORT).show();
//        } catch (IOException e) {
//            e.printStackTrace();
//            Toast.makeText(this, "Dosya kaydedilirken bir hata oluştu", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    private File getDestinationDirectory() {
//        // Hedef dizini belirle, örneğin:
//        // return new File(Environment.getExternalStorageDirectory(), "MyMapFiles");
//        // Bu örnekte dosyaların "MyMapFiles" dizinine kaydedileceği varsayılmıştır.
//        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
//    }
//
//    private String getFileName(Uri uri) {
//        // Uri'den dosya adını al, örneğin:
//        // String fileName = uri.getLastPathSegment();
//        // Bu örnekte dosya adının Uri'nin son segmenti olduğu varsayılmıştır.
//        return uri.getLastPathSegment();
//    }
//
//    private void copyFile(InputStream inputStream, OutputStream outputStream) throws IOException {
//        byte[] buffer = new byte[1024];
//        int length;
//        while ((length = inputStream.read(buffer)) > 0) {
//            outputStream.write(buffer, 0, length);
//        }
//        inputStream.close();
//        outputStream.close();
//    }
    @Override
    protected void onStart() {
        super.onStart();


        if (checkStoragePermissions()){
//            FileInputStream fis = getFileInputStreamForSpecificFile("/storage/emulated/0/Maps/turkey.map");
//            mapViewController.addMapTile(fis);
//
//            try {
//                FileInputStream fiss = (FileInputStream) getApplicationContext().getContentResolver().openInputStream(Uri.parse("content://media/external/file/1000000023"));
//                mapViewController.addMapTile(fiss);
//
//            } catch (FileNotFoundException e) {
//                throw new RuntimeException(e);
//            }

            List<String> retrievedList = SharedPreferencesManager.getStringList(getApplicationContext());


            for (String mapItem : retrievedList){
                FileInputStream fileInputStream = null;
                try {
                    fileInputStream = (FileInputStream) getApplicationContext().getContentResolver().openInputStream(Uri.parse(mapItem));
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
                mapViewController.addMapTile(fileInputStream);
            }

        }
        else{
            requestForStoragePermissions();
        }

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

    //private static final int STORAGE_PERMISSION_CODE = 23;
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

    private ActivityResultLauncher<Intent> storageActivityResultLauncher =
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
    }

    private FileInputStream getFileInputStreamForSpecificFile(String filePath) {
        try {
            return new FileInputStream(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
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