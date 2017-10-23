package com.example.chadrick.datalabeling;

import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.text.method.Touch;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import static com.android.volley.VolleyLog.TAG;

/**
 * Created by chadrick on 17. 10. 11.
 */

public class ImageViewerFragment extends Fragment {

  private CustomViewPager customviewPager;
  private Button drawButton, yesbtn, nobtn;


  private DataSet dataSet;
  private final String TAG = this.getClass().getSimpleName();
  private boolean drawBtnpressed = false;
  private FullScreenImageAdapter adapter;

  private CallbackWithRect RectReadycallback;

  private int viewpager_currentposition;
  private Rect receivedRect;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    View root = inflater.inflate(R.layout.imageviewerfrag_layout, container, false);
    customviewPager = (CustomViewPager) root.findViewById(R.id.customviewpager);

    drawButton = (Button) root.findViewById(R.id.drawbtn);
    drawButton.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
          case MotionEvent.ACTION_DOWN:
            Log.d(TAG, "draw button pressed");
            drawBtnpressed = true;
            drawButton.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.buttonpressedcolor));
            break;
          case MotionEvent.ACTION_UP:
            drawBtnpressed = false;
            Log.d(TAG, "draw button released");
            drawButton.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.buttonreleasedcolor));
            break;
          default:
            break;
        }
        return false;
      }
    });

    yesbtn = (Button) root.findViewById(R.id.yesbtn);
    yesbtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        //enable the touch events in the two main IVs
        View pageview = adapter.getPage(viewpager_currentposition);

        // fetch the two IVs
        TouchImageView mainIV = pageview.findViewById(R.id.touchimageview);
        MaskImageView maskIV = pageview.findViewById(R.id.tempdrawarea);

        // disable the touch of these two IVs
        mainIV.setTouchEnable(true);
        maskIV.setTouchEnable(true);

        // hide yes and no btn from layout
        yesbtn.setVisibility(View.INVISIBLE);
        nobtn.setVisibility(View.INVISIBLE);

        // enable draw btn
        drawButton.setVisibility(View.VISIBLE);

        // actually draw rectangle in mainIV
        mainIV.drawRect();

        // clear the temp rectangle in maskIV
        maskIV.eraseall();
      }
    });


    nobtn = (Button) root.findViewById(R.id.nobtn);

    nobtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

        //enable the touch events in the two main IVs
        View pageview = adapter.getPage(viewpager_currentposition);

        // fetch the two IVs
        TouchImageView mainIV = pageview.findViewById(R.id.touchimageview);
        MaskImageView maskIV = pageview.findViewById(R.id.tempdrawarea);

        // disable the touch of these two IVs
        mainIV.setTouchEnable(true);
        maskIV.setTouchEnable(true);

        // hide yes and no btn from layout
        yesbtn.setVisibility(View.INVISIBLE);
        nobtn.setVisibility(View.INVISIBLE);

        // enable draw btn
        drawButton.setVisibility(View.VISIBLE);

        //erase maskIV
        maskIV.eraseall();


      }
    });


    // get dataset
    try {
      dataSet = DataSet.deserialize(getArguments().getString("ds"));

    } catch (JSONException e) {
      Log.d(TAG, "dataset deserialize problem occured");
      e.printStackTrace();
      return root;
    }

    //get filelist
    File dir = new File(dataSet.getDirstr());
    ArrayList<File> imagefiles = Util.getImageFileList(dir);

    if (imagefiles.size() == 0) {
      Log.d(TAG, "no files are found");
      return root;
    }

    // sort the list alphabetically
    Collections.sort(imagefiles);


    Callback drawbtnpressedcallback = new Callback() {
      @Override
      public boolean getBoolean() {
        return drawBtnpressed;
      }
    };

    Display display = getActivity().getWindowManager().getDefaultDisplay();
    Point size = new Point();
    display.getSize(size);
    int screenwidth = size.x;
    int screenheight = size.y;

    adapter = new FullScreenImageAdapter(getContext(), imagefiles, customviewPager, drawbtnpressedcallback, screenwidth, screenheight);

    customviewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
      @Override
      public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

      }

      @Override
      public void onPageSelected(int position) {
        // update the page position

        viewpager_currentposition = position;

      }

      @Override
      public void onPageScrollStateChanged(int state) {

      }
    });

    customviewPager.setAdapter(adapter);


    RectReadycallback = new CallbackWithRect() {
      @Override
      public void doit(Rect rect) {
        Log.d(TAG,"inside testcallback");

        // set the receivedRect
        receivedRect = rect;

        // disable the touch handler in the two IVs

        // first access the appropriate page
        View pageview = adapter.getPage(viewpager_currentposition);

        // fetch the two IVs
        TouchImageView mainIV = pageview.findViewById(R.id.touchimageview);
        MaskImageView maskIV = pageview.findViewById(R.id.tempdrawarea);

        // disable the touch of these two IVs
        mainIV.setTouchEnable(false);
        maskIV.setTouchEnable(false);

        // show yes/no btns
        yesbtn.setVisibility(View.VISIBLE);
        nobtn.setVisibility(View.VISIBLE);



        // release and disable the drawbtn

        drawButton.setVisibility(View.INVISIBLE);


        // force draw in maskIV
//        maskIV.forcedrawRect(receivedRect);


      }
    };

    adapter.passRectReadyCallback(RectReadycallback);


    return root;
  }
}
