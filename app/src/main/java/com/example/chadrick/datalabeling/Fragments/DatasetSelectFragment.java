package com.example.chadrick.datalabeling.Fragments;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.chadrick.datalabeling.DSAdapter;
import com.example.chadrick.datalabeling.Models.DataSet;
import com.example.chadrick.datalabeling.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chadrick on 17. 10. 8.
 */

public class DatasetSelectFragment extends Fragment {

  private RecyclerView dsrcv;
  private DSAdapter mDSAdapter;
  private final String TAG = "DatasetSelectFrag";
  private static String baseurl = "http://13.124.175.119:4001";
  private RequestQueue queue;
  private List<DataSet> dslist;
  private SwipeRefreshLayout swipeRefreshLayout;
  private ListView menulist;

  private String username;
  private String photourl;

  private TextView profilename;
  private ImageView profileicon;

  @Override
  public void onCreate(Bundle savedinstance) {

    super.onCreate(savedinstance);
    queue = Volley.newRequestQueue(getContext());
    dslist = new ArrayList<>();
    mDSAdapter = new DSAdapter(getContext(), dslist);

    Log.d(TAG, "oncreate finished");

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    Log.d(TAG, "oncreateview start");

//    username = getArguments().getString("displayname");
//    photourl = getArguments().getString("photourl");

    View v = inflater.inflate(R.layout.datasetselectfragment_layout, container, false);
    // Inflate the layout for this fragment

    dsrcv = v.findViewById(R.id.dslistrv);
    dsrcv.setAdapter(mDSAdapter);
    RecyclerView.LayoutManager mDSLayoutManager = new LinearLayoutManager(getContext());
    dsrcv.setLayoutManager(mDSLayoutManager);

    swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swiperefreshlayout);
    swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
      @Override
      public void onRefresh() {
        Log.d(TAG, "refreshing recycler view");
        populateRecyclerView(true);
      }
    });

//    menulist = (ListView) v.findViewById(R.id.menulist);
//    ArrayList<String> menuitems = new ArrayList<String>();
//    menuitems.add("Main");
//    menuitems.add("Settings");
//    menulist.setAdapter(new ArrayAdapter<String>(getContext(),R.layout.menulist_item_layout,menuitems));
//    menulist.setOnItemClickListener((parent,view, position,id)->{
//      String itemtext = (String) parent.getItemAtPosition(position);
//      Log.d(TAG, "onCreateView: "+itemtext+" is selected");
//      if(itemtext.equals("Settings")){
//        SettingsFragment fragment = new SettingsFragment();
//        getFragmentManager().beginTransaction().add(R.id.dataselect_fragmentcontainer, fragment).commit();
//      }
//      else if(itemtext.equals("Main")){
//
//      }
//
//    });
//
//    profilename = (TextView) v.findViewById(R.id.profile_name);
//    profileicon = (ImageView) v.findViewById(R.id.profile_icon);
//
//    profilename.setText(username);
//
//    ImageRequest imageRequest = new ImageRequest(
//        photourl,
//        (Bitmap bitmap)->{
//          profileicon.setImageBitmap(bitmap);
//
//        },
//        0,
//        0,
//        ImageView.ScaleType.CENTER_CROP,
//        Bitmap.Config.RGB_565,
//        (err)->{
//          err.toString();
//          Log.d(TAG, "onCreateView: error while fetching profile image");
//        }
//    );
//
//    RequestQueue requestQueue = Volley.newRequestQueue(getContext());
//    requestQueue.add(imageRequest);
//    Log.d(TAG, "onCreateView: imagerequest added");



    Log.d(TAG, "oncreateview end");

    return v;
  }

  @Override
  public void onResume() {
    super.onResume();
    populateRecyclerView(false);

  }


  private void populateRecyclerView(boolean calledfromRefresh) {
    // fetch ds data from server
    //clear dslist
    dslist.clear();

    JSONObject jsonObject = new JSONObject();
    try {
      jsonObject.put("ds_request_id", "anything");
    } catch (JSONException e) {
      e.printStackTrace();
    }

    JsonObjectRequest jsonRequest = new JsonObjectRequest(baseurl + "/dslist",
        jsonObject,
        (JSONObject response) -> {
          // parse the response and populate listview

          try {
            JSONArray jsonArray = response.getJSONArray("dslist");


            for (int i = 0; i < jsonArray.length(); i++) {
              Log.d(TAG, jsonArray.getJSONObject(i).get("name").toString());
              JSONObject object = jsonArray.getJSONObject(i);


              //check for storage
              Boolean direxist = false;
              String filename = object.get("id").toString();
              File file = new File(getContext().getFilesDir(), filename);

              if (file.exists()) {
                Log.d(TAG, filename + "exists");
                direxist = true;
              } else {
                Log.d(TAG, filename + "not exists");
              }

              DataSet ds = new DataSet(object.getInt("id"), object.getString("name"), direxist, getContext().getFilesDir().toString());

              dslist.add(ds);
            }

          } catch (JSONException e) {
            e.printStackTrace();
          }

          mDSAdapter.notifyDataSetChanged();
          Log.d(TAG, "response finished");

          //
          if (calledfromRefresh) {
            swipeRefreshLayout.setRefreshing(false);
          }


        }, (error) -> {
      Log.d(TAG, "post failed to get dslist");
      Toast.makeText(getContext(), "failed to fetch dslist", Toast.LENGTH_SHORT).show();
      if (calledfromRefresh) {
        swipeRefreshLayout.setRefreshing(false);
      }
    }
    );

    queue.add(jsonRequest);
    Log.d(TAG, "fetch data finished");
  }

}
