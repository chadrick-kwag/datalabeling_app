package com.example.chadrick.datalabeling;

import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by chadrick on 17. 10. 13.
 */

public class Util {


    public static ArrayList<File> getImageFileList(File dir){
//sanity check
        if(!dir.exists()){
            Log.d("util","ds dir doesn't exist");
            return null;
        }

        File[] files = dir.listFiles();
        ArrayList<File> imagefilelist = new ArrayList<>();
        for(File itemfile: files){
            String pathstr = itemfile.toString();
            String extension = pathstr.substring(pathstr.lastIndexOf(".") + 1, pathstr.length());
            if (extension.equals("png") || extension.equals("jpg")) {
                Log.d("util", files.toString() + " -> png or jpg file added to list");
                imagefilelist.add(itemfile);
            }
        }

        return imagefilelist;
    }


  public static Rect convertToRect(PointF start, PointF last){
    int x1,x2,y1,y2;
    if(start.x>last.x){
      x1 = (int) last.x;
      x2 = (int) start.x;
    }
    else{
      x2 = (int) last.x;
      x1 = (int) start.x;
    }

    if(start.y>last.y){
      y1=(int)last.y;
      y2=(int)start.y;
    }
    else{
      y2=(int)last.y;
      y1=(int)start.y;
    }

    Rect rect = new Rect(x1,y1,x2,y2);
    return rect;
  }



}
