package com.example.chadrick.datalabeling;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.RotateDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.chadrick.datalabeling.Models.DataSet;
import com.example.chadrick.datalabeling.Models.DownloadTaskManager;
import com.example.chadrick.datalabeling.Tasks.UnzipTask;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import org.json.JSONObject;

/**
 * Created by chadrick on 17. 10. 9.
 */

public class DownloadTask extends AsyncTask<String, Integer, String> {

  private Context mContext;
  private final String TAG = "DownloadTask";
  private DSAdapter.CustomCallbackInterface mCallback;
  private DSAdapter.DSViewHolder mholder;
  private UnzipTask unzipTask = null;
  private DataSet dataSet;


  public DownloadTask(Context context, DSAdapter.DSViewHolder holder,
      DSAdapter.CustomCallbackInterface callback, DataSet dataSet) {
    this.mContext = context;
    this.mholder = holder;
    this.mCallback = callback;
    this.dataSet = dataSet;
  }

  @Override
  protected String doInBackground(String... inputurl) {
    Log.d(TAG, "inside doinbackground");
    // create jsonrequest based on dataset

    InputStream input = null;
    OutputStream output = null;
    HttpURLConnection connection = null;
    URL url;
    try {

      String baseurl = "http://13.124.175.119:4001/download/dszip";
      String finalurl = baseurl + "?id=" + Integer.toString(dataSet.getId());
      url = new URL(finalurl);

    } catch (MalformedURLException e) {
      e.printStackTrace();
      return null;
    }

    // prepare outputfile
    File outputpath = new File(dataSet.getZipfilestr());
    try {

      if (!outputpath.exists()) {
        // if the output file does not exist, then
        // create the parents and the file itself.
        outputpath.getParentFile().mkdirs();
        outputpath.createNewFile();
      } else {
        // if it exist, delete it
        outputpath.delete();

        // only recreate the file itself
        // since the parent dirs should exist.
        outputpath.createNewFile();
      }

      output = new FileOutputStream(outputpath);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return null;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }

    // attempt to connect
    try {

      Log.d(TAG,"creating connection to: "+url.toString());
      connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");


      connection.connect();

      // expect HTTP 200 OK, so we don't mistakenly save error report
      // instead of the file
      if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
        Log.d(TAG, "download attempt fail. " + connection.getResponseMessage());
        return "Error: Server returned HTTP " + connection.getResponseCode()
            + " " + connection.getResponseMessage();

      }

      Log.d(TAG, "doInBackground: responsecode = "+connection.getResponseCode());
      Log.d(TAG, "doInBackground: responsemsg = "+connection.getResponseMessage());


      // this will be useful to display download percentage
      // might be -1: server did not report the length
      int fileLength = connection.getContentLength();
      Log.d(TAG, "filelength: " + Integer.toString(fileLength));

      // download the file
      input = connection.getInputStream();

      byte data[] = new byte[4096];
      long total = 0;
      int count;
      while ((count = input.read(data)) != -1) {

        // allow canceling with back button
        if (isCancelled()) {
          input.close();
          return null;
        }
        total += count;
        // publishing the progress....
        if (fileLength > 0) // only if total length is known
        {
          publishProgress((int) (total * 100 / fileLength));
        }
        output.write(data, 0, count);
      }


    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        if (output != null) {
          output.close();
        }
        if (input != null) {
          input.close();
        }
      } catch (IOException ignored) {
      }

      if (connection != null) {
        connection.disconnect();
      }
    }
    Log.d(TAG, "download finished. file: " + outputpath.toString());

    String returnstr = new String("success");
    return returnstr;
  }

  @Override
  protected void onProgressUpdate(Integer... progress) {
    super.onProgressUpdate(progress);
    Log.d(TAG, "setting progress to " + Integer.toString(progress[0]));
    mholder.downloadpgb.setProgress(progress[0]);

  }

  @Override
  protected void onPostExecute(String url) {
    Log.d(TAG, "inside postexecute");

    if(url.contains("Error")){
      Log.d(TAG, "onPostExecute: error: "+url);
      Toast.makeText(mContext,"download failed",Toast.LENGTH_SHORT).show();

      mholder.downloadpgb.setVisibility(View.INVISIBLE);
      mholder.downloadpgb.setProgress(0);
      return;
    }

    Toast.makeText(mContext, "download finished", Toast.LENGTH_SHORT).show();
    // need to change the store icon
    mholder.downloadpgb.setVisibility(View.INVISIBLE);
    mholder.downloadpgb.setProgress(0);

    // unzip the file
    File zipfile = new File(dataSet.getZipfilestr());
    Log.d(TAG, "parent: " + zipfile.getParent() + ", filename: " + zipfile.getName());

//    Unzip.unpackZip(zipfile);
//    mCallback.execute();

    // hmm.. could there be another way to deal with this properly?

    unzipTask = new UnzipTask(zipfile, mCallback);
    unzipTask.execute();

    return;

  }

  public boolean isFinished() {
    // just because this asynctask(DownloadTask) is finished(onPostExecute finished)
    // doesn't mean that the actuall downloading and unzipping process is finished
    // this is all finished when the unzipTask is finished for good.

    if (unzipTask == null) {
      // the fact that unzipTask is not even created means that this downloadtask
      // hasn't even reached onPostExecute phase. Therefore it is safe to say
      // that the entire process is not finished.
      return false;
    }

    if (unzipTask.getStatus() == Status.FINISHED) {
      return true;
    }

    return false;
  }
}
