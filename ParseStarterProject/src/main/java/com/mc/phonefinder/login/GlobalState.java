package com.mc.phonefinder.login;

/**
 * Created by RakeshSubramanian on 11/10/2015.
 */
import java.util.ArrayList;
import java.util.List;
import android.app.Application;

public class GlobalState extends Application
{
    private String userId;

    public String  getUserId(){
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
}