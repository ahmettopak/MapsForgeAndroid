package com.ahmet.mapsforgeapp.gps;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.ahmet.mapsforgeapp.MapViewController;

import org.mapsforge.core.model.LatLong;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Author: Ahmet TOPAK$
 * Date: 1/20/2024$
 */
public class FakeGpsService {
    GpsListener gpsListener;
    private Timer fakeGpsTimer;
    private int refreshPeriod = 1000;

    private static final double ANKARA_LATITUDE = 39.721148566384485;
    private static final double ANKARA_LONGITUDE = 32.84021947057348;


    public FakeGpsService(GpsListener gpsListener){
        this.gpsListener = gpsListener;
    }

    public void startFakeGpsTimer() {
        fakeGpsTimer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                new Handler(Looper.getMainLooper()).post(() -> {
                    gpsListener.onLocationUpdate(MapViewController.generateRandomLocation(new LatLong(ANKARA_LATITUDE, ANKARA_LONGITUDE), 0.02));
                });
            }
        };
        fakeGpsTimer.schedule(timerTask, 0, refreshPeriod);
    }
    public void stopFakeGpsTimer() {
        fakeGpsTimer.cancel();
    }

    public void setRefreshPeriod(int refreshPeriod) {
        this.refreshPeriod = refreshPeriod;
    }
}
