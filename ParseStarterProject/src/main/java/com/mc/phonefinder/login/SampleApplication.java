package com.mc.phonefinder.login;

/**
 * Created by RakeshSubramanian on 11/10/2015.
 */
import android.app.Application;
import android.os.Parcel;
import android.os.Parcelable;

import com.mc.phonefinder.R;
import com.parse.Parse;

import java.io.Serializable;

public class SampleApplication extends Application implements Serializable
{
    private String userId;
    public String  getUserId(){
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    @Override
    public void onCreate(){
        Parse.initialize(this, getString(R.string.parse_app_id), getString(R.string.parse_client_id));
    }

}


