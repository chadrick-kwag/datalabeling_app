package com.example.chadrick.datalabeling.CustomComponents;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * Created by chadrick on 17. 10. 19.
 */

public class PageFrameLayout extends FrameLayout {

  private TouchImageView mainIV;
  private MaskImageView maskIV;

  private final String TAG = this.getClass().getSimpleName();

  public PageFrameLayout(Context context) {
    super(context);
  }

  public PageFrameLayout(Context context, AttributeSet attrs){
    super(context,attrs);
  }


  // need to override ontouchdispatch
  @Override
  public boolean dispatchTouchEvent(MotionEvent e){
//    Log.d(TAG,"Pageframelayout x:"+e.getX()+",y:"+e.getY());
    MotionEvent e2 = MotionEvent.obtain(e);
//    Log.d(TAG,"Pageframelayout e2 x:"+e2.getX()+",y:"+e2.getY());

    mainIV.dispatchTouchEvent(e);
    maskIV.dispatchTouchEvent(e2);
    return true;
  }

  public void registerIVs(TouchImageView mainIV, MaskImageView maskIV){
    this.mainIV = mainIV;
    this.maskIV = maskIV;
  }


}
