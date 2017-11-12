package com.example.chadrick.datalabeling;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

/**
 * Created by chadrick on 17. 11. 13.
 */

public class MenuListAdapter extends ArrayAdapter<String> {

  private ArrayList<String> menuItems = new ArrayList<String>();

  public MenuListAdapter(Context context, int itemlayoutresource){
    super(context, itemlayoutresource);

    initMenuItems();


  }

  private void initMenuItems(){
    menuItems.add("Main");
    menuItems.add("Settings");
  }



}
