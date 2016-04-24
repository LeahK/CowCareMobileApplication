package com.example.leah.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import java.io.OutputStreamWriter;
import java.util.ArrayList;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class NextStep extends AppCompatActivity {

    private GetPlanTask mGetPlanTask = null;
    MyCustomAdapter dataAdapter = null;
    private UpdateCowTask mUpdateCowTask = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next_step);


        SharedPreferences sp = getSharedPreferences("com.example.leah.myapplication", Context.MODE_PRIVATE);
        String cowID = sp.getString("currentCow", "NO cow found");
        TextView cowIdView = (TextView)findViewById(R.id.textView2);
        cowIdView.setText("Cow id: " + cowID);
        mGetPlanTask = new GetPlanTask();
        mGetPlanTask.execute((Void) null);



    }


    private void Display(){
        SharedPreferences sp = getSharedPreferences("com.example.leah.myapplication", Context.MODE_PRIVATE);

        //display the steps
        int size = sp.getInt("numOfChild", 0);
        //Array list of countries
        final ArrayList<Step> stepList = new ArrayList<Step>();
        for (int i = 0; i < size;i++){
            String key1 = "description"+i;
            String key2 = "wait_time"+i;

            String id = "waiting time: "+ sp.getString(key2,"no des found")+"days";
            String des = sp.getString(key1,"no time found");
            Step step = new Step(id,des,false);
            stepList.add(step);
        }


        //create an ArrayAdaptar from the String Array
        dataAdapter = new MyCustomAdapter(this,
                R.layout.step_info, stepList);
        final ListView listView = (ListView) findViewById(R.id.listView1);
        // Assign adapter to ListView
        listView.setAdapter(dataAdapter);
        //
        Button nextStep = (Button) findViewById(R.id.ok);
        nextStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int check =0;
                int key = 0;
                for (int i =0; i < stepList.size();i++){
                    if(stepList.get(i).isSelected()){
                        check++;
                        key =i;

                    }
                }
                if(check ==0){
                    Toast.makeText(getApplicationContext(),
                            "You must select one step"
                            ,
                            Toast.LENGTH_LONG).show();
                }else if(check ==1){

                    mUpdateCowTask = new UpdateCowTask(key);
                    mUpdateCowTask.execute((Void) null);
                }else if(check > 1){
                    Toast.makeText(getApplicationContext(),
                            "You can not select more than one step"
                            ,
                            Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    private class MyCustomAdapter extends ArrayAdapter<Step> {

        private ArrayList<Step> stepList;

        public MyCustomAdapter(Context context, int textViewResourceId,
                               ArrayList<Step> stepList) {
            super(context, textViewResourceId, stepList);
            this.stepList = new ArrayList<Step>();
            this.stepList.addAll(stepList);
        }

        private class ViewHolder {
            TextView id;
            CheckBox des;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;
            Log.v("ConvertView", String.valueOf(position));

            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.step_info, null);

                holder = new ViewHolder();
                holder.id = (TextView) convertView.findViewById(R.id.code);
                holder.des = (CheckBox) convertView.findViewById(R.id.checkBox1);
                convertView.setTag(holder);

                holder.des.setOnClickListener( new View.OnClickListener() {
                    public void onClick(View v) {
                        CheckBox cb = (CheckBox) v ;
                        Step step = (Step) cb.getTag();
                        if (!step.isSelected()) {

                            step.setSelected(cb.isChecked());

                        }else{

                            step.setSelected(cb.isChecked());
                        }
                    }
                });
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }

            Step step = stepList.get(position);
            holder.id.setText(" (" +  step.getID() + ")");
            holder.des.setText(step.getDes());
            holder.des.setChecked(step.isSelected());
            holder.des.setTag(step);

            return convertView;

        }

    }

    public class GetPlanTask extends AsyncTask<Void, Void, Boolean> {

        SharedPreferences sp = getSharedPreferences("com.example.leah.myapplication", Context.MODE_PRIVATE);
        String token = sp.getString("token", "NO_TOKEN_FOUND");

        String cowID = sp.getString("currentCow", "NO_COW_FOUND");
        String farmID = sp.getString("farmID", "NO_COW_FOUND");

        String request  = "http://katys-care-api.herokuapp.com/v1/calves/"+farmID+"_"+cowID+"?include=treatment_plan";

        GetPlanTask(){

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
                Log.i("url", url.toString());

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setInstanceFollowRedirects(false);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", token);
                conn.setRequestProperty("charset", "utf-8");
                conn.setUseCaches(false);

                SharedPreferences sp = getSharedPreferences("com.example.leah.myapplication", Context.MODE_PRIVATE);

                if (conn.getResponseCode() == HttpURLConnection.HTTP_CREATED || conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    Log.i("GetPlan success",conn.getResponseMessage());

                    JSONObject resp = new JSONObject(getHttpResponse(conn));
                    String currentState = resp.getJSONObject("data").getJSONObject("attributes").getString("treatment_plan_position");
                    JSONObject planBody = resp.getJSONArray("included").getJSONObject(0).getJSONObject("attributes").getJSONObject("body");
                    String head = planBody.getString("head");

                    if(currentState.equals("null")){
                        JSONObject headJson = planBody.getJSONObject(head);
                        Log.i("head", headJson.toString());
                        JSONArray children = headJson.getJSONArray("children");
                        Log.i("child", children.toString());
                        sp.edit().putInt("numOfChild",children.length()).apply();


                        for (int i =0; i< children.length();i++){
                            String childId = children.getString(i);
                            String des = planBody.getJSONObject(childId).getString("description");
                            String time = planBody.getJSONObject(childId).getString("wait");

                            Log.i("des"+i,des);
                            String description = "description"+i;
                            String wait_time = "wait_time"+i;
                            String child_id = "id" +i;
                            sp.edit().putString(description,des).apply();
                            sp.edit().putString(wait_time,time).apply();
                            sp.edit().putString(child_id,childId).apply();

                            Log.i(child_id,childId);


                        }
                    }else{

                        JSONArray children = planBody.getJSONObject(currentState).getJSONArray("children");
                        if(children.length()==0){
                            startActivity(new Intent(NextStep.this, MainActivity.class));
                        }
                        sp.edit().putInt("numOfChild",children.length()).apply();

                        for (int i = 0; i< children.length();i++){
                            String childId = children.getString(i);
                            String des = planBody.getJSONObject(childId).getString("description");
                            String time = planBody.getJSONObject(childId).getString("wait");

                            Log.i("des" + i, des);
                            String description = "description"+i;
                            String wait_time = "wait_time"+i;
                            String child_id = "id" +i;
                            sp.edit().putString(description,des).apply();
                            sp.edit().putString(wait_time,time).apply();
                            sp.edit().putString(child_id,childId).apply();


                        }

                    }

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
            mGetPlanTask = null;

            if (success) {
                // finish();
                Display();
                Log.i("GetPlan postExecuted", "success");
            } else {
                Log.i("GetPlan postExecuted", "Not success");

            }
        }

        @Override
        protected void onCancelled() {
            mGetPlanTask = null;
        }



    }
    public class UpdateCowTask extends AsyncTask<Void, Void, Boolean> {
        private final String mStepID;
        private final String mTimeID;


        SharedPreferences sp = getSharedPreferences("com.example.leah.myapplication", Context.MODE_PRIVATE);

        String cowID = sp.getString("currentCow", "NO_COW_FOUND");
        String farmID = sp.getString("farmID", "NO_COW_FOUND");

        String request  = "http://katys-care-api.herokuapp.com/v1/calves/"+farmID+"_"+cowID;

        UpdateCowTask(int key){
            mStepID = "id"+ key;
            mTimeID = "wait_time"+key;

        }
        protected String makeUpdateCowRequest(){

            JSONObject postData = new JSONObject();
            JSONObject data = new JSONObject();
            String type = "calves";
            boolean waiting = false;
            String waitExp = null;
            String cowid = farmID+"_"+cowID;

            Log.i("key",mStepID);

            String childID = sp.getString(mStepID,"no step id found");
            String wait_time_string = sp.getString(mTimeID, "no time is found");

            int wait_time = Integer.parseInt(wait_time_string);
            sp.edit().putInt("checkTime",wait_time).apply();

            if(wait_time != 0){
                waiting = true;
                Date we = new Date();
                SimpleDateFormat dateFormat= new SimpleDateFormat("yyyy-MM-dd");
                Calendar c = Calendar.getInstance();
                c.setTime(new Date());
                c.add(Calendar.DATE, wait_time);
                waitExp = dateFormat.format(c.getTime());
            }else{
                Log.i("ggggg","getin");
                waiting = false;
                waitExp = "";

            }
            Log.i("wait exp",waitExp.toString());

            JSONObject attributes = new JSONObject();


            try {



                attributes.put("treatment_plan_position", childID);
                attributes.put("waiting", waiting);
                if (waitExp != ""){
                attributes.put("wait_expires", waitExp);
                }else{
                    attributes.put("wait_expires", JSONObject.NULL);
                }
                data.put("type", type);
                data.put("id", cowid);

                data.put("attributes", attributes);
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

        protected Boolean doInBackground(Void... params) {
            Boolean result = true;
            SharedPreferences sp = getSharedPreferences("com.example.leah.myapplication", Context.MODE_PRIVATE);
            String token = sp.getString("token", "NO_TOKEN_FOUND");
            Log.i("do in background", "yep, I get called");


            try {
                String postData = makeUpdateCowRequest();
                Log.i("update postdata: ", postData);

                URL url = new URL(request);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setInstanceFollowRedirects(false);
                //conn.setRequestProperty("X-HTTP-Method-Override", "PATCH");
                conn.setRequestMethod("PATCH");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", token);
                conn.setRequestProperty("charset", "utf-8");
                conn.setRequestProperty("content-Length", Integer.toString(postData.length()));
                conn.setUseCaches(false);

                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write(postData);
                wr.close();
                Log.i("update post data",postData);



                if (conn.getResponseCode() == HttpURLConnection.HTTP_CREATED || conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    Log.i("update success",conn.getResponseMessage());

                    //Object childStates = planBody.getJSONObject(currentState).get("children");
                    //Log.i("child states", childStates.toString());
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
            mGetPlanTask = null;

            if (success) {
                // finish();
                // on success, we want to redirect to main activity

                SharedPreferences sp = getSharedPreferences("com.example.leah.myapplication", Context.MODE_PRIVATE);
                int checkTime = sp.getInt("checkTime", 0);
                if (checkTime == 0){

                    finish();
                    startActivity(getIntent());

                }else {
                    startActivity(new Intent(NextStep.this, MainActivity.class));
                }
                Log.i("update postExecuted", "success");
            } else {
                Log.i("update postExecuted", "Not success");

            }
        }

        @Override
        protected void onCancelled() {
            mGetPlanTask = null;
        }



    }

}

