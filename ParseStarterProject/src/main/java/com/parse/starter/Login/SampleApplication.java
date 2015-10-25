package com.parse.starter.Login;

import android.app.Application;

import com.parse.Parse;
import com.parse.starter.R;

public class SampleApplication extends Application {

    @Override
    public void onCreate(){
        Parse.initialize(this, getString(R.string.parse_app_id), getString(R.string.parse_client_id));
    }
}
