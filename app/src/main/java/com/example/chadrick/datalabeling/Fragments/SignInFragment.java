package com.example.chadrick.datalabeling.Fragments;

import android.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import com.example.chadrick.datalabeling.MainActivity;
import com.example.chadrick.datalabeling.R;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;

/**
 * Created by chadrick on 17. 10. 31.
 */

public class SignInFragment extends Fragment {

  private Button signInButton;
  private MainActivity mainActivity;
  private final String TAG = this.getClass().getSimpleName();

  public static int RC_SIGN_IN = 1;

  @Override
  public void onCreate(Bundle s) {
    super.onCreate(s);
    mainActivity = (MainActivity) getActivity();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {

    View root = inflater.inflate(R.layout.signinfrag_layout, container, false);
    signInButton = (Button) root.findViewById(R.id.sign_in_button);

    signInButton.setOnClickListener((view) -> signIn());

//    getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

    return root;

  }

  private void signIn() {
    Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mainActivity.googleApiClient);
    if (mainActivity.googleApiClient.isConnected()) {
      Log.d(TAG, "gac connected");
    } else {
      Log.d(TAG, "gac not connected");
    }
    Log.d(TAG, "trying to start google api activity");
    startActivityForResult(signInIntent, mainActivity.RC_SIGN_IN);
  }

  @Override
  public void onActivityResult(int requestCode, int resultcode, Intent data) {
    super.onActivityResult(requestCode, resultcode, data);

    if (requestCode == RC_SIGN_IN) {
      GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

      //handle signin result
      if (result.isSuccess()) {
        GoogleSignInAccount acct = result.getSignInAccount();
        Log.d(TAG, "sign in account =" + acct.getDisplayName());

        // if we succeed, then move on to dataselect
        gotoMainPortalFragment(acct.getDisplayName(),acct.getPhotoUrl());

      } else {
        Log.d(TAG, "sign in failed");
        Log.d(TAG, "onActivityResult: failed message=" + result.getStatus().getStatusMessage());
        Log.d(TAG, "onActivityResult: failed detail=" + result.getStatus().toString());
        Toast.makeText(getContext(), "fuck: sign in failed", Toast.LENGTH_SHORT).show();
      }
    }
  }

  private void gotoMainPortalFragment(String displayname, Uri photourl) {
    Log.d(TAG, "fuck: gotoMainPortalFragment: from signInFragment");
    MainPortalFragment fragment = new MainPortalFragment();
    Bundle passData = new Bundle();
    passData.putString("displayname", displayname);
    passData.putString("photourl", photourl.toString());
    fragment.setArguments(passData);

    getFragmentManager().beginTransaction().replace(R.id.fragmentcontainer, fragment).commit();
  }

}
