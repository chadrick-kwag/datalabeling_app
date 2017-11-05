package com.example.chadrick.datalabeling.CustomComponents;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.example.chadrick.datalabeling.Callback;
import com.example.chadrick.datalabeling.CustomViewPager;
import com.example.chadrick.datalabeling.Models.PageInfoSet;
import com.example.chadrick.datalabeling.Util;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;


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
  private Paint paint;
  private Paint selectedpaint;
  private Paint transparentpaint;
  private boolean touchEnable = true;
  private Rect savedrect;

  private PageInfoSet pageInfoSet;

  private Consumer<Rect> addRectCallback;
  private Runnable saveLabelCallback;
  private Function<Point, Boolean> checkRectSelectCallback;

  private Point sendpoint = new Point();

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

    // setup paint
    paint = new Paint();
    paint.setColor(Color.rgb(255, 63, 20));
    paint.setStrokeWidth(5);
    paint.setStyle(Paint.Style.STROKE);

    // setup selected paint
    selectedpaint = new Paint();
    selectedpaint.setColor(Color.rgb(0, 255, 0));
    selectedpaint.setStrokeWidth(5);
    selectedpaint.setStyle(Style.STROKE);

    // setup transparent paint
    transparentpaint = new Paint();
    transparentpaint.setColor(Color.TRANSPARENT);
    transparentpaint.setStrokeWidth(5);
    transparentpaint.setStyle(Style.STROKE);

    setOnTouchListener(new OnTouchListener() {

      @Override
      public boolean onTouch(View v, MotionEvent event) {

//        Log.d(TAG,"touch handler of touchIV");

        // if touch is disabled, do nothing.
        if (!touchEnable) {
          return true;
        }

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

            Log.d(TAG, "mainIV x:" + event.getX() + ", y:" + event.getY());

            if (drawbtnpressed) {

              // calcuate the inversematrix
              inverseMatrix = new Matrix(matrix);
              inverseMatrix.invert(inverseMatrix);

              // then get the absolute x,y values
              event.transform(inverseMatrix);

// when draw btn is not pressed save the last,start with current scale.
              last.set(event.getX(), event.getY());
              start.set(last);

              mode = DRAG;
            } else {

              last.set(event.getX(), event.getY());
              start.set(last);
              mode = DRAG;

              // we still need the fullscalestart saved so that we can use this
              // in ACTION_UP in order to pass on to check selected rect.

              inverseMatrix = new Matrix(matrix);
              inverseMatrix.invert(inverseMatrix);

              // then get the absolute x,y values
              event.transform(inverseMatrix);

              sendpoint.set((int) event.getX(), (int) event.getY());

            }

            break;

          case MotionEvent.ACTION_MOVE:
            if (mode == DRAG) {

              if (drawbtnpressed) {
                // we are not drawing temp rects here. so do nothing.

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

              event.transform(inverseMatrix);
              last.set(event.getX(), event.getY());

              // get the final rect
              // but don't draw it yet. just keep it.
              // the drawing will be done when the user clicks yes.
              savedrect = Util.convertToRect(start, last);
              Log.d(TAG, "onTouch: savedrect updated");

            } else {

              int xDiff = (int) Math.abs(curr.x - start.x);
              int yDiff = (int) Math.abs(curr.y - start.y);
              if (xDiff < CLICK && yDiff < CLICK) {

                Log.d(TAG, "onTouch: inside click case");
                Log.d(TAG, "onTouch: sendpoint x=" + sendpoint.x + ", y=" + sendpoint.y);
                boolean isRectSelectExist = checkRectSelectCallback.apply(sendpoint);
                if (isRectSelectExist) {
                  Log.d(TAG, "onTouch: rect select exist");
                } else {
                  Log.d(TAG, "onTouch: rect select not exist");
                }
                performClick();
              }


            }

            break;

          case MotionEvent.ACTION_POINTER_UP:
            mode = NONE;
            break;
          default:
            Log.d(TAG, "touch event falling to default");
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
          || origHeight * saveScale <= viewHeight) {
        matrix.postScale(mScaleFactor, mScaleFactor, viewWidth / 2,
            viewHeight / 2);
      } else {
        matrix.postScale(mScaleFactor, mScaleFactor,
            detector.getFocusX(), detector.getFocusY());
      }

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

    if (fixTransX != 0 || fixTransY != 0) {
      matrix.postTranslate(fixTransX, fixTransY);
    }
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

    if (trans < minTrans) {
      return -trans + minTrans;
    }
    if (trans > maxTrans) {
      return -trans + maxTrans;
    }
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
        || viewWidth == 0 || viewHeight == 0) {
      return;
    }
    oldMeasuredHeight = viewHeight;
    oldMeasuredWidth = viewWidth;

    if (saveScale == 1) {
      // Fit to screen.
      float scale;

      Drawable drawable = getDrawable();
      if (drawable == null || drawable.getIntrinsicWidth() == 0
          || drawable.getIntrinsicHeight() == 0) {
        return;
      }
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

  public void passCanvas(Canvas canvas) {
    this.canvas = canvas;
  }


  public void setTouchEnable(boolean value) {
    this.touchEnable = value;
  }

  public void passPageInfoSet(PageInfoSet pageInfoSet) {
    this.pageInfoSet = pageInfoSet;
  }

  public void passAddRectCallback(Consumer<Rect> addRectCallback) {
    this.addRectCallback = addRectCallback;
  }

  public void passSaveLabelCallback(Runnable saveLabelCallback) {
    this.saveLabelCallback = saveLabelCallback;
  }

  public void passCheckRectSelectCallback(Function<Point, Boolean> checkRectSelectCallback) {
    this.checkRectSelectCallback = checkRectSelectCallback;
  }

  public void drawRect() {
    canvas.drawRect(savedrect, paint);

    // send the savedrect to the pageinfoset so that it can update the rectarraylist
    // and save the updated info to labelfile

//    pageInfoSet.addRect(savedrect);
//    pageInfoSet.saveLabelFile();

    addRectCallback.accept(savedrect);
    saveLabelCallback.run();

    Log.d(TAG, "call savedlabelfile from mainIV");

  }

  public void drawSelectedRect(Rect rect) {
    canvas.drawRect(rect, selectedpaint);
    Log.d(TAG, "drawSelectedRect: finished");
  }


  public void drawUnselectedRect(Rect rect) {
    canvas.drawRect(rect, paint);
    Log.d(TAG, "drawUnselectedRect: finished");
  }

  public void drawDeleteRect(Rect rect) {
    canvas.drawRect(rect, transparentpaint);
    Log.d(TAG, "drawDeleteRect: finished");
  }


}
