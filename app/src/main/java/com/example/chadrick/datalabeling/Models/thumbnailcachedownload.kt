package com.example.chadrick.datalabeling.Models

import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.Log
import android.widget.ImageView
import com.example.chadrick.datalabeling.Tasks.dszipDownloadTask
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Created by chadrick on 17. 12. 7.
 */

class thumbnailcachedownload(iv: ImageView?, dsid: Int, savefile: File) : AsyncTask<Void, Int, Int>() {

    private val serveraddr = ServerInfo.instance.serveraddress
    private val dsid = dsid
    private val savefile = savefile
    private val iv = iv

    companion object {
        val TAG = "thumbnailcachedownload"
    }

    override fun doInBackground(vararg p0: Void?): Int {
        val url: URL = URL(serveraddr + "/thumbnail" + "?id=" + dsid.toString())
        if (!savefile.exists()) {
            savefile.parentFile.mkdirs()
            savefile.createNewFile()

        } else {
            // this download task is launched under the assumption that savefile doesn't exist.
            // but for safety, if it does exist, delete it.
            savefile.delete()
            savefile.createNewFile()
        }

        val output = FileOutputStream(savefile)


        val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connect()

        if (connection.responseCode != HttpURLConnection.HTTP_OK) {
            Log.d(dszipDownloadTask.TAG, "download attempt fail. " + connection.responseMessage)
            return 1
        }

        val filelength = connection.contentLength
        val input = connection.inputStream

        var data = ByteArray(4096)
        var total: Long = 0
        var count: Int = 0
        while (true) {
            count = input.read(data)
            if (count == -1) {
                break
            }

            if (isCancelled) {
                input.close()
                return 1
            }
            total += count
            // publishing the progress....
            if (filelength > 0)
            // only if total length is known
            {
//                publishProgress((total * 100 / filelength).toInt() as Integer)
            }
            output.write(data, 0, count)
        }

        input?.close()
        output?.close()
        connection?.disconnect()

        Log.d(TAG, "download finished")

        return 0


    }


    override fun onProgressUpdate(vararg values: Int?) {
        super.onProgressUpdate(*values)

    }

    override fun onPostExecute(result: Int?) {
        super.onPostExecute(result)



        when (result) {
            0 -> {
                val savedbitmap = BitmapFactory.decodeFile(savefile.toString())
                iv?.let { it.setImageBitmap(savedbitmap) }


            }
            else -> {
                Log.d(TAG, "error with cache thumbnail download")
            }
        }
    }

}
