package com.example.chadrick.datalabeling.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.example.chadrick.datalabeling.Models.DataSet;
import com.example.chadrick.datalabeling.MainActivity;
import com.example.chadrick.datalabeling.R;
import com.example.chadrick.datalabeling.Util;
import com.example.chadrick.datalabeling.VolleyMultipartRequest;

import org.json.JSONException;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by chadrick on 17. 10. 11.
 */

public class DatasetProgressFragment extends Fragment {

  private TextView mdstitle;
  private TextView mprogresspercentage;
  private TextView mtotaltextview;
  private TextView mdonetextview;
  private Button muploadbtn, mcontinuebtn;
  private DataSet dataset;
  private Runnable updateStatsRunnable;
  private LinearLayout upload_layout;

  private final String TAG = "DatasetProgressFrag";


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    updateStatsRunnable = new Runnable() {
      @Override
      public void run() {
        Log.d(TAG,"update stat runnable executed");
        updateStats();
      }
    };

    // get data from caller. the data should include ds object.
    dataset = new DataSet();
    try {
      dataset = DataSet.deserialize(getArguments().getString("ds"));
    } catch (JSONException e) {
      e.printStackTrace();
      Log.d(TAG, "failed to recreate dataset");
      dataset = null;
    }

    //now init ui elements

    View root = inflater.inflate(R.layout.datasetinfofrag_layout, container, false);
    mdstitle = (TextView) root.findViewById(R.id.dstitle);
    mprogresspercentage = (TextView) root.findViewById(R.id.progresspercentage);
    mtotaltextview = (TextView) root.findViewById(R.id.total_textview);
    mdonetextview = (TextView) root.findViewById(R.id.done_textview);
    muploadbtn = (Button) root.findViewById(R.id.uploadbtn);
    mcontinuebtn = (Button) root.findViewById(R.id.continuebtn);
    upload_layout = (LinearLayout) root.findViewById(R.id.uploadbtn_wrapping_layout);


    // attach clicklisteners
    muploadbtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Log.d(TAG, "upload clicked");

        // gather the imagefiles
        // there are several ways to do this. but for simplicity, let's just gather the .json files
        // and check the number of files and see if they match with the number of image files

        // gathering .json files
        File dir = new File(dataset.getDirstr());
        ArrayList<File> jsonfiles = Util.getJsonFileList(dir);

        // check if number matches with imagefiles
        if(jsonfiles.size() != Util.getImageFileList(dir).size()){
          Log.d(TAG,"jsonfiles number doesn't match with image files number. abort");
          return;
        }

        Log.d(TAG,"jsonfiles number match");

        // if any zip file exists(probably with older date in name), delete it.


        // create output zip file
        String date = new SimpleDateFormat("yyMMdd_HHmmss").format(Calendar.getInstance().getTime());
        String outputzipfile_path = dir.getPath() + File.separator + dataset.getName() + "-" + date + ".zip";
        File outputzipfile = new File(outputzipfile_path);


        // zip the json files into the output zip file

        if( Util.createZipFilefromFiles(jsonfiles, outputzipfile)){
          Log.d(TAG,"zipping success");
          Log.d(TAG,"output zip file name: "+outputzipfile.getPath());
          Log.d(TAG,"output zip file size: "+outputzipfile.length());
        }
        else{
          Log.d(TAG,"zipping failed");
        }

        // send to server
        // first create multipart request
        VolleyMultipartRequest request = Util.createRequestFileUpload(outputzipfile,"http://13.124.175.119:4001/upload/labelzip");

        RequestQueue queue = ((MainActivity) getActivity()).getQueue();

        if(queue!=null){
          queue.add(request);
        }
        else{
          Log.d(TAG,"queue is null");
          return;
        }



    }
    });


    mcontinuebtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Log.d(TAG, "continue btn clicked.");

        ImageViewerFragment imageViewerFragment = new ImageViewerFragment();
        imageViewerFragment.passUpdateStatCallback(updateStatsRunnable);
        Bundle b = new Bundle();
        b.putString("ds", dataset.serialize());

        imageViewerFragment.setArguments(b);
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().add(R.id.fragmentcontainer, imageViewerFragment)
            .addToBackStack(null).commit();

      }
    });



    return root;

  }

  @Override
  public void onResume() {
    // update values of textview

    if (dataset == null) {
      Log.d(TAG, "dataset is null");
      Toast.makeText(getContext(), "dataset is null", Toast.LENGTH_SHORT).show();

      super.onResume();
      return;
    }

    // reach here, then dataset is valid
    mdstitle.setText(Integer.toString(dataset.getId()));

    // get number of image files in the dataset dir
    Log.d(TAG, "ds getdirstr result: " + dataset.getDirstr());

    updateStats();


    super.onResume();

  }

  private int getFinishedImageNum(ArrayList<File> imagefiles) {
    int count = 0;
    for (int i = 0; i < imagefiles.size(); i++) {

      File labelfile = Util.getLabelFilefromImageFile(imagefiles.get(i));
      if (labelfile.exists()) {
        count++;
      }
    }

    return count;
  }


  private void updateStats() {

    File dir = new File(dataset.getDirstr());

    ArrayList<File> imagefilelist = Util.getImageFileList(dir);

    int num_finishedimages = getFinishedImageNum(imagefilelist);
    int sizeofds = imagefilelist.size();
    int percentage;
    if (sizeofds > 0) {
      percentage = num_finishedimages * 100 / sizeofds;
    } else {
      percentage = 0;
    }


    mprogresspercentage.setText(Integer.toString(percentage) + "%");
    mtotaltextview.setText("total images: " + Integer.toString(sizeofds));
    mdonetextview.setText("labeled images: " + Integer.toString(num_finishedimages));

    // check and update upload btn status
    if(num_finishedimages == sizeofds){
      // enable upload btn
      upload_layout.setBackgroundResource(R.color.dsprogressfrag_btn_enable_color);
      muploadbtn.setEnabled(true);
    }
    else{
      upload_layout.setBackgroundResource(R.color.dsprogressfrag_btn_disable_color);
      muploadbtn.setEnabled(false);
    }

  }


}
