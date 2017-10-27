package com.example.chadrick.datalabeling.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.chadrick.datalabeling.R;

/**
 * Created by chadrick on 17. 10. 15.
 */

public class SplashScreenFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.loadingscreen,container,false);

        return root;
    }
}
