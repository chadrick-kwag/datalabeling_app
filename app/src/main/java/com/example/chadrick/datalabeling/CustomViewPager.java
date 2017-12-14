package com.example.chadrick.datalabeling;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import java.lang.ref.WeakReference;

/**
 * Created by chadrick on 17. 10. 14.
 */

public class CustomViewPager extends ViewPager {

    private boolean swipeEnabled=true;
    private final String TAG = this.getClass().getSimpleName();

    public CustomViewPager(Context context){
        super(new WeakReference<>(context).get());

    }

    public CustomViewPager(Context context, AttributeSet attrs){
        super(new WeakReference<>(context).get(), attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e){
        if(swipeEnabled){
            return super.onInterceptTouchEvent(e);
        }
        else{
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e){
        if(swipeEnabled){
            return super.onTouchEvent(e);
        }
        else{
            return false;
        }

    }

    public void enableSwipe(){
        Log.i(TAG,"swipe enabled");
        this.swipeEnabled = true;
    }

    public void disableSwipe(){
        Log.i(TAG,"swipe disabled");
        this.swipeEnabled = false;
    }




}
