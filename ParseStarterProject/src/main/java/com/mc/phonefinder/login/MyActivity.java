package com.mc.phonefinder.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import com.mc.phonefinder.usersettings.FindPhoneInterface;
import com.mc.phonefinder.usersettings.LocationService;
import com.mc.phonefinder.usersettings.MySettings;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.mc.phonefinder.R;
import java.util.List;
import java.util.Timer;


public class MyActivity extends Activity implements LocationListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        Intent i = new Intent();
        i.setClass(this, LocationService.class);
        try {
            i.putExtra("userObjectId", ParseUser.getCurrentUser().getObjectId().toString());
        }
        catch (Exception e)
        {}
        startService(new Intent(i));
        LocationManager mgr = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String best = mgr.getBestProvider(criteria, false);
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        mgr.requestLocationUpdates(best, 0, 1, this);

        ((Button) findViewById(R.id.action_logout)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Starts an intent of the log in activity
                ParseUser.getCurrentUser().logOut();
                startActivity(new Intent(MyActivity.this, DispatchActivity.class));
            }
        });
        ((Button) findViewById(R.id.findLostPhone)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Starts an intent of the log in activity
                startActivity(new Intent(MyActivity.this, FindPhoneActivity.class));
            }
        });

        ((Button) findViewById(R.id.mySettings)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Starts an intent of the log in activity
                startActivity(new Intent(MyActivity.this, MySettings.class));
            }
        });
    }
    public void savePrefs(String key, boolean value)
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor edit = sp.edit();
        edit.putBoolean(key,value);
        edit.commit();
    }
    public void savePrefs(String key, String value)
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString(key, value);
        edit.commit();
    }
    public boolean loadBoolPrefs(String key)
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        boolean cbVal =  sp.getBoolean(key, false);
        return cbVal;
    }
    public String loadStringPrefs(String key)
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String cbVal =  sp.getString(key, "");
        return cbVal;
    }
    double latitude=0;
    double longitude=0;

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
    public void onLocationChanged(final Location location) {
        new Thread(new Runnable() {

            @Override
            public void run() {
               latitude = location.getLatitude();
                longitude = location.getLongitude();
                saveLocation();
            }
        }).start();
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
    @Override
    protected void onResume() {
        super.onResume();
        LocationManager mgr = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String best = mgr.getBestProvider(criteria, false);
        mgr.requestLocationUpdates(best, 1, 1, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocationManager mgr = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String best = mgr.getBestProvider(criteria, false);
        mgr.removeUpdates(this);
    }
}
