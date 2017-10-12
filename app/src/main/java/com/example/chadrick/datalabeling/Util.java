package com.example.chadrick.datalabeling;

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
}
