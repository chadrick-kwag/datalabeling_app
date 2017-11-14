package com.example.chadrick.datalabeling.Fragments

import android.content.BroadcastReceiver
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.chadrick.datalabeling.MainActivity
import com.example.chadrick.datalabeling.R
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInApi
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.Result
import kotlinx.android.synthetic.main.settings_layout.*

/**
 * Created by chadrick on 17. 11. 13.
 */

class SettingsFragment : Fragment() {

    companion object {
        fun newInstance(): SettingsFragment {
            return SettingsFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.settings_layout, container, false)

    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        test1.setText("added now")
        signoutbtn.setOnClickListener({ v ->
            val alertbuilder = AlertDialog.Builder(activity)
            alertbuilder.setMessage("Sign out for real?")
                    .setPositiveButton("Let's geddit", { x, y -> signout() })
                    .setNegativeButton("Nope", { x, y ->  })
            val alert = alertbuilder.create()
            alert.show()
        })
    }

    private fun signout() {
        val googleapiclient = (activity as MainActivity).getGoogleApiClient()
        val pendingresult = Auth.GoogleSignInApi.signOut(googleapiclient)
        pendingresult.setResultCallback { status ->
            if (status.isSuccess) {
                gotoSignInFragment()
            }
        }
    }

    private fun gotoSignInFragment() {
        fragmentManager.beginTransaction().replace(R.id.fragmentcontainer, SignInFragment()).commit()
    }

}