package com.example.chadrick.datalabeling;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private boolean firstentry=true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        queue = Volley.newRequestQueue(this);
        setContentView(R.layout.activity_main);
        fragmentManager = getSupportFragmentManager();

    }

    @Override
    protected void onResume(){
        super.onResume();

        final Handler handler = new Handler();

        if(firstentry){
            firstentry=false;
            SplashScreenFragment splashScreenFragment = new SplashScreenFragment();
            fragmentManager.beginTransaction().add(R.id.fragmentcontainer,splashScreenFragment).commit();
            handler.postDelayed(new Runnable(){
                @Override
                public void run(){


                    checkinternetandAction();
                    Log.d(TAG,"end of handler delayed run");
                }
            },500);

        }else{
            checkinternetandAction();
        }
    }

    protected RequestQueue getQueue(){
        return this.queue;
    }

    private void checkinternetandAction(){
        String url = "http://www.google.com";
        StringRequest stringRequest = new StringRequest(Request.Method.GET,url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "internet available");



                        DatasetSelectFragment fragment = new DatasetSelectFragment();
                        fragmentManager.beginTransaction().add(R.id.fragmentcontainer,fragment).commit();


                    }
                }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError e){
                Toast.makeText(getApplicationContext(),"no internet connection",Toast.LENGTH_SHORT).show();
                NoInternetFragment noInternetFragment = new NoInternetFragment();
                fragmentManager.beginTransaction().add(R.id.fragmentcontainer,noInternetFragment).commit();
            }
        });
        queue.add(stringRequest);

    }


}
