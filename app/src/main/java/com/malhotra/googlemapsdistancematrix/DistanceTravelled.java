package com.malhotra.googlemapsdistancematrix;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.malhotra.googlemapsdistancematrix.R;

/**
 * Created by Malhotra G on 8/3/2016.
 */
public class DistanceTravelled extends AppCompatActivity implements View.OnClickListener {
    ProgressBar distance;
    int total_dist = 9;
    Button start;
    TextView distance_done;
    double lat, lon;
    private Location thislocation;
    private boolean valid = false;
    private LocationManager locationManager;
    private LocationListener locationListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distance);
        intitialise();
    }

    private void intitialise() {

        try{
            total_dist = Integer.parseInt(getIntent().getStringExtra("total"));
        } catch (Exception e){
            total_dist = 254;
        }
        distance = (ProgressBar) findViewById(R.id.dist_travel);
        distance.setVisibility(View.GONE);
        start = (Button) findViewById(R.id.trip_start);
        start.setOnClickListener(this);
        distance.setMax(10000);
        distance_done = (TextView) findViewById(R.id.textView);
    }

    @Override
    public void onClick(View view) {
        getLocationFromGPS();
    }

    private void getLocationFromGPS() {
        locationListener = new LocationListener() {

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }

            @Override
            public void onLocationChanged(Location location) {
                gpsLocationReceived(location);

                if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    lat = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getLatitude();
                    lon = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getLongitude();
                }

            }
        };


        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        Criteria locationCritera = new Criteria();
        locationCritera.setAccuracy(Criteria.ACCURACY_COARSE);
        locationCritera.setAltitudeRequired(false);
        locationCritera.setBearingRequired(false);
        locationCritera.setCostAllowed(true);
        locationCritera.setPowerRequirement(Criteria.NO_REQUIREMENT);
        String providerName = locationManager.getBestProvider(locationCritera, true);

        if (providerName != null && locationManager.isProviderEnabled(providerName)) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

            }
            locationManager.requestLocationUpdates(providerName, 20000, 100, locationListener);
        } else {
            // Provider not enabled, prompt user to enable it
            Toast.makeText(getApplicationContext(), "please_turn_on_gps", Toast.LENGTH_LONG).show();
            Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(myIntent);
        }

        if (locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null) {

            lat = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude();
            lon = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude();
        } else if (locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null) {
            Log.e("TAG", "Inside NETWORK");

            lat = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getLatitude();
            lon = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getLongitude();

        } else {

            Log.e("TAG", "else +++++++ ");
            lat = -1;
            lon = -1;
        }
        Log.e("lat", String.valueOf(lat));
        Log.e("lng", String.valueOf(lon));
        SharedPreferences sp = getApplicationContext().getSharedPreferences("start_location", MODE_PRIVATE);
        if (sp.contains("latitude")) {
            Log.e("if", "done");
            double lati, lng, dist_traveled;
            dist_traveled = Double.parseDouble(sp.getString("dist_traveled", null));
            lati = Double.parseDouble(sp.getString("latitude", null));
            lng = Double.parseDouble(sp.getString("longitude", null));
            calculateDistance(lati, lng, lat, lon, dist_traveled);
        } else {
            Log.e("else", "done");
            SharedPreferences.Editor ed = sp.edit();
            ed.clear();
            ed.putString("latitude", String.valueOf(31.633979));
            ed.putString("longitude", String.valueOf(74.872264));
            ed.putString("dist_traveled", "0");
            ed.commit();
        }
    }

    private void calculateDistance(double lati, double lng, double cur_lat, double cur_lon, double dist_traveled) {
        double latA = Math.toRadians(lati);
        double lonA = Math.toRadians(lng);
        double latB = Math.toRadians(cur_lat);
        double lonB = Math.toRadians(cur_lon);
        double cosAng = (Math.cos(latA) * Math.cos(latB) * Math.cos(lonB - lonA)) +
                (Math.sin(latA) * Math.sin(latB));
        double ang = Math.acos(cosAng);
        double dist = ang * 6371;
        dist_traveled += dist;
        Toast.makeText(getApplicationContext(), "Distance Covered" + dist_traveled, Toast.LENGTH_LONG).show();
        SharedPreferences sp = getApplicationContext().getSharedPreferences("start_location", MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putString("dist_traveled", String.valueOf(dist_traveled));
        ed.commit();
        updateProgressBar(dist_traveled);
    }

    private void updateProgressBar(double dist_traveled) {
        int progress = (int) (dist_traveled/total_dist*100);
        distance.setVisibility(View.VISIBLE);
        //distance.setProgress(100*100);
        setProgressAnimate(distance, progress);
        startCountAnimation(progress);
    }
    private void setProgressAnimate(ProgressBar pb, int progressTo)
    {
        ObjectAnimator animation = ObjectAnimator.ofInt(pb, "progress", pb.getProgress(), progressTo * 100);
        animation.setDuration(3000);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.start();
    }

    private void startCountAnimation(final int progress) {
        //Log.e("progress",String.valueOf(progress));
        final ValueAnimator animator = new ValueAnimator();
        try {
            animator.setObjectValues(0, progress);
            animator.setDuration(3000);
            //Log.e("progress", String.valueOf(progress));
        }catch (Exception e){
            Log.e("progress",e.toString());
        }
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                //Log.e("progress",String.valueOf(progress));
                try{
                    distance_done.setText("" + (int) animator.getAnimatedValue() + "% Completed");
                }catch (Exception e){
                    Log.e("progress",e.toString());
                }
            }
        });
        animator.start();
    }

    protected void gpsLocationReceived(Location location) {
        thislocation = location;
    }
}


