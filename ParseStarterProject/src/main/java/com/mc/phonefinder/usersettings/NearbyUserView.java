package com.mc.phonefinder.usersettings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.mc.phonefinder.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.pubnub.api.*;
import org.json.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NearbyUserView extends ActionBarActivity {
    public String loadStringPrefs(String key)
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String cbVal =  sp.getString(key, "");
        return cbVal;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_user_view);
        finish();
        //startActivity(getIntent());
        final List<List<String>> objectList = new ArrayList<List<String>>();
        final String ObjectId = loadStringPrefs("findPhoneId");//(String) this.getIntent().getSerializableExtra("userObjectId");
        final ParseQuery<ParseObject> nearByLocn = new ParseQuery<ParseObject>("Location");
        nearByLocn.whereEqualTo("userId", ObjectId);
        nearByLocn.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                final ParseGeoPoint userLocation;
                if(objects != null) {
                    if (objects.size() > 0) {
                        userLocation = objects.get(0).getParseGeoPoint("location");
                        ParseQuery<ParseObject> helperUsers = new ParseQuery<ParseObject>("Location");
                        helperUsers.findInBackground(new FindCallback<ParseObject>() {
                            @Override
                            public void done(final List<ParseObject> objects, ParseException e) {
                                ParseGeoPoint pt;
                                List<String> objectIds = new ArrayList<String>();
                                for (int i = 0; i < objects.size(); i++) {
                                    if (objects.get(i).getString("userId").equals(ObjectId)) {
                                        continue;
                                    }
                                    pt = objects.get(i).getParseGeoPoint("location");



                                    float temp = distFrom((float) userLocation.getLatitude(), (float) userLocation.getLongitude(), (float) pt.getLatitude(), (float) pt.getLongitude());
                                    if (temp <= 100) {
                                        objectIds.add(objects.get(i).getString("userId"));
                                        ParseQuery<ParseObject> query = ParseQuery.getQuery("Social");

                                        query.whereEqualTo("userId", objects.get(i).getString("userId"));
                                        final int finalI = i;
                                        query.findInBackground(new FindCallback<ParseObject>() {
                                            public void done(List<ParseObject> scoreList, ParseException e) {
                                                //get current user
                                                int j = finalI;
                                                if (e == null) {
                                                    if (scoreList.size() > 0) {
                                                        //store the location of the user with the objectId of the user
                                                        String temp = scoreList.get(0).getString("targetIds");
                                                        temp += "," + ObjectId;
                                                        scoreList.get(0).put("targetIds", temp);
                                                        scoreList.get(0).saveInBackground();
                                                    } else {
                                                        ParseObject locationObject = new ParseObject("Social");
                                                        locationObject.put("userId", objects.get(finalI).getString("userId"));
                                                        locationObject.put("targetIds", ObjectId);
                                                        locationObject.saveInBackground();
                                                    }
                                                } else {

                                                }
                                            }
                                        });

                                    }
                                }
                            }
                        });
                        Toast.makeText(NearbyUserView.this, "Asking Nearby Users For Help", Toast.LENGTH_LONG)
                                .show();
                    }
                }
            }
        });
    }
    public static float distFrom(float lat1, float lng1, float lat2, float lng2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        float dist = (float) (earthRadius * c);

        return dist;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_nearby_user_view, menu);
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
