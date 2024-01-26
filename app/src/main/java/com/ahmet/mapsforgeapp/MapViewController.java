package com.ahmet.mapsforgeapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;

import android.net.Uri;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

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
import org.mapsforge.map.datastore.MultiMapDataStore;
import org.mapsforge.map.layer.Layers;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.overlay.Circle;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.InternalRenderTheme;
import org.mapsforge.map.util.MapViewProjection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class MapViewController {

    private MapView mapView;
    private TileCache tileCache;
    private final Context context;
    List<Marker> markerList = new ArrayList<>();
    MultiMapDataStore multiMapDataStore;

    public MapViewController(Context context){
        this.context = context;
        multiMapDataStore = new MultiMapDataStore(MultiMapDataStore.DataPolicy.RETURN_ALL);
    }

    public void updateMarkerLatLong(Marker marker , LatLong latLong) {
        marker.setLatLong(latLong);
        mapView.invalidate();
    }

    public void addMarker(Marker marker) {
        if (mapView.getLayerManager().getLayers().contains(marker)){
            mapView.getLayerManager().getLayers().remove(marker);
            mapView.getLayerManager().getLayers().add(marker);

            markerList.remove(marker);
            markerList.add(marker);

        }
        else{
            mapView.getLayerManager().getLayers().add(marker);
            markerList.add(marker);
        }
    }

    public void addMapTile(FileInputStream fileInputStream) {
        try {
            multiMapDataStore.addMapDataStore(new MapFile(fileInputStream) , false ,true);

            TileRendererLayer tileRendererLayer = new TileRendererLayer(tileCache, multiMapDataStore,
                    mapView.getModel().mapViewPosition, AndroidGraphicFactory.INSTANCE);
            tileRendererLayer.setXmlRenderTheme(InternalRenderTheme.OSMARENDER);

            mapView.getLayerManager().getLayers().add(tileRendererLayer);

           // mapView.invalidate();

            tileRendererLayer.requestRedraw();
        } catch (Exception e) {
            Log.e("MapViewController", "Error while adding map tile: " + e.getMessage());
            Toast.makeText(context, "Error while adding map tile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void addMapTileWithUriList(List<String> UriList){
        for (String mapItem : UriList){
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = (FileInputStream) context.getContentResolver().openInputStream(Uri.parse(mapItem));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            addMapTile(fileInputStream);
        }
    }


    public MapView getMapView() {
        return mapView;
    }

    public void setMapView(@NonNull MapView mapView) {
        this.mapView = mapView;
        initMapView();
        setTileCache();
    }

    private void initMapView() {
        mapView.setClickable(true);
        mapView.getMapScaleBar().setVisible(true);
        mapView.setBuiltInZoomControls(true);
        mapView.getFpsCounter().setVisible(true);
        mapView.setZoomLevelMax((byte) 15);
        mapView.setZoomLevelMin((byte) 5);

    }

    public void setTileCache() {

        tileCache = null;

        int tileSize = mapView.getModel().displayModel.getTileSize();
        float scaleFactor = 1.0f;
        double overdrawFactor = mapView.getModel().frameBufferModel.getOverdrawFactor();

        tileCache = AndroidUtil.createTileCache(context, "mapcache", tileSize, scaleFactor, overdrawFactor);
    }

    public void clearMap(){

        mapView.getLayerManager().getLayers().clear();

        multiMapDataStore = new MultiMapDataStore(MultiMapDataStore.DataPolicy.RETURN_ALL);

        setTileCache();
        markerList.clear();
        mapView.invalidate();
    }



}
