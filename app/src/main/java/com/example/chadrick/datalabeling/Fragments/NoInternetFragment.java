package com.example.chadrick.datalabeling.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.chadrick.datalabeling.MainActivity;
import com.example.chadrick.datalabeling.R;

/**
 * Created by chadrick on 17. 10. 15.
 */

public class NoInternetFragment extends Fragment {

    private Button retrybtn;
    private RequestQueue queue;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private final String TAG = this.getClass().getSimpleName();



    @Override
    public void onCreate(Bundle savedinstance) {
        super.onCreate(savedinstance);

        queue = ((MainActivity) getActivity()).getQueue();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.nointernetfrag_layout,container,false);

        retrybtn = (Button) root.findViewById(R.id.retrybtn);
        retrybtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(queue==null) return;

                String url = "http://www.google.com";
                StringRequest stringRequest = new StringRequest(Request.Method.GET,url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.d(TAG, "internet available");

                                fragmentManager = getFragmentManager();


                                DatasetSelectFragment fragment = new DatasetSelectFragment();
                                fragmentManager.beginTransaction().add(R.id.fragmentcontainer,fragment).commit();


                            }
                        }, new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError e){
                        Log.d(TAG,"still no internet connection");
                        Toast.makeText(getContext(),"still no internet",Toast.LENGTH_SHORT).show();
                    }
                });
                queue.add(stringRequest);

            }
        });

        return root;
    }

}
