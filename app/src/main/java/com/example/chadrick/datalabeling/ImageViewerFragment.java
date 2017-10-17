package com.example.chadrick.datalabeling;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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
  private Button drawButton;
    private DataSet dataSet;
    private final String TAG = this.getClass().getSimpleName();
  private boolean drawBtnpressed=false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        View root = inflater.inflate(R.layout.imageviewerfrag_layout,container, false);
        customviewPager = (CustomViewPager) root.findViewById(R.id.customviewpager);
      drawButton = (Button) root.findViewById(R.id.drawbtn);
      drawButton.setOnTouchListener(new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
          switch(motionEvent.getAction()){
            case MotionEvent.ACTION_DOWN:
              Log.d(TAG,"draw button pressed");
              drawBtnpressed=true;
              drawButton.setBackgroundColor(ContextCompat.getColor(getContext(),R.color.buttonpressedcolor));
              break;
            case MotionEvent.ACTION_UP:
              drawBtnpressed=false;
              Log.d(TAG,"draw button released");
              drawButton.setBackgroundColor(ContextCompat.getColor(getContext(),R.color.buttonreleasedcolor));
              break;
            default:
              break;
          }
          return false;
        }
      });


        // get dataset
        try{
            dataSet = DataSet.deserialize(getArguments().getString("ds"));

        }
        catch(JSONException e)
        {
            Log.d(TAG,"dataset deserialize problem occured");
            e.printStackTrace();
            return root;
        }

        //get filelist
        File dir = new File(dataSet.getDirstr());
        ArrayList<File> imagefiles = Util.getImageFileList(dir);

        if(imagefiles.size()==0){
            Log.d(TAG,"no files are found");
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
        FullScreenImageAdapter adapter = new FullScreenImageAdapter(getContext(),imagefiles,customviewPager, drawbtnpressedcallback);
        customviewPager.setAdapter(adapter);



        return root;
    }
}
