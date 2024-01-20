package com.ahmet.mapsforgeapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

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

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class MapViewController {

    private MapView mapView;
    private TileCache tileCache;
    private final Context context;
    List<Marker> markerList = new ArrayList<>();
    MultiMapDataStore multiMapDataStore;
    private static final double DEFAULT_LATITUDE = 39.721148566384485;
    private static final double DEFAULT_LONGITUDE = 32.84021947057348;

    public MapViewController(Context context){
        this.context = context;
        multiMapDataStore = new MultiMapDataStore(MultiMapDataStore.DataPolicy.RETURN_ALL);

    }
    public Marker createMarker(LatLong latLong, Drawable markerIcon) {
        return new Marker(latLong, AndroidGraphicFactory.convertToBitmap(markerIcon), 0, 0);
    }

    public Marker createMarker(Drawable markerIcon) {
        return new Marker(new LatLong(DEFAULT_LATITUDE,DEFAULT_LONGITUDE), AndroidGraphicFactory.convertToBitmap(markerIcon), 0, 0);
    }
    public void updateMarkerLatLong(Marker marker , LatLong latLong) {
        marker.setLatLong(latLong);
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
            multiMapDataStore.addMapDataStore(new MapFile(fileInputStream) , false ,false);

            TileRendererLayer tileRendererLayer = new TileRendererLayer(tileCache, multiMapDataStore,
                    mapView.getModel().mapViewPosition, AndroidGraphicFactory.INSTANCE);
            tileRendererLayer.setXmlRenderTheme(InternalRenderTheme.OSMARENDER);

            mapView.getLayerManager().getLayers().add(tileRendererLayer);

           // mapView.invalidate();

            tileRendererLayer.requestRedraw();
        } catch (Exception e) {
            Log.e("MapViewController", "Error while adding map tile: " + e.getMessage());
        }
    }


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
    public MapView getMapView() {
        return mapView;
    }

    public void setMapView(@NonNull MapView mapView) {
        this.mapView = mapView;
        initMapView();
        setTileCache();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initMapView() {
        mapView.setClickable(true);
        mapView.getMapScaleBar().setVisible(true);
        mapView.setBuiltInZoomControls(true);
        mapView.getFpsCounter().setVisible(true);
        mapView.setZoomLevelMax((byte) 15);
        mapView.setZoomLevelMin((byte) 5);

    }

    public void setTileCache() {
        int tileSize = mapView.getModel().displayModel.getTileSize();
        float scaleFactor = 1.0f;
        double overdrawFactor = mapView.getModel().frameBufferModel.getOverdrawFactor();

        tileCache = AndroidUtil.createTileCache(context, "mapcache", tileSize, scaleFactor, overdrawFactor);
    }


}
