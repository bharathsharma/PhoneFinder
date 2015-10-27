package com.mc.phonefinder.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.mc.phonefinder.usersettings.GeoLocation;
import com.parse.ParseUser;
import com.mc.phonefinder.R;


public class MyActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_my);
        ((Button) findViewById(R.id.action_logout)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Starts an intent of the log in activity
                ParseUser.getCurrentUser().logOut();
                startActivity(new Intent(MyActivity.this, DispatchActivity.class));
            }
        });

        ((Button) findViewById(R.id.geoSetting)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Starts an intent of the log in activity
                startActivity(new Intent(MyActivity.this, GeoLocation.class));
            }
        });
    }
}
