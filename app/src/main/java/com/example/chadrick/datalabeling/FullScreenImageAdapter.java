package com.example.chadrick.datalabeling;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.example.chadrick.datalabeling.CustomComponents.MaskImageView;
import com.example.chadrick.datalabeling.CustomComponents.PageFrameLayout;
import com.example.chadrick.datalabeling.CustomComponents.TouchImageView;
import com.example.chadrick.datalabeling.Models.LabelDrawPad;
import com.example.chadrick.datalabeling.Models.LabelDrawPad.LabelDrawPadBuilder;
import com.example.chadrick.datalabeling.Models.PageInfoSet;

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
  private HashMap<Integer, PageInfoSet> pageInfoSetHashMap = new HashMap<Integer, PageInfoSet>();

  private HashMap<Integer, LabelDrawPad> labelDrawPadHashMap = new HashMap<Integer, LabelDrawPad>();

  private final String TAG = this.getClass().getSimpleName();

  public FullScreenImageAdapter(Context context, ArrayList<File> imagefiles, CustomViewPager customViewPager, Callback drawBtnpressedcallback, int screenwidth, int screenheight) {
    this.context = context;
    this.imagefiles = imagefiles;
    this.customViewPager = customViewPager;

    // drawbtnpressedcallback is used for checking if the drawbtn is pressed or not.
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

    LayoutInflater inflater = (LayoutInflater) context
        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    LabelDrawPad labelDrawPad =  new LabelDrawPad.LabelDrawPadBuilder(inflater,container,position)
        .setDrawBtnPressedCallback(drawBtnpressedcallback)
        .setCustomViewPager(customViewPager)
        .setMaskRectReadyCallback(callback2)
        .setImageFile(imagefiles.get(position))
        .build();

    // add to labeldrawpad object to hashmap
    labelDrawPadHashMap.put(position,labelDrawPad);


    View rootview = labelDrawPad.getRootview();
    ((ViewPager) container).addView(rootview);

    return rootview;

//
////        ImageView imgDisplay;
//    TouchImageView mainIV;
//    MaskImageView maskIV;
//    PageFrameLayout pageframelayout;
//
//
//
//    View viewLayout = inflater.inflate(R.layout.layout_fullscreen_image, container,
//        false);
//
//
//    mainIV = (TouchImageView) viewLayout.findViewById(R.id.touchimageview);
//    maskIV = (MaskImageView) viewLayout.findViewById(R.id.tempdrawarea);
//    pageframelayout = (PageFrameLayout) viewLayout.findViewById(R.id.pageFramelayout);
//
//    PageInfoSet pageInfoSet = new PageInfoSet(mainIV, maskIV, pageframelayout, customViewPager,
//        drawBtnpressedcallback, callback2, imagefiles.get(position));
//
//    ((ViewPager) container).addView(viewLayout);
//
//    // save this object in the hashmap
//    savedpages.put(position,viewLayout);
//    pageInfoSetHashMap.put(position,pageInfoSet);
//    Log.d(TAG,"pageinfoset of position: "+ position + " is added to hashmap");
//
//    return viewLayout;
  }

  @Override
  public void destroyItem(ViewGroup container, int position, Object object) {
    Log.d(TAG,"destroying position: "+position);
    ((ViewPager) container).removeView((FrameLayout) object);

    labelDrawPadHashMap.remove(position);

//    // removed from savedpages
//    savedpages.remove(position);
//
//    // remove from pageinfosethashmap
//    pageInfoSetHashMap.remove(position);
  }

  // this is called by the ImageViewerfragment which holds the control over
  // draw/yes/no/del buttons.
  // this is why this class needs to keep a record of pages in 'savedpages'
  // don't be mistaken. the `savedpages` is a hashmap of
  // position and the view of the page. not the page itself.
  // that pair is saved in `pageinfosethashmap`

//  public View getPage(int position){
//    return savedpages.get(position);
//  }

  public LabelDrawPad getLabelDrawPad(int position){
    return labelDrawPadHashMap.get(position);
  }

  public void passRectReadyCallback(CallbackWithRect callback){
    this.callback2 = callback;
  }

}
