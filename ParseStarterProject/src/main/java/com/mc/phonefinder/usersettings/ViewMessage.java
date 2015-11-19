package com.mc.phonefinder.usersettings;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.mc.phonefinder.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ViewMessage extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_message);
        try {
            ParseQuery<ParseObject> helpQuery = new ParseQuery<ParseObject>("Acknowledge");
            String objVal = ParseUser.getCurrentUser().getObjectId();
            helpQuery.whereEqualTo("userId", ParseUser.getCurrentUser().getObjectId());
            final List<List<String>> list = new ArrayList<List<String>>();
            List<String> temp = new ArrayList<String>();
            list.add(temp);
            helpQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    for (int i = 0; i < objects.size(); i++) {
                        list.get(0).add(objects.get(i).getString("ack"));
                        objects.get(0).deleteInBackground();
                    }
                    final ListView listView = (ListView) findViewById(R.id.list);
                    if (list.get(0).size() > 0) {
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(ViewMessage.this,
                                android.R.layout.simple_list_item_1, android.R.id.text1, list.get(0));
                        listView.setAdapter(adapter);
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view,
                                                    int position, long id) {

                                // ListView Clicked item index
                                int itemPosition = position;

                                // ListView Clicked item value
                                String itemValue = (String) listView.getItemAtPosition(position);

                                // Show Alert
                                Toast.makeText(getApplicationContext(),
                                        "Position :" + itemPosition + "  ListItem : " + itemValue, Toast.LENGTH_LONG)
                                        .show();

                            }

                        });
                    }
                }
            });

        }
        catch (Exception e)
        {

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_message, menu);
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
