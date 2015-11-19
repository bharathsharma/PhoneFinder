package com.mc.phonefinder.usersettings;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.mc.phonefinder.R;
import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

/**
 * Created by Sharma on 11/16/15.
 */
public class ShowPhoneFinderImage extends Activity {

    public static final String TAG = "bharathdebug";

    public String loadStringPrefs(String key)
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String cbVal =  sp.getString(key, "");
        return cbVal;
    }

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_phone_finder_image);

        String objecid = loadStringPrefs("findfaceObjId");
        Log.i(TAG,"Object id -- "+ objecid);
        ParseQuery<ParseObject> query = ParseQuery.getQuery("ImageUpload");
        query.whereEqualTo("userObjectId", objecid);
        query.orderByDescending("createdAt").setLimit(1);

        Log.i(TAG, "Query created");

        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> imageList, ParseException e) {

                if (e == null) {
                    Log.i(TAG, "Query extraction ok");
                    if(imageList != null ) {
                        if(imageList.size()>0) {
                            ParseFile image = imageList.get(0).getParseFile("imageFile");
                            final ImageView imageView = (ImageView) findViewById(R.id.finder_imageView);

                            image.getDataInBackground(new GetDataCallback() {

                                @Override
                                public void done(byte[] data, ParseException e) {

                                    if (e == null) {
                                        Log.i(TAG, "Got image in BG");
                                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0,
                                                data.length);

                                        if (bmp != null) {
                                            Log.i(TAG, "Bitmap not null");
                                            imageView.setImageBitmap(bmp);
                                        }
                                    } else {
                                        Log.e("paser after downloade", " null");
                                    }

                                }
                            });
                        }
                        else
                        {
                            Toast.makeText(ShowPhoneFinderImage.this, "No images found", Toast.LENGTH_SHORT);
                        }
                    }

                }


            }

        });
    }
}