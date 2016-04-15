package com.example.leah.myapplication;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
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




public class AddNewCow extends AppCompatActivity {

    private AddNewCowTask mNewCowTask = null;


    private AutoCompleteTextView mCowIDView;
    private EditText mStateView;
    private EditText mCowNameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_cow);

        mCowIDView = (AutoCompleteTextView) findViewById(R.id.cowID);

        mStateView = (EditText) findViewById(R.id.state);
        mStateView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.state || id == EditorInfo.IME_NULL) {
                    return true;
                }
                return false;
            }
        });
        mCowNameView = (EditText) findViewById(R.id.cowName);
        mCowNameView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.cowName || id == EditorInfo.IME_NULL) {
                    return true;
                }
                return false;
            }
        });

        Button addCowToListButton = (Button) findViewById(R.id.addCowToList);
        addCowToListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptAdd();
                startActivity(new Intent(AddNewCow.this, MainActivity.class));
            }
        });




    }


    private void attemptAdd() {
        if (mNewCowTask != null) {
            return;
        }

        // Reset errors.
        mCowIDView.setError(null);
        mStateView.setError(null);
        mCowNameView.setError(null);


        SharedPreferences sp = getSharedPreferences("com.example.leah.myapplication", Context.MODE_PRIVATE);

        // Store values at the time of the add attempt.
        String cowID = mCowIDView.getText().toString();
        String state = mStateView.getText().toString();
        String cowName = mCowNameView.getText().toString();
        Log.i("cowID", cowID);
        Log.i("state", state);
        Log.i("cowName", cowName);

        String farmID = sp.getString("farmID","NO farm found");
        boolean cancel = false;
        View focusView = null;

        // Check for a valid cowID, if the user entered one.
        if (TextUtils.isEmpty(cowID)) {
            mCowIDView.setError(getString(R.string.error_invalid_cowID));
            focusView = mCowIDView;
            cancel = true;
        }




        if (cancel) {

            Log.i("executed", "no");
            focusView.requestFocus();

        } else {
            // perform the new user attempt.
            Log.i("executed", "yes");

            mNewCowTask = new AddNewCowTask(cowID, state, farmID,cowName);
            Log.i("executed", "created");

            mNewCowTask.execute((Void) null);
            Log.i("executed", "done");

        }
    }



    public class AddNewCowTask extends AsyncTask<Void, Void, Boolean> {

        private final String mCowID;
        private final String mState;
        private final String mFarmID;
        private final String mCowName;
        String request  = "http://polls.apiblueprint.org/calves?include=farm,treatment_plan";
        AddNewCowTask(String cowID, String state, String farmID,String cowName) {
            mCowID = cowID;
            mState = state;
            mFarmID = farmID;
            mCowName = cowName;
        }

        protected String makeAddNewCowRequest(){
            JSONObject postData = new JSONObject();

            JSONObject data = new JSONObject();
            String typeCow = "calves";
            String typeFarm = "farms";
            //String typeTreatment = "treatment_plans";
            JSONObject attributes = new JSONObject();
            JSONObject relationship = new JSONObject();
            JSONObject farm = new JSONObject();
            JSONObject farmData = new JSONObject();
            //JSONObject treatment_plan = new JSONObject();
            //JSONObject treatment_Data = new JSONObject();

            try {
                attributes.put("cid", mCowID);
                attributes.put("name", mCowName);

                farmData.put("type", typeFarm);
                farmData.put("id", mFarmID);

                farm.put("data", farmData);

                //treatment_Data.put("type", typeTreatment);
                //treatment_Data.put("id", mState);
                //treatment_plan.put("data", treatment_Data);
                relationship.put("farm", farm);
                //relationship.put("treatment_plan", treatment_plan);
                data.put("type", typeCow);
                data.put("attributes", attributes);
                data.put("relationships", relationship);
                postData.put("data",data);

                Log.i("postData", postData.toString());

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
            Log.i("do in background", "yep, I get called");

            try {
                String postData = makeAddNewCowRequest();
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

                    Log.i("doInBackground", "added");
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
            mNewCowTask = null;

            if (success) {
                // finish();
                // on success, we want to redirect to main activity
                startActivity(new Intent(AddNewCow.this, MainActivity.class));
            } else {
                mCowIDView.setError(getString(R.string.error_incorrect_information));
                mCowIDView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mNewCowTask = null;
        }



    }




}
