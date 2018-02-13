package com.example.chadrick.datalabeling.Fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.chadrick.datalabeling.R

/**
 * Created by chadrick on 18. 2. 13.
 */

class SplashScreenFragment : Fragment(){
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.loadingscreen,container,false)
    }
}