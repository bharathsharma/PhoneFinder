package com.mc.phonefinder.usersettings;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class LocationService extends Service
{
    public static final String BROADCAST_ACTION = "Hello World";
    private static final int TWO_SECONDS = 1000;
    public LocationManager locationManager;

    public  String ObjectId;
    public Location previousBestLocation = null;
    private static long UPDATE_INTERVAL = 1*5*1000;  //default
    double latitude=0;
    double longitude=0;
    private static Timer timer = new Timer();
    Intent intent;
    int counter = 0;

    @Override
    public void onCreate()
    {
        intent = new Intent(BROADCAST_ACTION);
        _startService();

    }
    private void _startService()
    {
        timer.scheduleAtFixedRate(

                new TimerTask() {

                    public void run() {

                        doServiceWork();

                    }
                }, 1000, UPDATE_INTERVAL);
        Log.i(getClass().getSimpleName(), "FileScannerService Timer started....");
    }

    private void doServiceWork()
    {
        //do something wotever you want
        //like reading file or getting data from network
        try {
            /*locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            listener = new MyLocationListener();
            Criteria criteria = new Criteria();
            String best = locationManager.getBestProvider(criteria, false);
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            locationManager.requestLocationUpdates(best, 0, 1, (LocationListener) listener);*/
            getLocation();
            saveLocation();

        }
        catch (Exception e) {
        }

    }
    LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
        };
        public Location getLocation() {
        Location location = null;
        try {
            locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
            _startService();
            LocationListener locationListener = new LocationListener() {
                public void onLocationChanged(Location location) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();

                    System.out.println("Latitude:- " + latitude);
                    System.out.println("Longitude:- " + longitude);
                }

                public void onStatusChanged(String provider, int status,
                                            Bundle extras) {
                }

                public void onProviderEnabled(String provider) {
                }

                public void onProviderDisabled(String provider) {
                }
            };

                // getting GPS status
            boolean isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            boolean isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                if (isNetworkEnabled) {
                    if (Looper.myLooper() == null) {
                        Looper.prepare();
                    }
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            1,
                            5, locationListener);

                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        if (Looper.myLooper() == null) {
                            Looper.prepare();
                        }
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                1,
                                5,locationListener);
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    @Override
    public void onStart(Intent intent, int startId)
    {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_SECONDS;
        boolean isSignificantlyOlder = timeDelta < -TWO_SECONDS;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }



    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }



    @Override
    public void onDestroy() {
        // handler.removeCallbacks(sendUpdatesToUI);
        super.onDestroy();
    }

    public static Thread performOnBackgroundThread(final Runnable runnable) {
        final Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } finally {

                }
            }
        };
        t.start();
        return t;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

//you're code here
        return START_STICKY;

    }



    public void saveLocation() {
        //get the current location
        //query to see if the user is already added to location
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Location");
String val = ParseUser.getCurrentUser().getObjectId().toString();
        query.whereEqualTo("userId", ParseUser.getCurrentUser().getObjectId().toString());
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> scoreList, ParseException e) {
                //get current user
                ParseUser user = ParseUser.getCurrentUser();
                if (e == null) {
                    if(scoreList.size()>0){
                        //store the location of the user with the objectId of the user
                        ParseGeoPoint point = new ParseGeoPoint(latitude, longitude);
                        scoreList.get(0).put("location", point);
                        scoreList.get(0).put("userId", user.getObjectId());
                        scoreList.get(0).saveInBackground();}
                    else
                    {
                        ParseObject locationObject = new ParseObject("Location");
                        ParseGeoPoint point = new ParseGeoPoint(latitude, longitude);
                        locationObject.put("location", point);
                        locationObject.put("userId", user.getObjectId());
                        locationObject.saveInBackground();
                    }
                } else {

                }
            }
        });
    }
   /* public class MyLocationListener implements LocationListener
    {
        double latitude=0;
        double longitude=0;
        public void onLocationChanged(final Location loc)
        {
            if(isBetterLocation(loc, previousBestLocation)) {
                loc.getLatitude();
                loc.getLongitude();
                intent.putExtra("Latitude", loc.getLatitude());
                latitude = loc.getLatitude();
                longitude = loc.getLongitude();
                intent.putExtra("Longitude", loc.getLongitude());
                intent.putExtra("Provider", loc.getProvider());
                saveLocation();
                sendBroadcast(intent);

            }
        }


        public void saveLocation() {
            //get the current location
            //query to see if the user is already added to location
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Location");

            query.whereEqualTo("userId", ParseUser.getCurrentUser().getObjectId().toString());
            query.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> scoreList, ParseException e) {
                    //get current user
                    ParseUser user = ParseUser.getCurrentUser();
                    if (e == null) {
                        if(scoreList.size()>0){
                            //store the location of the user with the objectId of the user
                            ParseGeoPoint point = new ParseGeoPoint(latitude, longitude);
                            scoreList.get(0).put("location", point);
                            scoreList.get(0).put("userId", user.getObjectId());
                            scoreList.get(0).saveInBackground();}
                        else
                        {
                            ParseObject locationObject = new ParseObject("Location");
                            ParseGeoPoint point = new ParseGeoPoint(latitude, longitude);
                            locationObject.put("location", point);
                            locationObject.put("userId", user.getObjectId());
                            locationObject.saveInBackground();
                        }
                    } else {

                    }
                }
            });
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        public void onProviderDisabled(String provider)
        {
            Toast.makeText(getApplicationContext(), "Gps Disabled", Toast.LENGTH_SHORT).show();
        }


        public void onProviderEnabled(String provider)
        {
            Toast.makeText( getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
        }



    }*/
}