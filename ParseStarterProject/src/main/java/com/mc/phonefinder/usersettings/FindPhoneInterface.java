package com.mc.phonefinder.usersettings;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
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

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

public class FindPhoneInterface extends ActionBarActivity {
/*
For the below: check if the setting is set for the user and if so let the user use the functionality.
View location - Referes to location column in Settings table
Take Picture - Referes to camera in Settings table
View nearby users - Refers to otherUsers in Settings table

Alert my phone - Rings the phone to easily identify it
 */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_phone_interface);
        String ObjectId = (String)this.getIntent().getSerializableExtra("userObjectId");

        ((Button) findViewById(R.id.nearbyUsers)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(FindPhoneInterface.this, NearbyUserView.class));
            }});
        ((Button) findViewById(R.id.viewLocation)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(FindPhoneInterface.this, "Getting Your Location", Toast.LENGTH_LONG)
                        .show();
                String ObjectId = (String)FindPhoneInterface.this.getIntent().getSerializableExtra("userObjectId");
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Location");
                if(!ObjectId.equals(null) && !ObjectId.equals(""))
                {
                    query.whereEqualTo("userId", ObjectId);
                    query.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> objects, ParseException e) {
                            ParseGeoPoint userLocation;
                            for (int i = 0; i < objects.size(); i++) {
                                userLocation = objects.get(i).getParseGeoPoint("location");
                               String uri = String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f", userLocation.getLatitude(), userLocation.getLongitude(),userLocation.getLatitude(), userLocation.getLongitude());
                                //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:%f,%f?q=%f,%f",userLocation.getLatitude(), userLocation.getLongitude(),userLocation.getLatitude(), userLocation.getLongitude()));
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                                startActivity(intent);
                                Toast.makeText(FindPhoneInterface.this, "Opening Maps", Toast.LENGTH_LONG)
                                        .show();
                            }
                        }
                    });
                }


            }});

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
