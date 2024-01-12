package com.ahmet.mapsforgeapp;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;

import android.util.Log;

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
import org.mapsforge.map.layer.Layers;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.overlay.Circle;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.InternalRenderTheme;

import java.io.FileInputStream;

public class MapViewController {

    private MapView mapView;
    private TileCache tileCache;

    private Context context;

    public MapViewController(Context context){
        this.context = context;
    }
    public Marker addMarker(LatLong latLong, Drawable markerIcon) {
        Marker marker = new Marker(latLong, AndroidGraphicFactory.convertToBitmap(markerIcon), 0, 0);
        mapView.getLayerManager().getLayers().add(marker);
        return marker;
    }

    public void addMapTile(FileInputStream fileInputStream) {
        try {

            setTileCache();

            MapDataStore mapDataStore = new MapFile(fileInputStream);

            TileRendererLayer tileRendererLayer = new TileRendererLayer(tileCache, mapDataStore,
                    mapView.getModel().mapViewPosition, AndroidGraphicFactory.INSTANCE);
            tileRendererLayer.setXmlRenderTheme(InternalRenderTheme.OSMARENDER);

            mapView.getLayerManager().getLayers().add(tileRendererLayer);
            mapView.invalidate();

        } catch (Exception e) {
            Log.e("MapViewController", "Error while adding map tile: " + e.getMessage());
        }
    }

    public LatLong generateRandomLocation() {
        double lat = 37.7749 + (Math.random() - 0.5) * 0.1;
        double lon = -122.4194 + (Math.random() - 0.5) * 0.1;
        return new LatLong(lat, lon);
    }

    public MapView getMapView() {
        return mapView;
    }

    public void setMapView(@NonNull MapView mapView) {
        this.mapView = mapView;
        initMapView();
    }

    private void initMapView() {
        mapView.setClickable(true);
        mapView.getMapScaleBar().setVisible(true);
        mapView.setBuiltInZoomControls(true);
        mapView.getFpsCounter().setVisible(true);
    }

    public void setTileCache() {
        tileCache = AndroidUtil.createTileCache(context, "mapcache",
                mapView.getModel().displayModel.getTileSize(), 2f,
                mapView.getModel().frameBufferModel.getOverdrawFactor());
    }
}
