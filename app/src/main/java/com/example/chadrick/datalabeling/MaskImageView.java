package com.example.chadrick.datalabeling;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by chadrick on 17. 10. 18.
 */

public class MaskImageView extends android.support.v7.widget.AppCompatImageView {

  private Context context;
  private Paint paint = new Paint();
  private int width, height;
  private Canvas canvas;
  private Bitmap bitmap;
  private Callback drawBtnpressedcallback;
  private CallbackWithRect RectReadyCallback;

  private PointF start, last;
  private boolean isdown = false;
  private boolean isMove = false;
  private boolean touchEnable = true;
  private TouchImageView mainiv;

  private final String TAG = this.getClass().getSimpleName();

  public MaskImageView(Context c) {
    super(c);
    sharedconstruct(c);
  }

  public MaskImageView(Context context, AttributeSet attrs) {
    super(context, attrs);
    sharedconstruct(context);
  }

  private void sharedconstruct(Context context) {
    this.context = context;


    paint.setColor(Color.rgb(255, 63, 20));
    paint.setStrokeWidth(5);
    paint.setStyle(Paint.Style.STROKE);

    start = new PointF();
    last = new PointF();

    setOnTouchListener(new OnTouchListener() {
      @Override
      public boolean onTouch(View view, MotionEvent motionEvent) {

//        Log.d(TAG,"ontouch in maskIV");

        // if touch is disabled, do nothing.
        if(!touchEnable){
          return true;
        }

        // what we need is get the x,y of event.
        // set start, last and draw on the canvas
        // but there is difference in the size of canvas and the IV's size
        // since we are inside the IV itself, I think we can access the size of IV much better

        // check if the drawBtn is pressed or not.
        boolean drawbtnpressed;
        if (drawBtnpressedcallback != null) {
          drawbtnpressed = drawBtnpressedcallback.getBoolean();
        } else {
          Log.d(TAG, "drawbtnpressedcallback is null");
          drawbtnpressed = false;
        }

        // deal with the event only when drawbtn is pressed.

        if (drawbtnpressed == false && isMove == false) {
          return false;
        }

        // prepare the adjusted x,y coordinates
        PointF curr = new PointF();
        curr.set(motionEvent.getX(),motionEvent.getY());


        switch (motionEvent.getAction()) {
          case MotionEvent.ACTION_DOWN:
            Log.d(TAG,"maskIV x:"+motionEvent.getX()+",y:"+motionEvent.getY());

//            Log.d(TAG,"action down detected");

            isdown = true;
            start.set(motionEvent.getX(), motionEvent.getY());
            last.set(start);
            break;
          case MotionEvent.ACTION_MOVE:
            isMove = true;

//            Log.d(TAG,"action move detected");



            if (drawbtnpressed == false) {
              // probably fall in here when the user released the drawbtn
              // while dragging the rectangle.
              // we just want to quit the drawing of the rectangle
              bitmap.eraseColor(Color.TRANSPARENT);

              break;
            }

            //remove the previous drawn rectangle
            Log.d(TAG,"maskIV erase in action_move");
            bitmap.eraseColor(Color.TRANSPARENT);

            last.set(curr.x, curr.y);

            // draw rectangle based on start, last
            Rect rect = Util.convertToRect(start, last);

            canvas.drawRect(rect, paint);

            break;
          case MotionEvent.ACTION_UP:

//            Log.d(TAG,"action up detected");
            isMove=false;
            isdown = false;

            // erase the rect draw in move case.
//            bitmap.eraseColor(Color.TRANSPARENT);
            last.set(curr.x, curr.y);
            // draw the rectangle. and then ask the user if this rectangle is going to be saved or not.
            Rect rect2 = Util.convertToRect(start,last);

//            invalidate();

            // call the RectReadycallback
//            RectReadyCallback.run();
            RectReadyCallback.doit(rect2);

            canvas.drawRect(rect2,paint);
            Log.d(TAG,"maskIV end of action_up");

            break;
          default:
        }

        invalidate();


        // indicate that touch event is still no handled. we need this event to go down
        // to the lower level.

        return true;
      }
    });
  }



  public void setdrawBtnpressedcallback(Callback callback) {
    this.drawBtnpressedcallback = callback;
  }


  public void passWH(int width, int height){

    Log.d(TAG,"inside passWH");
    this.width = width;
    this.height = height;

//    Log.d(TAG, "maskimageview width:" + width + ", height:" + height);

    // init the canvas and bitmap
    // the bitmap will have the same size as the IV
    bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    canvas = new Canvas(bitmap);

    this.setImageBitmap(bitmap);
//    Log.d(TAG, "passWH: finished setting the maskIV bitmap and canvas");

  }





  public void passRectReadyCallback(CallbackWithRect callback){
    this.RectReadyCallback = callback;
  }


  public void setTouchEnable(boolean value){
    this.touchEnable = value;
  }

  public void eraseall(){
    Log.d(TAG,"maskiv erase in eraseall method");
    bitmap.eraseColor(Color.TRANSPARENT);
  }

  public void forcedrawRect(Rect rect){
    canvas.drawRect(rect,paint);
    invalidate();
  }


}
