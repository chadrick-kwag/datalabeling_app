package com.example.chadrick.datalabeling;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.pdf.PdfDocument;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by chadrick on 17. 10. 12.
 */

public class FullScreenImageAdapter extends PagerAdapter {
  private Context context;

  private ArrayList<File> imagefiles;
  private CustomViewPager customViewPager;
  private Callback drawBtnpressedcallback;

  private CallbackWithRect callback2;
  private Canvas canvas;
  private Canvas subcanvas;
  private int screenwidth;
  private int screenheight;

  private HashMap<Integer,View> savedpages = new HashMap<Integer,View>();

  private final String TAG = this.getClass().getSimpleName();

  public FullScreenImageAdapter(Context context, ArrayList<File> imagefiles, CustomViewPager customViewPager, Callback drawBtnpressedcallback, int screenwidth, int screenheight) {
    this.context = context;
    this.imagefiles = imagefiles;
    this.customViewPager = customViewPager;
    this.drawBtnpressedcallback = drawBtnpressedcallback;
    this.screenwidth = screenwidth;
    this.screenheight = screenheight;

  }



  @Override
  public int getCount() {
    return this.imagefiles.size();
  }

  @Override
  public boolean isViewFromObject(View view, Object object) {
    return view == ((FrameLayout) object);
  }

  @Override
  public Object instantiateItem(ViewGroup container, int position) {

    Log.d(TAG,"instantiating position: "+position);
//        ImageView imgDisplay;
    TouchImageView touchimageview;
    MaskImageView tempdrawarea;
    PageFrameLayout pageframelayout;


    LayoutInflater inflater = (LayoutInflater) context
        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View viewLayout = inflater.inflate(R.layout.layout_fullscreen_image, container,
        false);

    touchimageview = (TouchImageView) viewLayout.findViewById(R.id.touchimageview);
    tempdrawarea = (MaskImageView) viewLayout.findViewById(R.id.tempdrawarea);
    pageframelayout = (PageFrameLayout) viewLayout.findViewById(R.id.pageFramelayout);
    pageframelayout.registerIVs(touchimageview,tempdrawarea);




    touchimageview.setCustomViewPager(customViewPager);
    touchimageview.setdrawBtnpressedcallback(drawBtnpressedcallback);



    tempdrawarea.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
      @Override
      public void onGlobalLayout() {

        tempdrawarea.passWH(tempdrawarea.getWidth(), tempdrawarea.getHeight());
      }
    });


    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
    options.inMutable = true;
    Bitmap bitmap = BitmapFactory.decodeFile(imagefiles.get(position).toString(), options);
//        imgDisplay.setImageBitmap(bitmap);
//        touchimageview.setImageBitmap(bitmap);
    Bitmap tempbitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
    canvas = new Canvas(tempbitmap);
    canvas.drawBitmap(bitmap, 0, 0, null);

    touchimageview.setImageBitmap(tempbitmap);
    touchimageview.passCanvas(canvas);


    tempdrawarea.setdrawBtnpressedcallback(drawBtnpressedcallback);
    tempdrawarea.passMainIV(touchimageview);

    tempdrawarea.passRectReadyCallback(callback2);



    ((ViewPager) container).addView(viewLayout);

    // save this object in the hashmap
    savedpages.put(position,viewLayout);

    return viewLayout;
  }

  @Override
  public void destroyItem(ViewGroup container, int position, Object object) {
    Log.d(TAG,"destroying position: "+position);
    ((ViewPager) container).removeView((FrameLayout) object);

    // removed from savedpages
    savedpages.remove(position);
  }

  public View getPage(int position){
    return savedpages.get(position);
  }




  public void passRectReadyCallback(CallbackWithRect callback){
    this.callback2 = callback;
  }

}
