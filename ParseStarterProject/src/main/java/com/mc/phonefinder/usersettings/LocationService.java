package com.mc.phonefinder.usersettings;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.mc.phonefinder.R;
import com.mc.phonefinder.login.MyActivity;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class LocationService extends Service
{
    public static final String BROADCAST_ACTION = "Hello World";
    private static final int TWO_SECONDS = 2000;
    public LocationManager locationManager;

    static final String TAG="bharathdebug";
    public  String ObjectId;
    public Location previousBestLocation = null;
    private static long UPDATE_INTERVAL = 60000;  //default
    double latitude=0;
    double longitude=0;
    private static Timer timer =null;
    Intent intent;
    int counter = 0;

    @Override
    public void onCreate()
    {
        intent = new Intent(BROADCAST_ACTION);
        if(timer!= null)
        {
            timer.cancel();
        }
        else
        {
            timer=new Timer();

        }
        _startService();


    }
    private void _startService()
    {

        timer.scheduleAtFixedRate(

                new TimerTask() {

                    public void run() {
                        doServiceWork();

                        Log.i(TAG,"Location service -- 1");

                        //finding if we can help any user
                       try {
                           ParseQuery<ParseObject> socialQuery = new ParseQuery<ParseObject>("Social");
                           socialQuery.whereEqualTo("userId", ParseUser.getCurrentUser().getObjectId());
                           socialQuery.findInBackground(new FindCallback<ParseObject>() {
                               @Override
                               public void done(List<ParseObject> objects, ParseException e) {
                                   if(objects!= null) {
                                       for (int i = 0; i < objects.size(); i++) {
                                           Log.i(TAG,"Location service -- 2");
                                           String value = objects.get(i).getString("targetIds");
                                           String split[] = value.split(",");
                                           for (String val : split) {
                                               Log.i(TAG,"Location service -- 3");
                                               if (val != null && val != "" && !val.isEmpty()) {
                                                   Log.i(TAG,"Location service -- 4");
                                                   notifyUser(val);
                                               }
                                           }
                                           objects.get(i).deleteInBackground();
                                       }
                                   }
                               }
                           });
                       }
                       catch (Exception e){}
                        //checking if we got help
                        try {
                            Log.i(TAG,"Location service -- 5");
                            ParseQuery<ParseObject> helpQuery = new ParseQuery<ParseObject>("Acknowledge");
                            String objVal =ParseUser.getCurrentUser().getObjectId();
                            helpQuery.whereEqualTo("userId", ParseUser.getCurrentUser().getObjectId());
                            final List<List<String>> list = new ArrayList<List<String>>();
                            List<String> temp = new ArrayList<String>();
                            list.add(temp);
                            helpQuery.findInBackground(new FindCallback<ParseObject>() {
                                @Override
                                public void done(List<ParseObject> objects, ParseException e) {
                                    Log.i(TAG,"Location service -- 6");
                                    if(objects != null) {
                                        if (objects.size() > 0) {
                                            notifyUserMessage();

                                        }
                                    }

                                }
                            });

                        }
                        catch (Exception e){
                            System.out.print(e.getStackTrace());
                        }



                        try {
                            ParseQuery<ParseObject> soundProfile = new ParseQuery<ParseObject>("Settings");
                            String objVal =ParseUser.getCurrentUser().getObjectId();
                            soundProfile.whereEqualTo("userObjectId", ParseUser.getCurrentUser().getObjectId());
                            soundProfile.findInBackground(new FindCallback<ParseObject>() {
                                @Override
                                public void done(List<ParseObject> objects, ParseException e) {
                                    Log.i(TAG,"Location service -- 7");
                                    if(objects!=null) {
                                        if(objects!=null)
                                        {
                                            if(objects.size()>0)
                                            {
                                                if(objects.get(0).getBoolean("alertPhone"))
                                                {
                                                    AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                                                    audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                                                    objects.get(0).put("alertPhone",false);
                                                    objects.get(0).saveInBackground();
                                                }
                                            }
                                        }
                                    }


                                }
                            });

                        }
                        catch (Exception e){
                            System.out.print(e.getStackTrace());
                        }

                    }
                }, 1000,5000);
        Log.i(TAG, "Location service -- 8");
        Log.i(getClass().getSimpleName(), "FileScannerService Timer started....");
    }

    private void doServiceWork()
    {

        try {

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
    public void notifyUserMessage() {
        NotificationManager notificationManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(LocationService.this, ViewMessage.class);
        //use the flag FLAG_UPDATE_CURRENT to override any notification already there
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new Notification(R.drawable.notification_template_icon_bg, "Some Text", System.currentTimeMillis());
        notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND;

        notification.setLatestEventInfo(this, "A user has responded for your help request", "Open here to view the user message", contentIntent);

        //10 is a random number I chose to act as the id for this notification
        notificationManager.notify(10, notification);
    }

    public void notifyUser(String value) {
        NotificationManager notificationManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(LocationService.this, HelpActivity.class);
        intent.putExtra("userObjectId",value);
        //use the flag FLAG_UPDATE_CURRENT to override any notification already there
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new Notification(R.drawable.notification_template_icon_bg, "Some Text", System.currentTimeMillis());
        notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND;

        notification.setLatestEventInfo(this, "A user needs help", "You can help find a lost device within 100 meters from where you are", contentIntent);

        //10 is a random number I chose to act as the id for this notification
        notificationManager.notify(10, notification);
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

}