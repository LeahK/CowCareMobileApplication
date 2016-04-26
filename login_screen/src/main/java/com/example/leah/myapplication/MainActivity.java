package com.example.leah.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TabHost;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // some variables for TabHost view
    ArrayList<Cow> todoCows = new ArrayList<Cow>();
    ArrayList<Cow> waitingCows = new ArrayList<Cow>();
    private GetCowTask mGetCowTask = null;

    // some variables for the TabHost listviews
    ListView listViewTodoCows;
    ListView listViewWaitingCows;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // set up the list views
        listViewTodoCows = (ListView) findViewById(R.id.listTodo);
        listViewWaitingCows = (ListView) findViewById(R.id.listWaiting);

        // tabHost is setup here
        TabHost tabHost = (TabHost) findViewById(R.id.tabHost);
        tabHost.setup();

        // now create a tab to the tab host
        TabHost.TabSpec tabSpec = tabHost.newTabSpec("TODO");
        // what's the content of this tab?
        tabSpec.setContent(R.id.tabTodo);

        // what's the text of the tab?
        tabSpec.setIndicator("TODO");

        // okay, now add the tab.
        tabHost.addTab(tabSpec);

        // populate the arrays with cows from the database
        mGetCowTask = new GetCowTask();
        mGetCowTask.execute((Void) null);

        // set up list adapters
        final ArrayAdapter<Cow> todoAdapter = new listTodoAdapter();

        final ArrayAdapter<Cow> waitAdapter = new listWaitingAdapter();

        // repeat for additional tab
        tabSpec = tabHost.newTabSpec("WAITING");
        // what's the content of this tab?
        tabSpec.setContent(R.id.tabWaiting);
        // what's the text of the tab?
        tabSpec.setIndicator("WAITING");

        // okay, now add the tab.
        tabHost.addTab(tabSpec);

        // set up addNewCowButton
        FloatingActionButton addNewCowButton = (FloatingActionButton) findViewById(R.id.addNewCow);
        addNewCowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, AddNewCow.class));
            }
        });

        // set up refresh button
        FloatingActionButton refreshButton = (FloatingActionButton) findViewById(R.id.refresh);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                todoCows.clear();
                waitingCows.clear();

                mGetCowTask = new GetCowTask();
                mGetCowTask.execute((Void) null);

                // notify the adapters that their data sets have changed
                todoAdapter.notifyDataSetChanged();
                waitAdapter.notifyDataSetChanged();
            }
        });
    }


    private void addTodoCow(long cowID){
        // note that Boolean isTodo will ALWAYS be true here and isWaiting will ALWAYS be false
        todoCows.add(new Cow(cowID, true, false));
    }

    private class listTodoAdapter extends ArrayAdapter<Cow> {
        // constructor
        public listTodoAdapter(){
            super (MainActivity.this, R.layout.listview_todocow, todoCows);
        }

        // create a method that will fill our view with all the array elements
        @Override
        public View getView(int position, View view, ViewGroup parent){
            if (view == null)
            {
                view = getLayoutInflater().inflate(R.layout.listview_todocow, parent, false);
            }

            Cow currentCow = todoCows.get(position);

            // create a text view
            TextView todoCowID = (TextView) view.findViewById(R.id.todoCowID);

            // set the text in the text view
            long cowID = currentCow.getCowID();
            final String cowID_text = String.valueOf(cowID);
            todoCowID.setText(cowID_text);

            todoCowID.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SharedPreferences sp = getSharedPreferences("com.example.leah.myapplication", Context.MODE_PRIVATE);
                    sp.edit().putString("currentCow", cowID_text).apply();
                    startActivity(new Intent(MainActivity.this, NextStep.class));
                }
            });

            // when done setting all the text and shtuff
            // return the view
            return view;

        }
    }

    private void addWaitingCow(long cowID){
        waitingCows.add(new Cow(cowID, false, true));
    }

    private class listWaitingAdapter extends ArrayAdapter<Cow> {
        // constructor
        public listWaitingAdapter(){
            super (MainActivity.this, R.layout.listview_waitingcow, waitingCows);
        }

        // create a method that will fill our view with all the array elements
        @Override
        public View getView(int position, View view, ViewGroup parent){
            if (view == null)
            {
                view = getLayoutInflater().inflate(R.layout.listview_waitingcow, parent, false);
            }

            Cow currentCow = waitingCows.get(position);


            // create a text view
            TextView waitingCowID = (TextView) view.findViewById(R.id.waitingCowID);

            // set the text in the text view
            long cowID = currentCow.getCowID();
            String cowID_text = String.valueOf(cowID);
            waitingCowID.setText(cowID_text);

            /*waitingCowID.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(MainActivity.this, CowInformation.class));
                }
            });*/

            // when done setting all the text and shtuff
            // return the view
            return view;

        }
    }

    public boolean checkIfCowInWaitArray(long id){
        for (Cow cow : waitingCows){
            if (cow.getCowID() == id){
                return true;
            }
        }
        return false;
    }

    public boolean checkIfCowInTodoArray(long id){
        for (Cow cow : todoCows){
            if (cow.getCowID() == id){
                return true;
            }
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        // updated to close application instead of going back to the login page
        this.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    public class GetCowTask extends AsyncTask<Void, Void, Boolean>{


        SharedPreferences sp = getSharedPreferences("com.example.leah.myapplication", Context.MODE_PRIVATE);
        String token = sp.getString("token", "NO_TOKEN_FOUND");

        String farmID = sp.getString("farmID", "NO_TOKEN_FOUND");
        String request  = "http://katys-care-api.herokuapp.com/v1/farms/"+ farmID +"?include=calves.treatment_plan";

        GetCowTask(){

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

        protected Boolean doInBackground(Void... params) {
            Boolean result = true;

            try {
                URL url = new URL(request);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setInstanceFollowRedirects(false);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", token);
                conn.setRequestProperty("charset", "utf-8");
                conn.setUseCaches(false);

                if (conn.getResponseCode() == HttpURLConnection.HTTP_CREATED || conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    Log.i("success",conn.getResponseMessage());

                    JSONObject resp = new JSONObject(getHttpResponse(conn));
                    //test
                    JSONArray includedArray = resp.getJSONArray("included");

                    for(int i =0; i < includedArray.length(); i++){
                        JSONObject cow = includedArray.getJSONObject(i);
                        if(cow.getString("type").equals("calves")){
                            JSONObject cowAttributes = cow.getJSONObject("attributes");
                            Long cid = Long.valueOf(cowAttributes.getString("cid"));

                            //display cow into two lists

                            if (cowAttributes.getBoolean("waiting")){
                                // only add the cow if it's not already in the array

                                if (cowAttributes.getString("wait_expires") != null){
                                    SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
                                    Date now = new Date(); // when creating a new date the date is the current time
                                    //String currentDate = sdf.format(now);
                                    String wait_expires = cowAttributes.getString("wait_expires"); // time as a string
                                    Date date_wait_expires = sdf.parse(wait_expires);
                                    //is wait expires in the past?

                                    if (date_wait_expires.before(now)){ // COW IS ON ToDoList
                                        Log.i("cid for to do", cid.toString());
                                        Log.i("current time", now.toString());
                                        Log.i("waitExp time", date_wait_expires.toString());
                                        if(!checkIfCowInTodoArray(cid)) {
                                            addTodoCow(cid);
                                        }
                                    }else { // COW IS ON WAIT LIST
                                        if(!checkIfCowInWaitArray(cid)){
                                            addWaitingCow(cid);
                                        }
                                        Log.i("cid for waiting", cid.toString());
                                        Log.i("current time", now.toString());
                                        Log.i("waitExp time", date_wait_expires.toString());

                                    }
                                }
                            }else{
                                Log.i("neither cid: ", cid.toString());
                                //cow is in neither list.  Happy Happy Joy Joy, the cow is dead or cured.
                            }
                        }
                    }
                    //test

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
            mGetCowTask = null;

            if (success) {
                // finish();
                // on success, we want to redirect to main activity
                ArrayAdapter<Cow> todoAdapter = new listTodoAdapter();
                listViewTodoCows.setAdapter(todoAdapter);
                todoAdapter.notifyDataSetInvalidated();

                ArrayAdapter<Cow> waitAdapter = new listWaitingAdapter();
                listViewWaitingCows.setAdapter(waitAdapter);
                waitAdapter.notifyDataSetInvalidated();
                Log.i("psotExecuted", "successed");
            } else {
                Log.i("psotExecuted", "Not successed");

            }
        }

        @Override
        protected void onCancelled() {
            mGetCowTask = null;
        }

    }


}
