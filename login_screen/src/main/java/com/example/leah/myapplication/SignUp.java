package com.example.leah.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SignUp extends AppCompatActivity {

    private AutoCompleteTextView mEmailView;
    private EditText mFarmIDView;
    private EditText mPasswordView;

    private NewUserTask mNewUserTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mEmailView = (AutoCompleteTextView) findViewById(R.id.newEmail);

        mFarmIDView = (EditText) findViewById(R.id.newFarmID);
        mFarmIDView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.newFarmID || id == EditorInfo.IME_NULL) {
                    return true;
                }
                return false;
            }
        });

        mPasswordView = (EditText) findViewById(R.id.newPassword);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.newPassword || id == EditorInfo.IME_NULL) {
                    return true;
                }
                return false;
            }
        });

        Button newUserButton = (Button) findViewById(R.id.signUp_button);
        newUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptAddNewUser();

            }
        });


    }

    private void attemptAddNewUser() {
        if (mNewUserTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mFarmIDView.setError(null);
        mPasswordView.setError(null);


        // Store values at the time of the add attempt.
        String email = mEmailView.getText().toString();
        String farmID = mFarmIDView.getText().toString();
        String password = mPasswordView.getText().toString();
        Log.i("email", email);
        Log.i("farmID", farmID);
        Log.i("password", password);

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email, if the user entered one.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }
        // Check for a valid farmID, if the user entered one.
        if (TextUtils.isEmpty(farmID)) {
            mFarmIDView.setError(getString(R.string.error_invalid_farmID));
            focusView = mFarmIDView;
            cancel = true;
        }
        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(email)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }



        if (cancel) {


            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // perform the user login attempt.
            Log.i("executed", "yes");
            mNewUserTask = new NewUserTask(email, farmID,password);
            Log.i("executed", "created");

            mNewUserTask.execute((Void) null);
            Log.i("executed", "done");

        }
    }

    public class NewUserTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mFarmID;
        private final String mPassword;
        //String request  = "http://private-a59ad-katyscareapi.apiary-mock.com/tokens?include=users";
        String request  = "http://katys-care-api.herokuapp.com/v1/users";
        NewUserTask(String email, String farmID, String password) {
            mEmail = email;
            mFarmID = farmID;
            mPassword = password;
        }

        protected String makeNewUserRequest(){
            JSONObject postData = new JSONObject();

            JSONObject data = new JSONObject();
            JSONObject attributes = new JSONObject();
            JSONObject relationship = new JSONObject();
            JSONObject farm = new JSONObject();
            JSONArray farmData = new JSONArray();
            JSONObject farmData1 = new JSONObject();
            String typeFarm = "farms";
            String type = "users";

            try {
                attributes.put("type", type);
                attributes.put("email", mEmail);
                attributes.put("password", mPassword);


                farmData1.put("type", typeFarm);
                farmData1.put("id", mFarmID);
                farmData.put(farmData1);
                farm.put("data", farmData);

                relationship.put("works_for", farm);

                data.put("type", type);
                data.put("attributes", attributes);
                data.put("relationships", relationship);

                postData.put("data",data);
            } catch (JSONException e){
                Log.i("makeRequestJSON", "Impossible Error");
            }
            Log.i("postData",postData.toString() );
            return postData.toString();
        }

        protected String getHttpResponse(HttpURLConnection conn) {
            String response = "";
            StringBuilder sb = new StringBuilder();
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line.concat("\n"));
                }
                br.close();
                response = sb.toString();
            } catch (Exception e){
                Log.i("getHttpResponse", "Impossible Error");
            }
            return response;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Boolean result = true;
            Log.i("do in background", "yep, I get called");

            try {
                String postData = makeNewUserRequest();
                URL url = new URL(request);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setInstanceFollowRedirects(false);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("charset", "utf-8");
                conn.setRequestProperty("content-Length", Integer.toString(postData.length()));

                conn.setUseCaches(false);


                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

                wr.write(postData);
                wr.close();

                if (conn.getResponseCode() == HttpURLConnection.HTTP_CREATED || conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    //JSONObject resp = new JSONObject(getHttpResponse(conn));
                    //JSONObject data = resp.getJSONObject("data");
                    //JSONObject attr = resp.getJSONObject("attributes");
                    Log.i("response", "looks good");
                } else {
                    result = false;
                    Log.i("doInBackground", conn.getResponseMessage());
                }

            } catch (Exception e) {
                result = false;
            }

            return result;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mNewUserTask = null;

            if (success) {
                // finish();
                // on success, we want to redirect to main activity
                startActivity(new Intent(SignUp.this, LoginActivity.class));
            } else {
                mEmailView.setError(getString(R.string.error_incorrect_information));
                mEmailView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mNewUserTask = null;
        }



    }


}
