package com.mc.phonefinder.usersettings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.mc.phonefinder.R;
import com.mc.phonefinder.login.FindPhoneActivity;
import com.mc.phonefinder.login.SampleApplication;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;


public class FindPhoneInterface extends ActionBarActivity {
/*
For the below: check if the setting is set for the user and if so let the user use the functionality.
View location - Referes to location column in Settings table
Show Phone finder image - Displays the image of the person who find the phone
View nearby users - Refers to otherUsers in Settings table

Alert my phone - Rings the phone to easily identify it
 */

    static final boolean[] nearByUserSetting = new boolean[1];
    static final boolean[] locationSetting = new boolean[1];

    AtomicBoolean nearByUserSetting_check = new AtomicBoolean();
    AtomicBoolean locPrefs_check = new AtomicBoolean();

    public static String test = "Class";

    static final String TAG="bharathdebug";

    public void savePrefs(String key, boolean value)
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor edit = sp.edit();
        edit.putBoolean(key, value);
        edit.commit();
    }

    public boolean loadBoolPrefs(String key)
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        boolean cbVal =  sp.getBoolean(key, false);
        return cbVal;
    }

    public void savePrefs(String key, String value)
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString(key, value);
        edit.commit();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_phone_interface);
        final String ObjectId = (String)this.getIntent().getSerializableExtra("userObjectId");

        nearByUserSetting[0] = false;
        locationSetting[0] = false;

        savePrefs("nearByUserSetting", false);
        savePrefs("locationSetting", false);


        Log.i(TAG,"Before inner class"+test);
        //  final String ObjectId = (String)this.getIntent().getSerializableExtra("userObjectId");
        ParseQuery<ParseObject> getSettings = new ParseQuery<ParseObject>("Settings");
        getSettings.whereEqualTo("userObjectId", ObjectId);

        getSettings.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (objects != null) {
                    Log.i(TAG, "Object not null");
                    if(objects.size()>0){
//                        nearByUserSetting[0] = objects.get(0).getBoolean("otherUser");
//                        locationSetting[0] = objects.get(0).getBoolean("location");
//                        test = "Inner class";
                         nearByUserSetting_check.set( objects.get(0).getBoolean("otherUser"));
                         locPrefs_check.set(objects.get(0).getBoolean("location"));

                         Log.i(TAG, "Inner class neary by " + String.valueOf(nearByUserSetting_check.get()));
                         Log.i(TAG,"Inner class Location pref "+ (String.valueOf(locPrefs_check.get())));
//
//                        savePrefs("nearByUserSetting", nearByUserSetting[0]);
//                        savePrefs("locationSetting", locationSetting[0]);

                    }
                }
            }
        });

//        nearByUserSetting_check=loadBoolPrefs("nearByUserSetting");
//        locPrefs_check = loadBoolPrefs("locationSetting");
//
//        Log.i(TAG,"Final val after inner class "+test);
      //  System.out.print("Camera Setting " + nearByUserSetting[0]);
        Log.i(TAG,"Near by user "+ (String.valueOf(nearByUserSetting_check.get())));
        Log.i(TAG,"Location pref "+ (String.valueOf(locPrefs_check.get())));


        ((Button) findViewById(R.id.nearbyUsers)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(nearByUserSetting_check.get()) {
                    Intent i = new Intent();
                    savePrefs("findPhoneId", ObjectId);
                    Log.i(TAG, "FindPhoneInterface Object id " + ObjectId);
                    i.setClass(FindPhoneInterface.this, NearbyUserView.class);
                    //i.putExtra("userObjectId", ObjectId);
                    startActivity(i);
                    startActivity(new Intent(FindPhoneInterface.this, NearbyUserView.class));
                }
                else
                {
                    Toast.makeText(FindPhoneInterface.this, "Find nearby user service not set ", Toast.LENGTH_SHORT).show();
                }
            }
        });
        ((Button) findViewById(R.id.viewLocation)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(locPrefs_check.get()) {
                    Toast.makeText(FindPhoneInterface.this, "Getting Your Location", Toast.LENGTH_LONG)
                            .show();
                    String ObjectId = (String) FindPhoneInterface.this.getIntent().getSerializableExtra("userObjectId");
                    ParseQuery<ParseObject> query = ParseQuery.getQuery("Location");
                    if (!ObjectId.equals(null) && !ObjectId.equals("")) {
                        query.whereEqualTo("userId", ObjectId);
                        query.findInBackground(new FindCallback<ParseObject>() {
                            @Override
                            public void done(List<ParseObject> objects, ParseException e) {
                                ParseGeoPoint userLocation;
                                for (int i = 0; i < objects.size(); i++) {
                                    userLocation = objects.get(i).getParseGeoPoint("location");
                                    String uri = String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f", userLocation.getLatitude(), userLocation.getLongitude(), userLocation.getLatitude(), userLocation.getLongitude());
                                    //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:%f,%f?q=%f,%f",userLocation.getLatitude(), userLocation.getLongitude(),userLocation.getLatitude(), userLocation.getLongitude()));
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                                    startActivity(intent);
                                    Toast.makeText(FindPhoneInterface.this, "Opening Maps", Toast.LENGTH_LONG)
                                            .show();
                                }
                            }
                        });
                    }
                }
                else {
                    Toast.makeText(FindPhoneInterface.this, "Location Preference service not set ", Toast.LENGTH_SHORT).show();
                }

            }});


        //Bharath - View image of person who picked phone

        ((Button) findViewById(R.id.btn_phonepicker)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Starts an intent of the log in activity
                Log.i(TAG,"Find face object id "+ObjectId);
                savePrefs("findfaceObjId",ObjectId);
                startActivity(new Intent(FindPhoneInterface.this, ShowPhoneFinderImage.class));
            }
        });

        //Change ringer alert
        ((Button) findViewById(R.id.triggerAlert)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Settings");
                query.whereEqualTo("userObjectId", ObjectId);

                query.findInBackground(new FindCallback<ParseObject>() {
                    public void done(List<ParseObject> scoreList, ParseException e) {
                        //get current user
                        ParseUser user = ParseUser.getCurrentUser();
                        if (e == null) {
                            if (scoreList != null) {
                                if (scoreList.size() > 0) {
                                    //store the location of the user with the objectId of the user
                                    scoreList.get(0).put("alertPhone", true);
                                    scoreList.get(0).saveInBackground();
                                    Toast.makeText(FindPhoneInterface.this, "Phone Alerted", Toast.LENGTH_LONG)
                                            .show();
                                }
                            } else {
                                ParseObject alertVal = new ParseObject("Settings");
                                alertVal.put("userObjectId", ObjectId);
                                alertVal.put("alertPhone", true);
                                scoreList.get(0).saveInBackground();
                                Toast.makeText(FindPhoneInterface.this, "Phone Alerted", Toast.LENGTH_LONG)
                                        .show();
                            }
                        }
                    }
                });

            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_find_phone_interface, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
