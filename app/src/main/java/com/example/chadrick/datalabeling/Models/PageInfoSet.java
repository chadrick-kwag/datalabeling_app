package com.example.chadrick.datalabeling.Models;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.ViewTreeObserver;

import com.example.chadrick.datalabeling.Callback;
import com.example.chadrick.datalabeling.CallbackWithRect;
import com.example.chadrick.datalabeling.CustomComponents.RectDrawImageView;
import com.example.chadrick.datalabeling.CustomComponents.PageFrameLayout;
import com.example.chadrick.datalabeling.CustomComponents.SavedRectsImageView;
import com.example.chadrick.datalabeling.CustomViewPager;
import com.example.chadrick.datalabeling.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by chadrick on 17. 10. 23.
 */

public class PageInfoSet {
  private SavedRectsImageView mainIV;
  private RectDrawImageView maskIV;
  private PageFrameLayout pageFrameLayout;
  private CustomViewPager customViewPager;
  private Callback drawBtnpressedcallback;
  private CallbackWithRect maskRectReadyCallback;
  private Canvas mainCanvas;
  private Paint mainCanvasPaint;
  private File bitmapFile, labelFile;
  private Bitmap mainBitmap;
  private ArrayList<Rect> rectArrayList = new ArrayList<Rect>();


  private final String TAG = this.getClass().getSimpleName();

