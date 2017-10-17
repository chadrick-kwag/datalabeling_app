package com.example.chadrick.datalabeling;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by chadrick on 17. 10. 12.
 */

public class TouchImageView extends android.support.v7.widget.AppCompatImageView {
  Matrix matrix;
  private Matrix inverseMatrix;

  // We can be in one of these 3 states
  static final int NONE = 0;
  static final int DRAG = 1;
  static final int ZOOM = 2;
  int mode = NONE;

  // Remember some things for zooming
  PointF last = new PointF();
  PointF start = new PointF();
  float minScale = 1f;
  float maxScale = 5f;
  float[] m;

  int viewWidth, viewHeight;
  static final int CLICK = 3;
  float saveScale = 1f;
  protected float origWidth, origHeight;
  int oldMeasuredWidth, oldMeasuredHeight;

  ScaleGestureDetector mScaleDetector;

  Context context;

  private CustomViewPager customViewPager;
  private Callback drawBtnpressedcallback;
  private Canvas canvas;
  private final String TAG = this.getClass().getSimpleName();

  public TouchImageView(Context context) {
    super(context);
    sharedConstructing(context);
  }

  public TouchImageView(Context context, AttributeSet attrs) {
    super(context, attrs);
    sharedConstructing(context);
  }

  private void sharedConstructing(Context context) {
    super.setClickable(true);
    this.context = context;
    mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    matrix = new Matrix();
    m = new float[9];
    setImageMatrix(matrix);
    setScaleType(ScaleType.MATRIX);

    setOnTouchListener(new OnTouchListener() {

      @Override
      public boolean onTouch(View v, MotionEvent event) {
        mScaleDetector.onTouchEvent(event);
        PointF curr = new PointF(event.getX(), event.getY());


        // check if the drawBtn is pressed or not.
        boolean drawbtnpressed;
        if (drawBtnpressedcallback != null) {
          drawbtnpressed = drawBtnpressedcallback.getBoolean();
        } else {
          Log.d(TAG, "drawbtnpressedcallback is null");
          drawbtnpressed = false;
        }

        switch (event.getAction()) {
          case MotionEvent.ACTION_DOWN:
            if(drawbtnpressed){
              // calcuate the inversematrix
              inverseMatrix = new Matrix(matrix);
              inverseMatrix.invert(inverseMatrix);

              // then get the absolute x,y values
              event.transform(inverseMatrix);

              last.set(event.getX(),event.getY());
              start.set(last);
              mode = DRAG;

            }
            else{
              last.set(curr);
              start.set(last);
              mode = DRAG;
            }

            break;

          case MotionEvent.ACTION_MOVE:
            if (mode == DRAG) {

              if (drawbtnpressed) {
                //again we need the absolute values of x,y

                event.transform(inverseMatrix);

                last.set(event.getX(),event.getY());

              } else {
                float deltaX = curr.x - last.x;
                float deltaY = curr.y - last.y;
                float fixTransX = getFixDragTrans(deltaX, viewWidth,
                    origWidth * saveScale);
                float fixTransY = getFixDragTrans(deltaY, viewHeight,
                    origHeight * saveScale);
                matrix.postTranslate(fixTransX, fixTransY);
                fixTrans();
                last.set(curr.x, curr.y);

              }

            }
            break;

          case MotionEvent.ACTION_UP:
            mode = NONE;
            if (drawbtnpressed) {
              Log.d(TAG,"draw btn pressed and button up");
              event.transform(inverseMatrix);

              last.set(event.getX(),event.getY());

              // draw rectangle on the canvas
              // get the smaller and larger x,y
              int x1,x2,y1,y2;
              if(start.x>last.x){
                x1 = (int) last.x;
                x2 = (int) start.x;
              }
              else{
                x2 = (int) last.x;
                x1 = (int) start.x;
              }

              if(start.y>last.y){
                y1=(int)last.y;
                y2=(int)start.y;
              }
              else{
                y2=(int)last.y;
                y1=(int)start.y;
              }

              Rect rect = new Rect(x1,y1,x2,y2);
              Paint paint = new Paint();
              paint.setColor(Color.rgb(0,0,0));
              paint.setStrokeWidth(10);
              paint.setStyle(Paint.Style.STROKE);
              canvas.drawRect(rect,paint);


            } else {
              int xDiff = (int) Math.abs(curr.x - start.x);
              int yDiff = (int) Math.abs(curr.y - start.y);
              if (xDiff < CLICK && yDiff < CLICK)
                performClick();
            }

            break;

          case MotionEvent.ACTION_POINTER_UP:
            mode = NONE;
            break;
        }

        setImageMatrix(matrix);
        invalidate();
        return true; // indicate event was handled
      }

    });
  }

