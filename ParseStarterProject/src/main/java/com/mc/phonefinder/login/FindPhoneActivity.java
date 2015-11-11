package com.mc.phonefinder.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mc.phonefinder.R;
import com.mc.phonefinder.usersettings.FindPhoneInterface;
import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.w3c.dom.Text;

import java.util.List;

public class FindPhoneActivity extends ActionBarActivity {
    private EditText usernameView;
    private EditText passwordView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_phone);
        usernameView = (EditText) findViewById(R.id.unTxt);
        passwordView = (EditText) findViewById(R.id.pwdTxt);



        // Set up the submit button click handler
        findViewById(R.id.findPhoneLogin).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // Validate the log in data
                boolean validationError = false;
                StringBuilder validationErrorMessage =
                        new StringBuilder(getResources().getString(R.string.error_intro));
                if (isEmpty(usernameView)) {
                    validationError = true;
                    validationErrorMessage.append(getResources().getString(R.string.error_blank_username));
                }
                if (isEmpty(passwordView)) {
                    if (validationError) {
                        validationErrorMessage.append(getResources().getString(R.string.error_join));
                    }
                    validationError = true;
                    validationErrorMessage.append(getResources().getString(R.string.error_blank_password));
                }
                validationErrorMessage.append(getResources().getString(R.string.error_end));

                // If there is a validation error, display the error
                if (validationError) {
                    Toast.makeText(FindPhoneActivity.this, validationErrorMessage.toString(), Toast.LENGTH_LONG)
                            .show();
                    return;
                }

                // Set up a progress dialog
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Settings");
                query.whereEqualTo("alertWord", passwordView.getText().toString().trim());
                final String[] userLoginCheck = new String[1];
                final String[] userNameGet = new String[1];

                query.findInBackground(new FindCallback<ParseObject>() {
                    public void done(List<ParseObject> scoreList, ParseException e) {
                        //get current user
                        if (e == null) {
                            if (scoreList.size() > 0) {
                                userLoginCheck[0] = String.valueOf(scoreList.get(0).get("userObjectId"));
                            }
                            if(userLoginCheck!=null) {
                                if ( userLoginCheck[0] != null) {
                                    ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
                                    userQuery.whereEqualTo("objectId", userLoginCheck[0]);
                                    try {
                                        List<ParseUser> listUser = userQuery.find();
                                        if(listUser.size()>0) {
                                            final ProgressDialog dlg = new ProgressDialog(FindPhoneActivity.this);
                                            dlg.setTitle("Please wait.");
                                            dlg.setMessage("Going to your lost phone settings.  Please wait.");
                                            dlg.show();
                                            for (ParseUser a : listUser) {
                                                if (a.getUsername().trim().equals(usernameView.getText().toString().trim())) {
                                                    startActivity(new Intent(FindPhoneActivity.this, FindPhoneInterface.class));
                                                }
                                                System.out.print(a.getUsername());
                                            }
                                        }
                                        else
                                        {
                                            startActivity(new Intent(FindPhoneActivity.this, FindPhoneActivity.class));
                                        }
                                    }
                                    catch (ParseException e1)
                                    {
                                    }
                                }
                            }

                        }
                    }
                });
            }
        });


    }
    private boolean isEmpty(EditText etText) {
        if (etText.getText().toString().trim().length() > 0) {
            return false;
        } else {
            return true;
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_find_phone, menu);
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
