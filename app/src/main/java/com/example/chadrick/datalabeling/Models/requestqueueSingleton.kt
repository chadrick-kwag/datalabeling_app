package com.example.chadrick.datalabeling.Models

import android.content.Context
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

/**
 * Created by chadrick on 17. 12. 15.
 */

class requestqueueSingleton(context: Context) {
    // make sure that this context is application context

    val mcontext = context
    var req_queue = Volley.newRequestQueue(context)

    companion object {



        @Volatile private var INSTANCE: requestqueueSingleton? = null

        fun getInstance(context: Context?): requestqueueSingleton? =
                context?.let { INSTANCE ?: synchronized(this) {
                    INSTANCE ?: requestqueueSingleton(context).also { INSTANCE = it; INSTANCE}

                } }
        fun getInstance():requestqueueSingleton? = INSTANCE
        fun getQueue():RequestQueue = INSTANCE!!.req_queue

    }







}