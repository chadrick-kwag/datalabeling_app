package com.example.chadrick.datalabeling.Models;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import com.example.chadrick.datalabeling.Callback;
import com.example.chadrick.datalabeling.CallbackWithRect;
import com.example.chadrick.datalabeling.CustomComponents.BasicZoomImageView;
import com.example.chadrick.datalabeling.CustomComponents.MaskImageView;
import com.example.chadrick.datalabeling.CustomComponents.PageFrameLayout2;
import com.example.chadrick.datalabeling.CustomComponents.TouchImageView;
import com.example.chadrick.datalabeling.CustomViewPager;
import com.example.chadrick.datalabeling.R;
import com.example.chadrick.datalabeling.Util;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by chadrick on 17. 11. 4.
 */

public class LabelDrawPad {

  private LayoutInflater inflater;
  private ViewGroup container;
  private int position;
  private View rootview;

  private BasicZoomImageView baseIV;
  private TouchImageView rectIV;
  private MaskImageView drawIV;
  private Callback drawBtnpressedcallback;
  private CallbackWithRect maskRectReadyCallback;

  private CustomViewPager customViewPager;
  private Paint mainCanvasPaint;

  private File imageFile, labelFile;
  private Bitmap baseBitmap, rectBitmap;
  private Canvas rectCanvas;
  private Rect selectedRect;
  private Runnable rectSelectedCallback;
  private Runnable hideDeleteBtnCallback;

  private ArrayList<Rect> rectArrayList = new ArrayList<Rect>();

  private static int RECT_SELECT_AREA_PADDING = 50;


  private final String TAG = this.getClass().getSimpleName();


