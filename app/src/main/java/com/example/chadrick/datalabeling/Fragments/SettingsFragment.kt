package com.example.chadrick.datalabeling.Fragments

import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.chadrick.datalabeling.R
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
        signoutbtn.setOnClickListener({v->
            val alertbuilder = AlertDialog.Builder(activity)
            alertbuilder.setMessage("some message")
                    .setPositiveButton("positive",{x,y -> Log.d("chadrick","positive message")})
                    .setNegativeButton("negative",{x,y -> Log.d("chadrick","negative message")})
            val alert = alertbuilder.create()
            alert.show()
        })
    }



}