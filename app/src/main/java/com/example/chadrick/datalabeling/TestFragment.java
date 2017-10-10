package com.example.chadrick.datalabeling;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by chadrick on 17. 10. 9.
 */

public class TestFragment extends Fragment {

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
    return inflater.inflate(R.layout.datasetselectfragment_layout,container, false);
  }
}
