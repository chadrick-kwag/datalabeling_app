package com.example.chadrick.datalabeling;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private RequestQueue queue;
    private static String baseurl="http://13.124.175.119:4001";
    private final String TAG="datalabel";
    private TextView mTextView;
    private Button testbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        queue = Volley.newRequestQueue(this);
        mTextView = (TextView) findViewById(R.id.mtextView);
        testbtn = (Button) findViewById(R.id.testbutton);

        testbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject jsonObject = new JSONObject();
                try{
                    jsonObject.put("any","thing");
                }
                catch(JSONException e){
                    e.printStackTrace();
                }

                JsonObjectRequest jsonRequest = new JsonObjectRequest(baseurl+"/dslist",
                        jsonObject,
                        (JSONObject response) -> {
                            // parse the response and populate listview

                            try{
                                JSONArray jsonArray = response.getJSONArray("dslist");
                                mTextView.setText(jsonArray.toString());

                                for(int i=0;i<jsonArray.length();i++){
                                    Log.d(TAG,jsonArray.getJSONObject(i).get("name").toString());
                                }

                            }
                            catch (JSONException e){
                                e.printStackTrace();
                            }



                        },(error) ->{mTextView.setText("post failed to get dslist");}
                        );

                queue.add(jsonRequest);
            }
        });





    }

    @Override
    protected void onResume(){
        super.onResume();
        // check network connectivity
        ConnectivityManager cm =
                (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if(isConnected){
            Toast.makeText(getApplicationContext(),"network connected", Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(getApplicationContext(),"network not available", Toast.LENGTH_LONG).show();
        }



        //send request
        StringRequest stringRequest = new StringRequest(Request.Method.GET, baseurl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        mTextView.setText("Response is: "+ response.toString());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mTextView.setText("That didn't work!");
            }
        });
// Add the request to the RequestQueue.
        queue.add(stringRequest);

    }
}
