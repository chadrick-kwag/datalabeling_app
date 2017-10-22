package com.example.chadrick.datalabeling;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.text.method.Touch;
import android.util.Log;
import android.view.ViewTreeObserver;

import java.io.File;

/**
 * Created by chadrick on 17. 10. 23.
 */

public class PageInfoSet {
  private TouchImageView mainIV;
  private MaskImageView maskIV;
  private PageFrameLayout pageFrameLayout;
  private CustomViewPager customViewPager;
  private Callback drawBtnpressedcallback;
  private CallbackWithRect maskRectReadyCallback;
  private Canvas mainCanvas;
  private File bitmapFile, labelFile;
  private Bitmap mainBitmap;


  private final String TAG = this.getClass().getSimpleName();

  public PageInfoSet(TouchImageView mainIV, MaskImageView maskIV, PageFrameLayout pageFrameLayout,
                     CustomViewPager customViewPager, Callback drawBtnpressedcallback, CallbackWithRect maskRectReadyCallback,
                     File bitmapFile){
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

    maskIV.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
      @Override
      public void onGlobalLayout() {

        maskIV.passWH(maskIV.getWidth(), maskIV.getHeight());
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
      Log.d(TAG," label file("+ labelFile.toString() +"), doesn't exist");
    }

  }

}
