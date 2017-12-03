package com.example.chadrick.datalabeling.Tasks

import android.os.AsyncTask
import android.util.Log
import com.example.chadrick.datalabeling.Fragments.UserMainFragment
import com.example.chadrick.datalabeling.MainActivity
import com.example.chadrick.datalabeling.Models.DataSet
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Created by chadrick on 17. 12. 4.
 */

class dszipDownloadTask : AsyncTask<String, Integer, String>(){

    lateinit var unziptask : UnzipTask
    lateinit var dataset : DataSet

    companion object {
        val TAG : String = this.javaClass.simpleName
    }

    constructor(param_unzipTask: UnzipTask , param_dataset: DataSet){
        this.unziptask = param_unzipTask
        dataset = param_dataset

    }

    override fun doInBackground(vararg p0: String?): String {
        val url : URL = URL(UserMainFragment.baseurl+"?id="+dataset.id.toString())
        val outputpath : File = File(dataset.zipfilestr)

        if(!outputpath.exists()){
            outputpath.parentFile.mkdirs()
            outputpath.createNewFile()

        }
        else{
            outputpath.delete()
            outputpath.createNewFile()
        }

        val output = FileOutputStream(outputpath)

        val connection : HttpURLConnection = url.openConnection() as HttpURLConnection
        connection.requestMethod="GET"
        connection.connect()

        if(connection.responseCode!=HttpURLConnection.HTTP_OK){
            Log.d(TAG,"download attempt fail. "+connection.responseMessage)
            return "Error : "+connection.responseMessage
        }

        val filelength = connection.contentLength
        val input = connection.inputStream

        var data = ByteArray(4096)
        var total : Long =0
        var count : Int =0
        while (true){
            count = input.read(data)
            if(count==-1){
                break
            }

            if(isCancelled){
                input.close()
                return "cancelled"
            }
            total += count
            // publishing the progress....
            if (filelength > 0)
            // only if total length is known
            {
                publishProgress((total * 100 / filelength).toInt() as Integer)
            }
            output.write(data, 0, count)
        }

        input?.close()
        output?.close()
        connection?.disconnect()

        Log.d(TAG,"download finished")

        return "success"

    }

    override fun onProgressUpdate(vararg values: Integer?) {
        super.onProgressUpdate(*values)
        // should update the progresscircle later on
        Log.d(TAG,"updated progress="+values)
    }

    override fun onPostExecute(result: String?) {
//        super.onPostExecute(result)
        if(result!!.contains("Error")){
            
        }

    }



}