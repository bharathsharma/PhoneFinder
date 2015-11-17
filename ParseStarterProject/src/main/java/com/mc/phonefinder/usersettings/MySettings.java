package com.mc.phonefinder.usersettings;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import com.mc.phonefinder.R;
import com.mc.phonefinder.login.DispatchActivity;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;

import android.content.SharedPreferences;
import android.widget.EditText;
import android.widget.Toast;

import android.hardware.SensorManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;
import android.telephony.SmsManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;


import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import android.view.View;
import android.widget.TextView;

public class MySettings extends AppCompatActivity implements SensorEventListener,SurfaceHolder.Callback {

    public static final String TAG = "bharathdebug";

    private SensorManager accelManage;
    private Sensor senseAccel;
    float accelValuesX[] = new float[128];
    float accelValuesY[] = new float[128];
    float accelValuesZ[] = new float[128];
    int index = 0;
    int k=0;

    boolean fallDetected = false;
    boolean pickUpDetected = false;
    Bundle b;
    String userId;
    //camera
    TextView testView;

    Camera camera;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;

    PictureCallback rawCallback;
    ShutterCallback shutterCallback;
    PictureCallback jpegCallback;

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }
    public void savePrefs(String key, boolean value)
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor edit = sp.edit();
        edit.putBoolean(key, value);
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
    public void onSensorChanged(SensorEvent sensorEvent) {
        // TODO Auto-generated method stub
        Sensor mySensor = sensorEvent.sensor;


        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            index++;
            accelValuesX[index] = sensorEvent.values[0];
            accelValuesY[index] = sensorEvent.values[1];
            accelValuesZ[index] = sensorEvent.values[2];
            Log.i(TAG,"Sensor change on");

            if(index>=127)
            {
                index=0;
                accelManage.unregisterListener(this);
                if(fallDetected == false)
                {
                    callForRecognition("fall");
                }
                else if(pickUpDetected == false)
                {
                    callForRecognition("pickup");
                }
                accelManage.registerListener(this, senseAccel, SensorManager.SENSOR_DELAY_NORMAL);
                Log.i(TAG,"Sensor change off");
                if(pickUpDetected== true)
                {

                    stopFallDetection(pickUpDetected);
                }

            }

        }
    }

    public void callForRecognition(String detectedParam){
        float prev = 0;
        float currx = 0;
        float curry = 0;
        float currz = 0;
        double rootSquare=0.0; //root square val of all sensors
        prev = 10;
        int smsCount = 0;

        for (int i = 11; i < 128; i++) {
            currx = accelValuesX[i];
            curry = accelValuesY[i];
            currz = accelValuesZ[i];

            if(detectedParam == "fall")
            {
                rootSquare = Math.sqrt(Math.pow(currx, 2) + Math.pow(curry, 2) + Math.pow(currz, 2));
                if (rootSquare < 2.0) {
                    smsCount++;
                    if (smsCount == 1) {
                        Toast.makeText(this, "Fall detected", Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "Fall Detected" + rootSquare);
                        Log.i(TAG, "Fall tracking disabled until pickup");
                        fallDetected = true;
                        pickUpDetected = false;
                        //    sendSMS();
                    }
                }
            }
            else if(detectedParam == "pickup")
            {
                if(curry >2.0 && currz < 9.0)
                {
                    Log.i(TAG, "Phone picked up");
                    Log.i(TAG, "Fall tracking started");
                    pickUpDetected = true;
                    // fallDetected = false;
                }
            }

        }
        smsCount = 0;
    }


    public void sendSMS() {
        String phoneNumber = "4807580126";
        Toast.makeText(MySettings.this, phoneNumber, Toast.LENGTH_SHORT).show();
        String message = "Fall detected";
        Log.i(TAG,"Sending SMS");
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
        Log.i(TAG, "SMS Sent");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_settings);

        CheckBox checkbox1,checkbox2,checkbox3,checkbox4;
        EditText accessCode;
        ParseUser user = ParseUser.getCurrentUser();
        checkbox1 = (CheckBox) findViewById(R.id.alertCamera);
        checkbox2= (CheckBox) findViewById(R.id.alertFall);
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

                //uploadImage("/storage/emulated/0/Pictures/PFS1447671561478.jpg", "ptJaUw8e6P");

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
                            String fallStatus;
                            String fallStatus_before = "false";
                            if (scoreList.size() > 0) {

                                //store prev fall status
                                fallStatus_before = scoreList.get(0).get("fall").toString();
                                Log.i(TAG, "fall status before save " + fallStatus_before);

                                //store the location of the user with the objectId of the user
                                scoreList.get(0).put("camera", checkbox1.isChecked());
                                scoreList.get(0).put("userObjectId", user.getObjectId());
                                scoreList.get(0).put("fall", checkbox2.isChecked());
                                scoreList.get(0).put("location", checkbox3.isChecked());
                                scoreList.get(0).put("otherUser", checkbox4.isChecked());
                                scoreList.get(0).put("alertWord", accessCode.getText().toString().trim());
                                scoreList.get(0).saveInBackground();
                                Toast.makeText(MySettings.this, "Saved", Toast.LENGTH_SHORT)
                                        .show();

                                //Get new fall status
                                fallStatus = scoreList.get(0).get("fall").toString();
                                userId = scoreList.get(0).get("userObjectId").toString();

                                Log.i(TAG, "fall status after save " + fallStatus);
                            } else {

                                ParseObject locationObject = new ParseObject("Settings");
                                locationObject.put("camera", checkbox1.isChecked());
                                locationObject.put("fall", checkbox2.isChecked());
                                locationObject.put("location", checkbox3.isChecked());
                                locationObject.put("otherUser", checkbox4.isChecked());
                                locationObject.put("userObjectId", user.getObjectId());
                                locationObject.put("alertWord", accessCode.getText().toString().trim());
                                locationObject.saveInBackground();
                                Toast.makeText(MySettings.this, "Saved", Toast.LENGTH_SHORT)
                                        .show();
                                //Start fall detection
                                fallStatus = locationObject.get("fall").toString();
                                Log.i(TAG, "fall status first time setting" + fallStatus);

                            }

                            if (fallStatus == "true") {
                                Log.i(TAG, "calling fall dect chk");
                                startFallDetection();
                            }
                            if (fallStatus_before == "true" && fallStatus == "false") {
                                stopFallDetection(false);
                            }

                        } else {

                        }
                    }
                });


            }
        }); // Starts an intent of the log in activity

        //Camera test function
        Log.i(TAG,"testing when back press");
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        Log.i(TAG, "Surface view created" + surfaceView);
        surfaceHolder = surfaceView.getHolder();

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        surfaceHolder.addCallback(this);

        // deprecated setting, but required on Android versions prior to 3.0
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        jpegCallback = new PictureCallback() {
            public void onPictureTaken(byte[] data, Camera camera) {
                FileOutputStream outStream = null;

                File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"PFS");
                if(!directory.exists())
                {
                    directory.mkdir();
                }
                String path = directory.toString();
                String fileName = "";
                Log.i(TAG, "Image saved Path -- " + path);
                try {
                    long filetime= System.currentTimeMillis();
                    fileName = String.format(path+"%d.jpg",filetime);
                    outStream = new FileOutputStream(fileName);
                    outStream.write(data);
                    outStream.close();
                    Log.d("Log", "onPictureTaken - wrote bytes: " + data.length);
                    Log.i(TAG, "in pic taken");
                } catch (FileNotFoundException e) {
                    Log.i(TAG, "file not found");
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                }
                Toast.makeText(getApplicationContext(), "Picture Saved", Toast.LENGTH_SHORT).show();

                uploadImage(fileName,userId);
                refreshCamera();
            }
        };
    }

    public void captureImage() throws IOException {

        Log.i(TAG,"capture image");
        camera.takePicture(null, null, jpegCallback);
    }

    public void refreshCamera() {
        if (surfaceHolder.getSurface() == null) {
            Log.i(TAG,"Suface is null");
            return;
        }

        // stop preview before making changes
        try {
            Log.i(TAG,"refresh cam");
            camera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }


        try {
            Log.i(TAG,"start preview cam");
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception e) {
            Log.i(TAG,"EXC" +e);
            Log.i(TAG,"start preview with exception  ");
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // Now that the size is known, set up the camera parameters and begin
        // the preview.
        Log.i(TAG, "surface change");
        refreshCamera();
    }

    public void surfaceCreated(SurfaceHolder holder) {
        try {
            // open the camera
            Log.i(TAG,"Creating camera");
            if (Camera.getNumberOfCameras() >= 2)
            {
                camera = Camera.open(1);
                Log.i(TAG,"Front facing camera opened");
            }
        } catch (RuntimeException e) {
            // check for exceptions
            System.err.println(e);
            Log.i(TAG, "Camera creation fail");
            return;
        }
        Camera.Parameters param;
        param = camera.getParameters();

        // modify parameter
        param.setPreviewSize(352, 288);
        camera.setParameters(param);
        try {
            // The Surface has been created, now tell the camera where to draw
            // the preview.
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception e) {
            // check for exceptions
            System.err.println(e);
            return;
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // stop preview and release camera
        Log.i(TAG, "destroy");
       // camera.stopPreview();
        //  camera.release();
        // camera = null;
    }
    public void startFallDetection()
    {
        Log.i(TAG,"inside fall detect");
        Toast.makeText(MySettings.this, "Tracking fall", Toast.LENGTH_SHORT).show();
        accelManage = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senseAccel = accelManage.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        accelManage.registerListener(this, senseAccel, SensorManager.SENSOR_DELAY_NORMAL);

    }

    public void stopFallDetection(boolean pickup)
    {
        accelManage.unregisterListener(this);
        Log.i(TAG, "In stop fall detection");
        if(pickup)
        {
            Toast.makeText(this, "Phone picked up", Toast.LENGTH_SHORT).show();
            pickUpDetected= false;
            fallDetected = false;
            try {
                captureImage();
            } catch (IOException e) {
                e.printStackTrace();
                Log.i(TAG,"Failed to auto capture image");
            }

        }
    }

    public void uploadImage(String filepath,String userIdObj)
    {
        Bitmap bitmap = BitmapFactory.decodeFile(filepath);

        if(bitmap == null)
        {
            Log.i(TAG,"Image null");
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        byte[] image = stream.toByteArray();

        ParseFile file = new ParseFile("picker.jpg", image);
        if(file == null)
        {
            Log.i(TAG,"File is null");
        }
        file.saveInBackground(new SaveCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    Log.i(TAG,"Upload Success");

                } else {
                    Log.i(TAG,"UploadFailure");
                    e.printStackTrace();
                }
            }
        }, new ProgressCallback() {
            public void done(Integer percentDone) {
                // Update your progress spinner here. percentDone will be between 0 and 100.
            }
        });

        Log.i(TAG, "Image File name " + filepath);
        Log.i(TAG, "Image user object " + userIdObj);

        ParseObject imgupload = new ParseObject("ImageUpload");
        imgupload.put("userObjectId", userIdObj);
        imgupload.put("fileName","picker");
        imgupload.put("imageFile",file);
        imgupload.saveInBackground();



        // Show a simple toast message
        Toast.makeText(MySettings.this, "Image Uploaded", Toast.LENGTH_SHORT).show();
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
    public void onBackPressed() {
        Log.i(TAG,"Back key pressed");
        camera.release();
        camera = null;
        finish();
    }
}