  public PageInfoSet(SavedRectsImageView mainIV, RectDrawImageView maskIV, PageFrameLayout pageFrameLayout,
                     CustomViewPager customViewPager, Callback drawBtnpressedcallback, CallbackWithRect maskRectReadyCallback,
                     File bitmapFile){

    // create paint for maincanvas
    mainCanvasPaint = new Paint();
    mainCanvasPaint.setColor(Color.rgb(255, 63, 20));
    mainCanvasPaint.setStrokeWidth(5);
    mainCanvasPaint.setStyle(Paint.Style.STROKE);

    // registering private fields and initializing the fields with the given parameters should be done here
    this.mainIV = mainIV;
    this.maskIV = maskIV;
    this.pageFrameLayout = pageFrameLayout;
    this.customViewPager = customViewPager;

    // drawbtnpressedcallback is used for checking if the drawbtn is pressed or not.
    this.drawBtnpressedcallback = drawBtnpressedcallback;
    this.maskRectReadyCallback = maskRectReadyCallback;
    this.bitmapFile = bitmapFile;



    // init relations

    pageFrameLayout.registerIVs(mainIV, maskIV);

    mainIV.setCustomViewPager(customViewPager);
    mainIV.setdrawBtnpressedcallback(drawBtnpressedcallback);
    mainIV.passPageInfoSet(this);

    ViewTreeObserver viewTreeObserver = maskIV.getViewTreeObserver();

    viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
      @Override
      public void onGlobalLayout() {

        maskIV.passWH(maskIV.getWidth(), maskIV.getHeight());

        maskIV.getViewTreeObserver().removeOnGlobalLayoutListener(this);

      }
    });


    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
    options.inMutable = true;
    Bitmap originalbitmap = BitmapFactory.decodeFile(bitmapFile.toString(), options);
    mainBitmap = Bitmap.createBitmap(originalbitmap.getWidth(), originalbitmap.getHeight(), Bitmap.Config.ARGB_8888);
    mainCanvas = new Canvas(mainBitmap);
    mainCanvas.drawBitmap(originalbitmap,0,0,null);

    mainIV.setImageBitmap(mainBitmap);
    mainIV.passCanvas(mainCanvas);
    // we create the canvas that is used by the mainIV here because
    // the mainIV's canvas needs to draw the original bitmap
    // but the mainIV's constructor does not do this automatically. (or I have not yet found a way to do so)
    // therefore, this job is done manually and the canvas is passed on to mainIV
    // so that in the future, mainIV can draw stuff with it.


    // maskIV has its own canvas
    maskIV.setdrawBtnpressedcallback(drawBtnpressedcallback);
    maskIV.passRectReadyCallback(maskRectReadyCallback);

    // generate label json file based on image file
    String imagefilename = Util.getOnlyFilename( bitmapFile.getName() );
    String parentpath = bitmapFile.getParent();

    String printstr = parentpath + File.separator + imagefilename + ".json";
    Log.d(TAG,"json file : "+ printstr);

    labelFile = new File( printstr);

    // check if labelfile exist. if exist, read it and draw them. if not exist, then do nothing.
    if(!labelFile.exists()){

      // when json file doesn't exist
      Log.d(TAG," label file("+ labelFile.toString() +"), doesn't exist");

    }
    else{
      // if json file exists, then read it into a json object
      Log.d(TAG,"json file exist. start parsing.");

      Log.d(TAG,"labelfile size"+labelFile.length());

      StringBuilder readtext = new StringBuilder();
      try{
        BufferedReader bufferedReader = new BufferedReader(new FileReader(labelFile));
        String line;
        while( (line= bufferedReader.readLine()) != null){
          readtext.append(line);
        }

      }
      catch(IOException e){
        e.printStackTrace();

      }

      String readresult = readtext.toString();
      Log.d(TAG,"read result: "+readresult);


      try{
        JSONObject parsed = new JSONObject(readresult);
        String bitmapfilename = parsed.getString("imgfile");
        JSONArray objects = parsed.getJSONArray("objects");

        for(int i=0;i< objects.length();i++){
          JSONObject item = objects.getJSONObject(i);

          String name = item.getString("name");
          JSONObject rect = item.getJSONObject("rect");

          int x1 = rect.getInt("x1");
          int y1 = rect.getInt("y1");
          int x2 = rect.getInt("x2");
          int y2 = rect.getInt("y2");

          Rect rectfromjson = new Rect(x1,y1,x2,y2);
          rectArrayList.add(rectfromjson);


        }


        // for all the rects in the jsonfile, parse it and save it in the rectarraylist.
//        if(rects.length()>0){
//          // rects item exists. transfer them to rectarray
//          for(int i =0;i<rects.length();i++){
//            JSONObject rectitem = rects.getJSONObject(i);
//            int x1 = rectitem.getInt("x1");
//            int y1 = rectitem.getInt("y1");
//            int x2 = rectitem.getInt("x2");
//            int y2 = rectitem.getInt("y2");
//
//            Rect rectfromjson = new Rect(x1,y1,x2,y2);
//            rectArrayList.add(rectfromjson);
//
//          }
//        }

      }
      catch(JSONException e){
        e.printStackTrace();
      }

      // parsing the jsonfile finished.
      // now draw each rect in the maincanvas

      for(int i=0;i<rectArrayList.size();i++){
        Rect rectToDraw = rectArrayList.get(i);
        mainCanvas.drawRect(rectToDraw,mainCanvasPaint);
      }


      // invalidate to make sure that the new drawing are displayed
      mainIV.invalidate();


    }

  }


  public boolean saveLabelFile(){
    // when called, rewrite the labelfile with current info

    // first create a jsonobject and populate with current info
    JSONObject newRoot = new JSONObject();

    try{
      newRoot.put("img",bitmapFile.getName());

      // create JSONarray object that contains all the current rects
      JSONArray rectarray = new JSONArray();

      for(int i=0;i<rectArrayList.size();i++){
        Rect rect = rectArrayList.get(i);
        JSONObject jsonrect = new JSONObject();
        jsonrect.put("x1", rect.left);
        jsonrect.put("y1", rect.top);
        jsonrect.put("x2", rect.right);
        jsonrect.put("y2", rect.bottom);

        rectarray.put(jsonrect);
      }

      newRoot.put("rects",rectarray);

    }
    catch(JSONException e){
      e.printStackTrace();
      return false;
    }

    // newroot is ready. write it to labelFile



    try{
      BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(labelFile));
      bufferedWriter.write(newRoot.toString());
      bufferedWriter.close();
      Log.d(TAG,"newly written contents:"+newRoot.toString());
      Log.d(TAG,"new labelfile size: "+ labelFile.length());
    }
    catch(IOException e){
      e.printStackTrace();
      return false;
    }
    // finished overwriting the labelfile
    // job finished
    // return true since we have successfully written it.
    return true;

  }

  public void addRect(Rect rect){
    rectArrayList.add(rect);
  }

}
