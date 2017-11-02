package com.example.chadrick.datalabeling;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.chadrick.datalabeling.Fragments.DatasetSelectFragment;
import com.example.chadrick.datalabeling.Fragments.NoInternetFragment;
import com.example.chadrick.datalabeling.Fragments.SignInFragment;
import com.example.chadrick.datalabeling.Fragments.SplashScreenFragment;

public class MainActivity extends AppCompatActivity {

  private RequestQueue queue;
  private static String baseurl = "http://13.124.175.119:4001";
  private final String TAG = "datalabel";
  private TextView mTextView;
  private Button testbtn;
  private FragmentManager fragmentManager;
  private FragmentTransaction fragmentTransaction;
  private boolean firstentry = true;
  public GoogleApiClient googleApiClient;

  public static int RC_SIGN_IN = 1;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    queue = Volley.newRequestQueue(this);
    setContentView(R.layout.activity_main);
    fragmentManager = getSupportFragmentManager();
    fragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {

      @Override
      public void onBackStackChanged() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager != null) {
          // see if there is a DatasetProgressFragment

          int lastindex = fragmentManager.getBackStackEntryCount();


        }
      }
    });

    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .build();

    googleApiClient = new GoogleApiClient.Builder(this)
        .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
          @Override
          public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            Log.d(TAG,"googleapiclient onconnectionfailed" + connectionResult);
            SignInFragment fragment = new SignInFragment();

            fragmentManager.beginTransaction().add(R.id.fragmentcontainer, fragment).commit();
          }
        })
        .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
          @Override
          public void onConnected(@Nullable Bundle bundle) {
            Log.d(TAG,"googleapiclient connected!!");

            DatasetSelectFragment fragment = new DatasetSelectFragment();
            fragmentManager.beginTransaction().add(R.id.fragmentcontainer, fragment).commit();

          }

          @Override
          public void onConnectionSuspended(int i) {

          }
        })
        .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
        .build();



    final Handler handler = new Handler();

    if (firstentry) {
      firstentry = false;
      SplashScreenFragment splashScreenFragment = new SplashScreenFragment();
      fragmentManager.beginTransaction().add(R.id.fragmentcontainer, splashScreenFragment).commit();
      handler.postDelayed(new Runnable() {
        @Override
        public void run() {


//          checkinternetandAction();
          Log.d(TAG, "end of handler delayed run");
        }
      }, 500);

    } else {
//      checkinternetandAction();
    }


  }

  @Override
  protected void onResume() {
    super.onResume();


  }

  @Override
  public void onStart(){
    super.onStart();

    googleApiClient.connect();
  }

  public RequestQueue getQueue() {
    return this.queue;
  }

  private void checkinternetandAction() {
    String url = "http://www.google.com";
    StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
        new Response.Listener<String>() {
          @Override
          public void onResponse(String response) {
            Log.d(TAG, "internet available");


//            DatasetSelectFragment fragment = new DatasetSelectFragment();

            // test signin frag



          }
        }, new Response.ErrorListener() {
      @Override
      public void onErrorResponse(VolleyError e) {
        Toast.makeText(getApplicationContext(), "no internet connection", Toast.LENGTH_SHORT).show();
        NoInternetFragment noInternetFragment = new NoInternetFragment();
        fragmentManager.beginTransaction().add(R.id.fragmentcontainer, noInternetFragment).commit();
      }
    });
    queue.add(stringRequest);

  }

//  @Override
//  public void onActivityResult(int requestCode, int resultCode, Intent data){
//    super.onActivityResult(requestCode,resultCode,data);
//    Log.d(TAG,"insdie activity result");
//
//    if(requestCode == RC_SIGN_IN){
//      GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
//
//      //handle signin result
//      if(result.isSuccess()){
//        GoogleSignInAccount acct = result.getSignInAccount();
//        Log.d(TAG,"sign in account ="+ acct.getDisplayName());
//      }
//      else{
//        Log.d(TAG,"sign in failed");
//      }
//    }
//  }


}
