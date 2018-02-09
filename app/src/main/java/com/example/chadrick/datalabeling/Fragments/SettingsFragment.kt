package com.example.chadrick.datalabeling.Fragments

import android.content.BroadcastReceiver
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.chadrick.datalabeling.MainActivity
import com.example.chadrick.datalabeling.Models.App
import com.example.chadrick.datalabeling.Models.SMitem
import com.example.chadrick.datalabeling.R
import com.example.chadrick.datalabeling.SettingsMenuAdapter
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



    lateinit var smAdapter: SettingsMenuAdapter

    private var initialized: Boolean = false

    val menulist: ArrayList<SMitem> = ArrayList()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("bitcoin","settingsfragment created")
        for( frag in fragmentManager.fragments){
            Log.d("bitcoin","print frags in settingsfragment frag = "+frag.toString())
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        if (!initialized) {
            menulist.add(SMitem(type = SMitem.TYPE_TOGGLE, title = "Night Mode"))
            menulist.add(SMitem(type = SMitem.TYPE_PLAIN, title = "Logout", clickable = true,
                    titleColor = Color.parseColor("#ff0000"),
                    clickAction = ::asksignout
            ))
            smAdapter = SettingsMenuAdapter(menulist)

            initialized = true
        }

        return inflater?.inflate(R.layout.settings_layout, container, false)

    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // recycler view manage
        settings_menu_rv.adapter = smAdapter
        settings_menu_rv.layoutManager = LinearLayoutManager(App.applicationContext(), LinearLayoutManager.VERTICAL, false)
    }

    private fun asksignout() {
        val alertbuilder = AlertDialog.Builder(activity)
        alertbuilder.setMessage("Sign out for real?")
                .setPositiveButton("Let's geddit", { x, y -> signout() })
                .setNegativeButton("Nope", { x, y -> })
        val alert = alertbuilder.create()
        alert.show()
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
        val mainportalfrag = fragmentManager.findFragmentByTag("mainportal")
        fragmentManager.beginTransaction().remove(mainportalfrag).commit()
        fragmentManager.beginTransaction().remove(this).commit()


    }

}