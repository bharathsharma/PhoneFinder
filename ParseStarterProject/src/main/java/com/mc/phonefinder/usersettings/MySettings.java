package com.mc.phonefinder.usersettings;

import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import com.mc.phonefinder.R;
import com.mc.phonefinder.login.DispatchActivity;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import android.content.SharedPreferences;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;


public class MySettings extends ActionBarActivity {

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_settings);
       try {
           CheckBox checkbox1, checkbox2, checkbox3, checkbox4;
           EditText accessCode;
           ParseUser user = ParseUser.getCurrentUser();
           checkbox1 = (CheckBox) findViewById(R.id.alertCamera);
           checkbox2 = (CheckBox) findViewById(R.id.alertFall);
           checkbox3 = (CheckBox) findViewById(R.id.alertLocation);
           checkbox4 = (CheckBox) findViewById(R.id.alertUsers);
           accessCode = (EditText) findViewById(R.id.alertAccessCode);

           checkbox1.setChecked(loadBoolPrefs(user.getObjectId() + String.valueOf(R.id.alertCamera)));
           checkbox2.setChecked(loadBoolPrefs(user.getObjectId() + String.valueOf(R.id.alertFall)));
           checkbox3.setChecked(loadBoolPrefs(user.getObjectId() + String.valueOf(R.id.alertLocation)));
           checkbox4.setChecked(loadBoolPrefs(user.getObjectId() + String.valueOf(R.id.alertUsers)));
           accessCode.setText(loadStringPrefs(user.getObjectId() + String.valueOf(R.id.alertAccessCode)));

           ((Button) findViewById(R.id.saveSettings)).setOnClickListener(new View.OnClickListener() {
               public void onClick(View v) {
                   ParseQuery<ParseObject> query = ParseQuery.getQuery("Settings");
                   query.whereEqualTo("userObjectId", ParseUser.getCurrentUser().getObjectId().toString());

                   query.findInBackground(new FindCallback<ParseObject>() {
                       public void done(List<ParseObject> scoreList, ParseException e) {
                           //get current user
                           ParseUser user = ParseUser.getCurrentUser();
                           CheckBox checkbox1, checkbox2, checkbox3, checkbox4;
                           EditText accessCode;
                           checkbox1 = (CheckBox) findViewById(R.id.alertCamera);
                           checkbox2 = (CheckBox) findViewById(R.id.alertFall);
                           checkbox3 = (CheckBox) findViewById(R.id.alertLocation);
                           checkbox4 = (CheckBox) findViewById(R.id.alertUsers);
                           accessCode = (EditText) findViewById(R.id.alertAccessCode);

                           savePrefs(user.getObjectId() + String.valueOf(R.id.alertCamera), checkbox1.isChecked());
                           savePrefs(user.getObjectId() + String.valueOf(R.id.alertFall), checkbox2.isChecked());
                           savePrefs(user.getObjectId() + String.valueOf(R.id.alertLocation), checkbox3.isChecked());
                           savePrefs(user.getObjectId() + String.valueOf(R.id.alertUsers), checkbox4.isChecked());
                           savePrefs(user.getObjectId() + String.valueOf(R.id.alertAccessCode), accessCode.getText().toString().trim());
                           if (e == null) {
                               if (scoreList.size() > 0) {
                                   //store the location of the user with the objectId of the user
                                   scoreList.get(0).put("camera", checkbox1.isChecked());
                                   scoreList.get(0).put("userObjectId", user.getObjectId());
                                   scoreList.get(0).put("fall", checkbox2.isChecked());
                                   scoreList.get(0).put("location", checkbox3.isChecked());
                                   scoreList.get(0).put("otherUser", checkbox4.isChecked());
                                   scoreList.get(0).put("alertWord", accessCode.getText().toString().trim());
                                   scoreList.get(0).saveInBackground();
                                   Toast.makeText(MySettings.this, "Saved", Toast.LENGTH_LONG)
                                           .show();
                               } else {

                                   ParseObject locationObject = new ParseObject("Settings");
                                   locationObject.put("camera", checkbox1.isChecked());
                                   locationObject.put("fall", checkbox2.isChecked());
                                   locationObject.put("location", checkbox3.isChecked());
                                   locationObject.put("otherUser", checkbox4.isChecked());
                                   locationObject.put("userObjectId", user.getObjectId());
                                   locationObject.put("alertWord", accessCode.getText().toString().trim());
                                   locationObject.saveInBackground();
                                   Toast.makeText(MySettings.this, "Saved", Toast.LENGTH_LONG)
                                           .show();
                               }
                           } else {

                           }
                       }
                   });


               }
           });

           // Starts an intent of the log in activity
       }
       catch (Exception e)
       {

       }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my_settings, menu);
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
