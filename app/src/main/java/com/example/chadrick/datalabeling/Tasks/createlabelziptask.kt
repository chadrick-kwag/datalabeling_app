package com.example.chadrick.datalabeling.Tasks

import android.os.AsyncTask
import android.util.Log
import com.example.chadrick.datalabeling.Models.DataSet
import com.example.chadrick.datalabeling.Models.JWTManager
import com.example.chadrick.datalabeling.Util
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by chadrick on 17. 12. 12.
 */

class createlabelziptask(dataset: DataSet, error: ()->Unit = {}, success : ()->Unit = {}) : AsyncTask<Void, Int, Int>() {

    companion object {
        val TAG = "createlabelziptask"
        val IMAGE_FILE_NUM_MISMATCH=-2
        val ERROR_WHILE_ZIPPING=-1
        val ERROR_USERID_NULL=-3
    }

    val ds = dataset
    val error_callback = error
    val success_callback = success
//    val userid = JWTManager.getInstance(null)?.userid
    var userid:String? = JWTManager.getInstance()?.userid
    lateinit var working_zipfile:File

    override fun doInBackground(vararg p0: Void?): Int {

        val dir = File(ds.dirstr)
        val jsonfiles = Util.getJsonFileList(dir)


        // check if number matches with imagefiles
        if (jsonfiles!!.size != Util.getImageFileList(dir)!!.size) {
            Log.d(TAG, "jsonfiles number doesn't match with image files number. abort")
            return IMAGE_FILE_NUM_MISMATCH
        }

        val date = SimpleDateFormat("yyMMdd_HHmmss",Locale.KOREA).format(Calendar.getInstance().time)

        if(userid==null){
            Log.d(TAG,"userid is null")
            return ERROR_USERID_NULL
        }

        val outputzipfile_path = dir.getPath() + File.separator + userid + "-" + date + ".zip"

        working_zipfile = File(outputzipfile_path)


        // zip the json files into the output zip file

        if (Util.createZipFilefromFiles(jsonfiles, working_zipfile)) {
            Log.d(TAG, "zipping success")
            Log.d(TAG, "output zip file name: " + working_zipfile.path)
            Log.d(TAG, "output zip file size: " + working_zipfile.length())
            return 0
        } else {
            Log.d(TAG, "zipping failed")
            return ERROR_WHILE_ZIPPING
        }
    }

    override fun onPostExecute(result: Int?) {
        result?.let {
            if(result<0){
                Log.d(TAG,"error occured")
                error_callback()
            }
            else{
                success_callback()
            }
        }

    }
}