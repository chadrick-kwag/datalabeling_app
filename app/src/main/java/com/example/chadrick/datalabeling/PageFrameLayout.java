package com.example.chadrick.datalabeling;

import android.content.Context;
import android.text.method.Touch;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * Created by chadrick on 17. 10. 19.
 */

public class PageFrameLayout extends FrameLayout {

  private TouchImageView mainIV;
  private MaskImageView maskIV;

  public PageFrameLayout(Context context) {
    super(context);
  }

  public PageFrameLayout(Context context, AttributeSet attrs){
    super(context,attrs);
  }


  // need to override ontouchdispatch
  @Override
  public boolean dispatchTouchEvent(MotionEvent e){
    mainIV.dispatchTouchEvent(e);
    maskIV.dispatchTouchEvent(e);
    return true;
  }

  public void registerIVs(TouchImageView mainIV, MaskImageView maskIV){
    this.mainIV = mainIV;
    this.maskIV = maskIV;
  }


}
