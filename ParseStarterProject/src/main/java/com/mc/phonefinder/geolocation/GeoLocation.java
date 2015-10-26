package com.mc.phonefinder.geolocation;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import com.mc.phonefinder.R;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;


public class GeoLocation extends ActionBarActivity implements LocationListener{
    double latitude=0;
    double longitude=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geo_location);
        //get the current location
        getLocation();

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
    public Location getLocation() {
        Location location = null;
        try {
            LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

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
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            1,
                            5, this);

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
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                1,
                                5, this);
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
    public void onLocationChanged(Location location) {

    }

    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

}