  private void initElements() {
    rootview = inflater.inflate(R.layout.labeldrawpad_layout, container,
        false);

    baseIV = (BasicZoomImageView) rootview.findViewById(R.id.baseiv);
    rectIV = (TouchImageView) rootview.findViewById(R.id.rectiv);
    drawIV = (MaskImageView) rootview.findViewById(R.id.drawiv);

    // need to register the IVs so that it can receive touch events
    PageFrameLayout2 pageframelayout2 = (PageFrameLayout2) rootview
        .findViewById(R.id.pageframelayout2);
    pageframelayout2.registerIVs(baseIV, drawIV, rectIV);

    // create paint for maincanvas
    mainCanvasPaint = new Paint();
    mainCanvasPaint.setColor(Color.rgb(255, 63, 20));
    mainCanvasPaint.setStrokeWidth(5);
    mainCanvasPaint.setStyle(Paint.Style.STROKE);

    // dynamic width/height measure for drawIV
    ViewTreeObserver viewTreeObserver = drawIV.getViewTreeObserver();

    viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
      @Override
      public void onGlobalLayout() {

        drawIV.passWH(drawIV.getWidth(), drawIV.getHeight());

        drawIV.getViewTreeObserver().removeOnGlobalLayoutListener(this);

      }
    });

    // create bitmap with imageFile and set up baseCanvas
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
    options.inMutable = true;

    Log.d(TAG, "initElements: imageFile=" + imageFile.toString());
    Log.d(TAG, "initElements: imageFile exist=" + imageFile.exists());
    Log.d(TAG, "initElements: imageFile size=" + imageFile.length());
    Bitmap originalbitmap = BitmapFactory.decodeFile(imageFile.toString(), options);
    baseBitmap = Bitmap.createBitmap(originalbitmap.getWidth(), originalbitmap.getHeight(),
        Bitmap.Config.ARGB_8888);
    Canvas basecanvas = new Canvas(baseBitmap);
    basecanvas.drawBitmap(originalbitmap, 0, 0, null);

    // create blank bitmap
    rectBitmap = Bitmap
        .createBitmap(originalbitmap.getWidth(), originalbitmap.getHeight(), Config.ARGB_8888);
    rectCanvas = new Canvas(rectBitmap);

    // set baseIV
    baseIV.setImageBitmap(baseBitmap);
    baseIV.setdrawBtnpressedcallback(drawBtnpressedcallback);

    // set rectIV
    rectIV.setImageBitmap(rectBitmap);
    rectIV.passCanvas(rectCanvas);
    rectIV.setCustomViewPager(
        customViewPager); // need this since swipe dis/enable will be controlled here
    rectIV.setdrawBtnpressedcallback(drawBtnpressedcallback);
    rectIV.passAddRectCallback((rect) -> {
      rectArrayList.add(rect);
    });
    rectIV.passSaveLabelCallback(this::saveLabelFile);
    rectIV.passCheckRectSelectCallback(this::checkRectSelect);
    rectIV.passUnselectAnyIfExist(this::unselectAnyIfExist);

    // drawIV has its own canvas
    drawIV.setdrawBtnpressedcallback(drawBtnpressedcallback);
    drawIV.passRectReadyCallback(maskRectReadyCallback);

    // generate label json file based on image file
    String imagefilename = Util.getOnlyFilename(imageFile.getName());
    String parentpath = imageFile.getParent();

    String printstr = parentpath + File.separator + imagefilename + ".json";
    Log.d(TAG, "json file : " + printstr);

    labelFile = new File(printstr);

    // check if labelfile exist. if exist, read it and draw them. if not exist, then do nothing.
    if (!labelFile.exists()) {

      // when json file doesn't exist
      Log.d(TAG, " label file(" + labelFile.toString() + "), doesn't exist");

    } else {
      // if json file exists, then read it into a json object
      Log.d(TAG, "json file exist. start parsing.");

      Log.d(TAG, "labelfile size" + labelFile.length());

      StringBuilder readtext = new StringBuilder();
      try {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(labelFile));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
          readtext.append(line);
        }

      } catch (IOException e) {
        e.printStackTrace();

      }

      String readresult = readtext.toString();
      Log.d(TAG, "read result: " + readresult);

      try {
        JSONObject parsed = new JSONObject(readresult);
        String bitmapfilename = parsed.getString("img");
        JSONArray rects = parsed.getJSONArray("rects");

        // for all the rects in the jsonfile, parse it and save it in the rectarraylist.
        if (rects.length() > 0) {
          // rects item exists. transfer them to rectarray
          for (int i = 0; i < rects.length(); i++) {
            JSONObject rectitem = rects.getJSONObject(i);
            int x1 = rectitem.getInt("x1");
            int y1 = rectitem.getInt("y1");
            int x2 = rectitem.getInt("x2");
            int y2 = rectitem.getInt("y2");

            Rect rectfromjson = new Rect(x1, y1, x2, y2);
            rectArrayList.add(rectfromjson);

          }
        }

      } catch (JSONException e) {
        e.printStackTrace();
      }

      // parsing the jsonfile finished.
      // now draw each rect in the maincanvas

      for (int i = 0; i < rectArrayList.size(); i++) {
        Rect rectToDraw = rectArrayList.get(i);
        rectCanvas.drawRect(rectToDraw, mainCanvasPaint);
      }

      // invalidate to make sure that the new drawing are displayed
      rectIV.invalidate();


    }

    Log.d(TAG, "initElements: finished");
  }

  // this is required to return the view to instantiating in fullimageadapter
  public View getRootview() {
    Log.d(TAG, "getRootview: returning rootview");
    return rootview;
  }

  private LabelDrawPad(LabelDrawPadBuilder builder) {
    this.inflater = builder.inflater;
    this.container = builder.container;
    this.position = builder.position;
    this.drawBtnpressedcallback = builder.drawBtnpressedcallback;
    this.maskRectReadyCallback = builder.maskRectReadyCallback;
    this.imageFile = builder.imageFile;
    this.customViewPager = builder.customViewPager;
    this.rectSelectedCallback = builder.rectSelectedCallback;
    this.hideDeleteBtnCallback = builder.hideDeleteBtnCallback;

    initElements();

  }


  public boolean saveLabelFile() {
    // when called, rewrite the labelfile with current info

    // first create a jsonobject and populate with current info
    JSONObject newRoot = new JSONObject();

    try {
      newRoot.put("img", imageFile.getName());

      // create JSONarray object that contains all the current rects
      JSONArray rectarray = new JSONArray();

      for (int i = 0; i < rectArrayList.size(); i++) {
        Rect rect = rectArrayList.get(i);
        JSONObject jsonrect = new JSONObject();
        jsonrect.put("x1", rect.left);
        jsonrect.put("y1", rect.top);
        jsonrect.put("x2", rect.right);
        jsonrect.put("y2", rect.bottom);

        rectarray.put(jsonrect);
      }

      newRoot.put("rects", rectarray);

    } catch (JSONException e) {
      e.printStackTrace();
      return false;
    }

    // newroot is ready. write it to labelFile

    try {
      BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(labelFile));
      bufferedWriter.write(newRoot.toString());
      bufferedWriter.flush();
      bufferedWriter.close();
      Log.d(TAG, "newly written contents:" + newRoot.toString());
      Log.d(TAG, "new labelfile size: " + labelFile.length());
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
    // finished overwriting the labelfile
    // job finished
    // return true since we have successfully written it.
    return true;

  }


  public void enableTouches() {
    baseIV.setTouchEnable(true);
    rectIV.setTouchEnable(true);
    drawIV.setTouchEnable(true);
  }

  public void disableTouches() {
    baseIV.setTouchEnable(false);
    rectIV.setTouchEnable(false);
    drawIV.setTouchEnable(false);
  }

  private boolean checkRectSelect(Point point) {
    // check with all rects. the first hit will be set as selected and
    // this method will return true
    // if there are no hits, then return false

    int touch_x = point.x;
    int touch_y = point.y;

    Log.d(TAG, "checkRectSelect: received x=" + touch_x + ", y=" + touch_y);

    for (Rect rect : rectArrayList) {

      Rect biggerRect = new Rect(rect.left - RECT_SELECT_AREA_PADDING,
          rect.top - RECT_SELECT_AREA_PADDING,
          rect.right + RECT_SELECT_AREA_PADDING,
          rect.bottom + RECT_SELECT_AREA_PADDING);

      Rect smallerRect = new Rect(rect.left + RECT_SELECT_AREA_PADDING,
          rect.top + RECT_SELECT_AREA_PADDING,
          rect.right - RECT_SELECT_AREA_PADDING,
          rect.bottom - RECT_SELECT_AREA_PADDING);

      Log.d(TAG,
          "checkRectSelect: biggerRect, left=" + biggerRect.left + ", top=" + biggerRect.top + ","
              + "right=" + biggerRect.right + ", bottom=" + biggerRect.bottom);

      Log.d(TAG,
          "checkRectSelect: smallerREct, left=" + smallerRect.left + ", top=" + smallerRect.top +
              ", right=" + smallerRect.right + ", bottom=" + smallerRect.bottom);

      // assume that there are no conflicts with the sizes of the bigger and smaller rect

      boolean biggercontain = false;
      boolean smallercontain = false;

      if (biggerRect.contains(touch_x, touch_y)) {
        biggercontain = true;
      }

      if (smallerRect.contains(touch_x, touch_y)) {
        smallercontain = true;
      }

      Log.d(TAG,
          "checkRectSelect: biggercontain=" + biggercontain + ", smallercontain=" + smallercontain);

      if (biggercontain == true && smallercontain == false) {
        Log.d(TAG, "checkRectSelect: select rect hit found");

        // okay so a hit was found, and we know which rectangle it is.
        // we need to redraw it, and it will eventually be done with
        // rectIV
        // the rectangle of interest is already drawn.
        // instead of redrawing the whole thing(including othe rectangles),
        // why not just overdraw the rectangle of interest with anther paint?

        // first draw unselected rect of the previous existing selectedRect
        if (selectedRect != null) {
          rectIV.drawUnselectedRect(selectedRect);
        }

        selectedRect = rect;

        rectIV.drawSelectedRect(selectedRect);

        // display delete btn
        rectSelectedCallback.run();

        return true;
      }


    }

    return false;
  }

  public void drawRect() {
    rectIV.drawRect();
  }

  public void eraseDrawnRect() {
    drawIV.eraseall();
  }


  /**
   * this will delete the selectedRect from the list
   * and redraw canvas without that rect
   */
  public void deleteSelectedRect() {
    // delete the selectedrect

    if (selectedRect == null) {
      return;
    }

//    rectIV.drawDeleteRect(selectedRect);

    // remove it from rectarray
    Log.d(TAG, "deleteSelectedRect: length of rectarray before remove=" + rectArrayList.size());
    rectArrayList.remove(selectedRect);
    Log.d(TAG, "deleteSelectedRect: length of rectarray after remove=" + rectArrayList.size());

    // nullify the selectedrect variable
    selectedRect = null;

//    // clear the rectIV, and redraw all rects in rectarray
//    rectBitmap.eraseColor(Color.TRANSPARENT);
//    for (Rect rect : rectArrayList) {
//      rectIV.drawUnselectedRect(rect);
//    }

    redrawrects();

    // updatelabelfile
    saveLabelFile();

  }

  /**
   * this method literally sets the selectedRect to null
   */
  public void removeSelectedRect(){
    this.selectedRect = null;
  }

  /**
   * this method will redraw the rects with unselected color
   */
  public void redrawrects(){
    rectBitmap.eraseColor(Color.TRANSPARENT);
    for (Rect rect : rectArrayList) {
      rectIV.drawUnselectedRect(rect);
    }
  }

  private void unselectAnyIfExist(){
    if(selectedRect==null){
      return;
    }

    removeSelectedRect();
    redrawrects();
    hideDeleteBtnCallback.run();

  }


  /***
   *
   *
   *
   *
   */

  public static class LabelDrawPadBuilder {

    private Callback drawBtnpressedcallback;
    private CallbackWithRect maskRectReadyCallback;
    private Runnable rectSelectedCallback;
    private Runnable hideDeleteBtnCallback;

    private LayoutInflater inflater;
    private ViewGroup container;
    private int position;
    private File imageFile;
    private CustomViewPager customViewPager;

    public LabelDrawPadBuilder(LayoutInflater inflater, ViewGroup container, int position) {
      this.inflater = inflater;
      this.container = container;
      this.position = position;
    }

    public LabelDrawPadBuilder setDrawBtnPressedCallback(Callback drawBtnpressedcallback) {
      this.drawBtnpressedcallback = drawBtnpressedcallback;
      return this;
    }

    public LabelDrawPadBuilder setMaskRectReadyCallback(CallbackWithRect maskRectReadyCallback) {
      this.maskRectReadyCallback = maskRectReadyCallback;
      return this;
    }

    public LabelDrawPadBuilder setImageFile(File imageFile) {
      this.imageFile = imageFile;
      return this;
    }

    public LabelDrawPadBuilder setCustomViewPager(CustomViewPager customViewPager) {
      this.customViewPager = customViewPager;
      return this;
    }

    public LabelDrawPadBuilder setRectSelectedCallback(Runnable rectSelectedCallback) {
      this.rectSelectedCallback = rectSelectedCallback;
      return this;
    }

    public LabelDrawPadBuilder setHideDeleteBtnCallback(Runnable hideDeleteBtnCallback) {
      this.hideDeleteBtnCallback = hideDeleteBtnCallback;
      return this;
    }

    public LabelDrawPad build() {
      return new LabelDrawPad(this);
    }



  }

}
