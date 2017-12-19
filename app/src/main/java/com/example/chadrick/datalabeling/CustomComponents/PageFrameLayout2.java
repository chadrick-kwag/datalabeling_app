package com.example.chadrick.datalabeling.CustomComponents;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import java.lang.ref.WeakReference;

/**
 * Created by chadrick on 17. 11. 4.
 */

public class PageFrameLayout2 extends FrameLayout {
  private BgImageView baseIV;
  private RectDrawImageView maskIV;
  private SavedRectsImageView rectIV;


  private final String TAG = this.getClass().getSimpleName();

  public PageFrameLayout2(Context context) {
    super(new WeakReference<>(context).get());
  }

  public PageFrameLayout2(Context context, AttributeSet attrs){
    super( new WeakReference<>(context).get(),attrs);
  }


  // need to override ontouchdispatch
  @Override
  public boolean dispatchTouchEvent(MotionEvent e){
//    Log.d(TAG,"Pageframelayout x:"+e.getX()+",y:"+e.getY());
    MotionEvent e2 = MotionEvent.obtain(e);
    MotionEvent e3 = MotionEvent.obtain(e);


//    Log.d(TAG,"Pageframelayout e2 x:"+e2.getX()+",y:"+e2.getY());

    baseIV.dispatchTouchEvent(e);
    rectIV.dispatchTouchEvent(e2);
    maskIV.dispatchTouchEvent(e3);

    // this order is identical to which IV's touch listener is triggered
    // the rectIV should be called before maskIV
    return true;
  }

  public void registerIVs(BgImageView mainIV, RectDrawImageView maskIV, SavedRectsImageView rectIV){
    this.baseIV = mainIV;
    this.maskIV = maskIV;
    this.rectIV = rectIV;
  }
}
