package com.example.chadrick.datalabeling.CustomComponents

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent

/**
 * Created by chadrick on 18. 2. 13.
 */

class DataImageViewPager @JvmOverloads constructor(context: Context, attrs: AttributeSet)
    : ViewPager(context,attrs){
    var isSwipeEnabled = true

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {

        return if(isSwipeEnabled){
            super.onInterceptTouchEvent(ev)
        }
        else{
            false
        }

    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        return if(isSwipeEnabled) super.onTouchEvent(ev)
        else false
    }
}