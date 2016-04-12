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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

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
                startActivity(new Intent(SignUp.this, MainActivity.class));
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
        String password = mPasswordView.getText().toString();;
        boolean cancel = false;
        View focusView = null;

        // Check for a valid email, if the user entered one.
        if (!TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }
        // Check for a valid farmID, if the user entered one.
        if (!TextUtils.isEmpty(farmID)) {
            mFarmIDView.setError(getString(R.string.error_invalid_farmID));
            focusView = mFarmIDView;
            cancel = true;
        }
        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(email)) {
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
            mNewUserTask = new NewUserTask(email, farmID,password);
            mNewUserTask.execute((Void) null);
        }
    }

    public class NewUserTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mFarmID;
        private final String mPassword;
        //String request  = "http://private-a59ad-katyscareapi.apiary-mock.com/tokens?include=users";
        String request  = "http://private-anon-e20dc275e-katyscareapi.apiary-proxy.com/users&offset=offset&limit=limit&include=farm";
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
            JSONObject farmData = new JSONObject();
            String userType = "app_users";
            String typeFarm = "farms";
            String type = "users";



            try {
                attributes.put("username", mEmail);
                attributes.put("type", userType);

                farmData.put("type", typeFarm);
                farmData.put("id", mFarmID);
                farm.put("data", farmData);

                relationship.put("farm", farm);
                data.put("type", type);
                data.put("attributes", attributes);
                data.put("relationships", relationship);

            } catch (JSONException e){
                Log.i("makeRequestJSON", "Impossible Error");
            }
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

                //try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())){
                // wr.write(postData);

                //}
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

                wr.write(postData);
                wr.close();

                if (conn.getResponseCode() == HttpURLConnection.HTTP_CREATED || conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    JSONObject resp = new JSONObject(getHttpResponse(conn));
                    JSONObject data = resp.getJSONObject("data");
                    String token = data.getString("id");
                    SharedPreferences sp = getSharedPreferences("com.example.leah.myapplication", Context.MODE_PRIVATE);
                    sp.edit().putString("token", token).apply();  //adds token to shared preferences
                    Log.i("doInBackground", sp.getString("token", "NO_TOKEN_FOUND"));
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
                startActivity(new Intent(SignUp.this, MainActivity.class));
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
