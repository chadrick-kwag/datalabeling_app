package com.example.chadrick.datalabeling.Fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.chadrick.datalabeling.MainActivity
import com.example.chadrick.datalabeling.R
import kotlinx.android.synthetic.main.starterrorfragment_layout.*

/**
 * Created by chadrick on 17. 11. 13.
 */

class StartErrorFragment : Fragment() {

    companion object {
        fun newInstance(): StartErrorFragment {
            return StartErrorFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.starterrorfragment_layout, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val errmsg = arguments.getString("msg")
        errormessage.setText(errmsg)

        retrybtn.setOnClickListener({ v ->
            val mainactivity = activity as MainActivity
            mainactivity.initsequence()
        })


    }
}