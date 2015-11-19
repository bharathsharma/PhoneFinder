package com.mc.phonefinder.usersettings;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.mc.phonefinder.R;
import com.mc.phonefinder.login.MyActivity;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;
import java.util.Locale;

public class HelpActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);



        ((Button) findViewById(R.id.locateUser)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(HelpActivity.this, "Getting users phone location", Toast.LENGTH_LONG)
                        .show();
                String ObjectId = (String) HelpActivity.this.getIntent().getSerializableExtra("userObjectId");
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
                                Toast.makeText(HelpActivity.this, "Opening Maps", Toast.LENGTH_LONG)
                                        .show();
                            }
                        }
                    });
                }


            }
        });

                ((Button) findViewById(R.id.ackUser)).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Toast.makeText(HelpActivity.this, "Sending user your acknowledgement with the message", Toast.LENGTH_LONG)
                                .show();
                        String ObjectId = (String) HelpActivity.this.getIntent().getSerializableExtra("userObjectId");
                        ParseObject obj = new ParseObject("Acknowledge");
                        obj.put("userId", ObjectId);
                        EditText sendUserView = (EditText) findViewById(R.id.msgUser);
                        if (!sendUserView.getText().toString().isEmpty()) {
                            obj.put("ack", sendUserView.getText().toString());
                        }
                        obj.saveInBackground();
                        finish();
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_help, menu);
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
