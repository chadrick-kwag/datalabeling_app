package com.example.chadrick.datalabeling;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.chadrick.datalabeling.Fragments.MainPortalFragment;
import com.example.chadrick.datalabeling.Fragments.NoInternetFragment;
import com.example.chadrick.datalabeling.Fragments.SignInFragment;
import com.example.chadrick.datalabeling.Fragments.SplashScreenFragment;
import com.example.chadrick.datalabeling.Fragments.StartErrorFragment;
import com.example.chadrick.datalabeling.Models.ServerInfo;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
  private RequestQueue queue;
//  private static String baseurl = "http://13.124.175.119:4001";
  private final String TAG = "datalabel";
  private FragmentManager fragmentManager;
  private boolean firstentry = true;
  public GoogleApiClient googleApiClient;
  public static int RC_SIGN_IN = 1;
  private ServerInfo serverInfo = ServerInfo.Companion.getInstance();


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    queue = Volley.newRequestQueue(this);
    setContentView(R.layout.activity_main);

    // setup server info loading
    try{
      serverInfo.config(getAssets().open("serverinfo.txt"));
    }
    catch(IOException e){
      e.printStackTrace();
    }

    // testing serverinfo read
    Log.d(TAG, "onCreate: read from serverinfo="+serverInfo.serveraddress);



    fragmentManager = getSupportFragmentManager();
    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestIdToken(getString(R.string.server_client_id))
        .requestProfile()
        .build();
    googleApiClient = new GoogleApiClient.Builder(this)
        .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
          @Override
          public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            Log.d(TAG, "googleapiclient onconnectionfailed" + connectionResult);
            gotoSignInFragment();
          }
        })
        .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
          // when connected, do what?

          @Override
          public void onConnected(@Nullable Bundle bundle) {
            Log.d(TAG, "googleapiclient connected!!");
            return;
          }

          @Override
          public void onConnectionSuspended(int i) {
            Log.d(TAG, "googleapi connection suspended");
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

          initsequence();
        }
      }, 500);
    } else {
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
  }

  @Override
  public void onStart() {
    super.onStart();
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

  private void checksigninresult(GoogleSignInResult result) {
    if (result.isSuccess()) {
      Log.d(TAG, "googlesigninresult is success");
      Log.d(TAG, "idtoken=" + result.getSignInAccount().getIdToken());
      Log.d(TAG, "checksigninresult: photourl=" + result.getSignInAccount().getPhotoUrl());
      Log.d(TAG, "checksigninresult: display name" + result.getSignInAccount().getDisplayName());
      authwithserver(result);
    } else {
      Log.d(TAG, "googlesigninresult failed");
      Log.d(TAG, "fail detail: " + result.getStatus().getStatusMessage());
      Log.d(TAG, "fail status tostring: " + result.getStatus().toString());
      if (result.getStatus().getStatusCode() == GoogleSignInStatusCodes.SIGN_IN_REQUIRED) {
        Log.d(TAG, "checksigninresult: sign in required");
        gotoSignInFragment();
      } else {
        Log.d(TAG, "checksigninresult: some weird signin case");
        Toast.makeText(getApplicationContext(), "Sing in critical error. Quiting App", Toast.LENGTH_SHORT).show();
        finish();
      }
    }
  }

  private void authwithserver(GoogleSignInResult signInResult) {
    // send token to server
    String url = serverInfo.serveraddress + "/tokensignin";
    JSONObject bodyjson = new JSONObject();
    String idtoken = signInResult.getSignInAccount().getIdToken();
    try {
      bodyjson.put("idToken", idtoken);
    } catch (JSONException e) {
      e.printStackTrace();
      Log.d(TAG, "bodyjson create failed. abort sending jsonobject request");
      return;
    }
    JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, bodyjson,
        // response listener
        (JSONObject response) -> {
          JSONObject resjson = (JSONObject) response;
          Log.d(TAG, "response :" + resjson.toString());
          try {
            if (resjson.getBoolean("userverified")) {
              // if true, then we can move on to dataselectfragment
              Log.d(TAG, "fuck: goto mainportal from MainActivity authwithserver");
              gotoMainPortalFragment(signInResult.getSignInAccount().getDisplayName(), signInResult.getSignInAccount().getPhotoUrl());
            } else {
              Log.d(TAG, "user not verified by server. go to sign in page");
              gotoSignInFragment();
            }
          } catch (JSONException e) {
            e.printStackTrace();
          }
        },
        // error listener
        (VolleyError error) -> {
          error.printStackTrace();
          Log.d(TAG, "error response");
          gotoStartErrorFragment("Can't communicate with server");

        }
    );
    getQueue().add(jsonRequest);
    Log.d(TAG, "sent idToken to server");
  }

  private void gotoMainPortalFragment(String displayname, Uri photourl) {
    Log.d(TAG,"fuck: gotoMainPortal from MainActivity");
    MainPortalFragment fragment = new MainPortalFragment();
    Bundle passData = new Bundle();
    passData.putString("displayname", displayname);
    passData.putString("photourl", photourl.toString());
    fragment.setArguments(passData);
    getSupportFragmentManager().beginTransaction().add(R.id.fragmentcontainer, fragment).commit();
  }

  private void gotoSignInFragment() {
    SignInFragment fragment = new SignInFragment();
    getSupportFragmentManager().beginTransaction().add(R.id.fragmentcontainer, fragment).commit();
  }

  public void initsequence() {
    googleApiClient.connect();
    OptionalPendingResult<GoogleSignInResult> pendingResult = Auth.GoogleSignInApi.silentSignIn(googleApiClient);
    if (pendingResult.isDone()) {
      Log.d(TAG, "immediate result available");
      GoogleSignInResult googleSignInResult = pendingResult.get();
      checksigninresult(googleSignInResult);
    } else {
      Log.d(TAG, "no immediate result available");
      pendingResult.setResultCallback(new ResultCallback<GoogleSignInResult>() {
        @Override
        public void onResult(@NonNull GoogleSignInResult googleSignInResult) {
          Log.d(TAG, "pending result onresult callback");
          checksigninresult(googleSignInResult);
        }
      });
    }

  }

  private void gotoStartErrorFragment(String msg){
    StartErrorFragment fragment = new StartErrorFragment();
    Bundle data = new Bundle();
    data.putString("msg",msg);


    fragment.setArguments(data);
    getSupportFragmentManager().beginTransaction().add(R.id.fragmentcontainer,fragment).commit();

  }

  public GoogleApiClient getGoogleApiClient(){
    return this.googleApiClient;
  }

}