  public void setMaxZoom(float x) {
    maxScale = x;
  }

  private class ScaleListener extends
      ScaleGestureDetector.SimpleOnScaleGestureListener {
    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
      mode = ZOOM;
      return true;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
      float mScaleFactor = detector.getScaleFactor();
      float origScale = saveScale;
      saveScale *= mScaleFactor;
      if (saveScale > maxScale) {
        saveScale = maxScale;
        mScaleFactor = maxScale / origScale;
      } else if (saveScale < minScale) {
        saveScale = minScale;
        mScaleFactor = minScale / origScale;
      }

      // if saveScale is minScale, then we enable viewpager swipe
      // if not, (which would be when the slightset zoom in has occured,
      // then we disable the swipe of viewpager.

      if (customViewPager != null) {
        if (saveScale == minScale) {
          Log.i(TAG, "enable swipe");
          customViewPager.enableSwipe();
        } else {
          Log.i(TAG, "disable swipe");
          customViewPager.disableSwipe();
        }
      }

      if (origWidth * saveScale <= viewWidth
          || origHeight * saveScale <= viewHeight)
        matrix.postScale(mScaleFactor, mScaleFactor, viewWidth / 2,
            viewHeight / 2);
      else
        matrix.postScale(mScaleFactor, mScaleFactor,
            detector.getFocusX(), detector.getFocusY());

      fixTrans();
      return true;
    }
  }

  void fixTrans() {
    matrix.getValues(m);
    float transX = m[Matrix.MTRANS_X];
    float transY = m[Matrix.MTRANS_Y];

    float fixTransX = getFixTrans(transX, viewWidth, origWidth * saveScale);
    float fixTransY = getFixTrans(transY, viewHeight, origHeight
        * saveScale);

    if (fixTransX != 0 || fixTransY != 0)
      matrix.postTranslate(fixTransX, fixTransY);
  }

  float getFixTrans(float trans, float viewSize, float contentSize) {
    float minTrans, maxTrans;

    if (contentSize <= viewSize) {
      minTrans = 0;
      maxTrans = viewSize - contentSize;
    } else {
      minTrans = viewSize - contentSize;
      maxTrans = 0;
    }

    if (trans < minTrans)
      return -trans + minTrans;
    if (trans > maxTrans)
      return -trans + maxTrans;
    return 0;
  }

  float getFixDragTrans(float delta, float viewSize, float contentSize) {
    if (contentSize <= viewSize) {
      return 0;
    }
    return delta;
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    viewWidth = MeasureSpec.getSize(widthMeasureSpec);
    viewHeight = MeasureSpec.getSize(heightMeasureSpec);

    //
    // Rescales image on rotation
    //
    if (oldMeasuredHeight == viewWidth && oldMeasuredHeight == viewHeight
        || viewWidth == 0 || viewHeight == 0)
      return;
    oldMeasuredHeight = viewHeight;
    oldMeasuredWidth = viewWidth;

    if (saveScale == 1) {
      // Fit to screen.
      float scale;

      Drawable drawable = getDrawable();
      if (drawable == null || drawable.getIntrinsicWidth() == 0
          || drawable.getIntrinsicHeight() == 0)
        return;
      int bmWidth = drawable.getIntrinsicWidth();
      int bmHeight = drawable.getIntrinsicHeight();

      Log.d("bmSize", "bmWidth: " + bmWidth + " bmHeight : " + bmHeight);

      float scaleX = (float) viewWidth / (float) bmWidth;
      float scaleY = (float) viewHeight / (float) bmHeight;
      scale = Math.min(scaleX, scaleY);
      matrix.setScale(scale, scale);

      // Center the image
      float redundantYSpace = (float) viewHeight
          - (scale * (float) bmHeight);
      float redundantXSpace = (float) viewWidth
          - (scale * (float) bmWidth);
      redundantYSpace /= (float) 2;
      redundantXSpace /= (float) 2;

      matrix.postTranslate(redundantXSpace, redundantYSpace);

      origWidth = viewWidth - 2 * redundantXSpace;
      origHeight = viewHeight - 2 * redundantYSpace;
      setImageMatrix(matrix);
    }
    fixTrans();
  }

  public void setCustomViewPager(CustomViewPager input) {
    this.customViewPager = input;
  }

  public void setdrawBtnpressedcallback(Callback callback) {
    this.drawBtnpressedcallback = callback;
  }

  public void passCanvas(Canvas canvas){
    this.canvas = canvas;
  }
}
