package com.example.chadrick.datalabeling.Fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.example.chadrick.datalabeling.Models.requestqueueSingleton
import com.example.chadrick.datalabeling.R
import kotlinx.android.synthetic.main.nointernetfrag_layout.*

/**
 * Created by chadrick on 18. 2. 13.
 */

class NoInternetFragment : Fragment(){
    private val requestQueue = requestqueueSingleton.getQueue()

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.nointernetfrag_layout, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        retrybtn.setOnClickListener {
            val url = "http://www.google.com"
            val stringRequest = StringRequest(Request.Method.GET, url,
                    Response.Listener {


                        val fragment = MainPortalFragment()

                        fragmentManager.beginTransaction().add(R.id.fragmentcontainer, fragment, "mainportal").commit()
                    },
                    Response.ErrorListener {
                Toast.makeText(context.applicationContext, "still no internet", Toast.LENGTH_SHORT).show()
            })
            requestQueue.add(stringRequest) }
    }
}