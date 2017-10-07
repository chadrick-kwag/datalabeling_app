package com.example.chadrick.datalabeling;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by chadrick on 17. 10. 8.
 */

public class DatasetSelectFragment extends Fragment {



    @Override
    public void onCreate(Bundle savedinstance){

        super.onCreate(savedinstance);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.datasetselectfragment_layout, container, false);
    }

}
