package com.example.chadrick.datalabeling;

import android.os.Bundle;
import android.service.autofill.Dataset;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;

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


    private final String TAG = "DatasetProgressFrag";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        // get data from caller. the data should include ds object.
        dataset = new DataSet();
        try{
            dataset= DataSet.deserialize(getArguments().getString("ds"));
        }
        catch(JSONException e){
            e.printStackTrace();
            Log.d(TAG,"failed to recreate dataset");
            dataset = null;
        }

        //now init ui elements

        View root = inflater.inflate(R.layout.datasetinfofrag_layout,container, false);
        mdstitle = (TextView) root.findViewById(R.id.dstitle);
        mprogresspercentage = (TextView) root.findViewById(R.id.progresspercentage);
        mtotaltextview =(TextView) root.findViewById(R.id.total_textview);
        mdonetextview = (TextView) root.findViewById(R.id.done_textview);
        muploadbtn = (Button) root.findViewById(R.id.uploadbtn);
        mcontinuebtn = (Button) root.findViewById(R.id.continuebtn);




        // attach clicklisteners
        muploadbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"upload clicked");
            }
        });


        mcontinuebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"continue btn clicked.");

            }
        });






        return root;
    }

    @Override
    public void onResume(){
        // update values of textview

        if(dataset==null){
            Log.d(TAG,"dataset is null");
            Toast.makeText(getContext(),"dataset is null",Toast.LENGTH_SHORT).show();

            super.onResume();
            return;
        }

        // reach here, then dataset is valid
        mdstitle.setText(Integer.toString(dataset.getId()));

        // get number of image files in the dataset dir
        Log.d(TAG,"ds getdirstr result: "+ dataset.getDirstr());

        File dir = new File(dataset.getDirstr());

        //sanity check
        if(!dir.exists()){
            Log.d(TAG,"ds dir doesn't exist");
            return;
        }

        File[] files = dir.listFiles();
        ArrayList<File> imagefilelist = new ArrayList<>();
        for(File itemfile: files){
            String pathstr = itemfile.toString();
            String extension = pathstr.substring(pathstr.lastIndexOf(".") + 1, pathstr.length());
            if (extension.equals("png") || extension.equals("jpg")) {
                Log.d(TAG, files.toString() + " -> png or jpg file added to list");
                imagefilelist.add(itemfile);
            }
        }

        // get the size of list and calcalate the progresspercentage
        // and the number of labeling finished images.

        int num_finisedimages =1;
        int sizeofds = imagefilelist.size();
        int percentage;
        if(sizeofds>0){
            percentage =num_finisedimages *100 / sizeofds;
        }
        else{
            percentage = 0;
        }


        mprogresspercentage.setText(Integer.toString(percentage)+"%");
        mtotaltextview.setText("total images: "+Integer.toString(sizeofds));
        mdonetextview.setText("labeled images: "+Integer.toString(num_finisedimages));


        super.onResume();




    }







}
