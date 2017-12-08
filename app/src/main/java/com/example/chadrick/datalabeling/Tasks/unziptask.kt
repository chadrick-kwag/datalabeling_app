package com.example.chadrick.datalabeling.Tasks

import android.os.AsyncTask
import com.example.chadrick.datalabeling.Unzip
import java.io.File

/**
 * Created by chadrick on 17. 12. 6.
 */


class unziptask(zipfile : File, callback : ()->Unit) : AsyncTask<Void, Void, Int>(){

    private val zipfile : File = zipfile
    private val callback : ()->Unit = callback



    override fun doInBackground(vararg p0: Void?): Int {
        Unzip.unpackZip(zipfile)
        return 0
    }

    override fun onPostExecute(result: Int?) {
        if(result==0){
            callback()
        }
    }
}