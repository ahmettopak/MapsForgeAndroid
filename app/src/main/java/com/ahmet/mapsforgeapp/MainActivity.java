package com.ahmet.mapsforgeapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;

import android.content.Intent;

import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;


import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidBitmap;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.layers.MyLocationOverlay;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.datastore.MapDataStore;
import org.mapsforge.map.layer.Layers;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.overlay.Circle;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.reader.MapFile;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import org.mapsforge.map.rendertheme.InternalRenderTheme;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_MAP_REQUEST = 1;
    private static final int AUTO_LOAD_MAP_REQUEST = 2;

    double ankaraLatitude = 39.9334;
    double ankaraLongitude = 32.8597;
    double konyaLatitude = 37.8714;
    double konyaLongitude = 32.4846;
    Marker robotMarker;

    MapViewController mapViewController;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mapViewController = new MapViewController(this);

        AndroidGraphicFactory.createInstance(this.getApplication());
        Button selectMapButton = findViewById(R.id.selectMapButton);
        selectMapButton.setOnClickListener(view -> pickMapFile());

        mapViewController.setMapView(findViewById(R.id.mapView));

        robotMarker =  mapViewController.addMarker(new LatLong(ankaraLatitude,ankaraLongitude) , getDrawable(android.R.drawable.ic_menu_mylocation) );

//        Timer timer = new Timer();
//        timer.scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run() {
//                // Yeni bir konum oluşturun (örneğin, rastgele bir konum)
//                LatLong newLocation = mapViewController.generateRandomLocation();
//
//                // İkona yeni konumu ata
//                robotMarker.setLatLong(newLocation);
//
//                // Haritayı güncelle
//                mapViewController.getMapView().postInvalidate();
//            }
//        }, 0, 3000);

    }
    private void pickMapFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
       /// intent.setType("*/*"); // Tüm dosya türlerini seçmek için


        intent.setType("application/*"); // Sadece belirli bir uzantıyı seçmek için

        String[] mimeTypes = {"application/map"}; // .map uzantılı dosyaları belirtin
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

        startActivityForResult(Intent.createChooser(intent, "Harita Dosyasını Seç"), PICK_MAP_REQUEST);
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
            }
        }

    }



//    private void openMap(Uri uri) {
//
//           try {
//
//               mapView.getMapScaleBar().setVisible(true);
//               mapView.setBuiltInZoomControls(true);
//
//               Log.d("TAG", "openMap: " + uri);
//               /*
//                * To avoid redrawing all the tiles all the time, we need to set up a tile cache with an
//                * utility method.
//                */
//               TileCache tileCache = AndroidUtil.createTileCache(this, "mapcache",
//                       mapView.getModel().displayModel.getTileSize(), 1f,
//                       mapView.getModel().frameBufferModel.getOverdrawFactor());
//
//               /*
//                * Now we need to set up the process of displaying a map. A map can have several layers,
//                * stacked on top of each other. A layer can be a map or some visual elements, such as
//                * markers. Here we only show a map based on a mapsforge map file. For this we need a
//                * TileRendererLayer. A TileRendererLayer needs a TileCache to hold the generated map
//                * tiles, a map file from which the tiles are generated and Rendertheme that defines the
//                * appearance of the map.
//                */
//               FileInputStream fis = (FileInputStream) getContentResolver().openInputStream(uri);
//               MapDataStore mapDataStore = new MapFile(fis);
//               TileRendererLayer tileRendererLayer = new TileRendererLayer(tileCache, mapDataStore,
//                       mapView.getModel().mapViewPosition, AndroidGraphicFactory.INSTANCE);
//               tileRendererLayer.setXmlRenderTheme(InternalRenderTheme.DEFAULT);
//
//
//
//               /*
//                * On its own a tileRendererLayer does not know where to display the map, so we need to
//                * associate it with our mapView.
//                */
//               mapView.getLayerManager().getLayers().add(tileRendererLayer);
//               mapView.getFpsCounter().setVisible(true);
//
//
//               mapView.getLayerManager().getLayers().add(myLocationOverlay);
//
//               createlayerMark();
//
//
//               LatLong targetLocation = new LatLong(39.9334, 32.8597);  // Ankara'nın koordinatları
//
//               mapView.setCenter(targetLocation);
//
//               Bitmap markerBitmap = AndroidGraphicFactory.convertToBitmap(getResources().getDrawable(R.drawable.ic_launcher_foreground));
//               Marker marker = new Marker(targetLocation, markerBitmap, 0, 0);
//
//
//
//
//               myLocationOverlay.setPosition(ankaraLatitude, ankaraLongitude, Float.valueOf(10));
//
//               mapView.setCenter(new LatLong(ankaraLatitude, ankaraLongitude));
//
//               mapView.getLayerManager().getLayers().add(marker);
//
//           }
//           catch (Exception e){
//               Log.d("TAG", "openMap: " + e);
//           }
//
//    }
//
//    private static Paint getPaint(int color, int strokeWidth, Style style) {
//        Paint paint = AndroidGraphicFactory.INSTANCE.createPaint();
//        paint.setColor(color);
//        paint.setStrokeWidth(strokeWidth);
//        paint.setStyle(style);
//        return paint;
//    }
//
//    private void createlayerMark()
//    {
//        // marker to show at the location
//        Bitmap bitmap = new AndroidBitmap(BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_menu_mylocation));
//        Marker marker = new Marker(new LatLong(ankaraLatitude, ankaraLongitude), bitmap, 0, 0);
//
//
//        // circle to show the location accuracy (optional)
//        Circle circle = new Circle(new LatLong(ankaraLatitude, ankaraLongitude), 0,
//                getPaint(AndroidGraphicFactory.INSTANCE.createColor(48, 0, 0, 255), 0, Style.FILL),
//                getPaint(AndroidGraphicFactory.INSTANCE.createColor(160, 0, 0, 255), 2, Style.STROKE));
//
//        // create the overlay
//
//        myLocationOverlay = new MyLocationOverlay(marker, circle);
//
//        Layers layers = this.mapView.getLayerManager().getLayers();
//        layers.add(this.myLocationOverlay);
//
//    }
//    // Uri'den dosya yolu elde etme
//    private String getPathFromUri(Uri uri) {
//        String[] projection = {MediaStore.Images.Media.DATA};
//        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
//        if (cursor != null) {
//            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//            cursor.moveToFirst();
//            String filePath = cursor.getString(columnIndex);
//            cursor.close();
//            return filePath;
//        }
//        return null;
//    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        try {
            mapViewController.getMapView().destroyAll();
            AndroidGraphicFactory.clearResourceMemoryCache();
            super.onDestroy();
        } catch (Exception e) {
            Log.e("MainActivity", "Error in onDestroy", e);
        }
    }
}