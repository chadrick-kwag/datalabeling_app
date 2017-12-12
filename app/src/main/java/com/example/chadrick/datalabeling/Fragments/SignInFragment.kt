package com.example.chadrick.datalabeling.Fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.chadrick.datalabeling.MainActivity
import com.example.chadrick.datalabeling.R
import com.google.android.gms.auth.api.Auth
import kotlinx.android.synthetic.main.signinfrag_layout.*

/**
 * Created by chadrick on 17. 12. 12.
 */

class SignInFragment : Fragment() {


    companion object {
        val RC_SIGN_IN = 1
        val TAG = "SignInFragment"
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootview = inflater?.inflate(R.layout.signinfrag_layout, container, false)

        return rootview
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sign_in_button.setOnClickListener({ view -> signIn() })


    }

    private fun signIn() {
        val apiclient = (activity as MainActivity).googleApiClient
        val signinIntent = Auth.GoogleSignInApi.getSignInIntent(apiclient)
        if (apiclient.isConnected()) {
            Log.d(TAG, "gac connected")
        } else {
            Log.d(TAG, "gac not connected")
        }
        Log.d(TAG, "trying to start google api activity")

        activity.startActivityForResult(signinIntent, RC_SIGN_IN)
    }

}