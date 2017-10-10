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

import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by chadrick on 17. 10. 9.
 */

public class DownloadTask extends AsyncTask<String, Integer, String> {

  private Context mContext;
  private final String TAG = "DownloadTask";
  private DSAdapter.CustomCallbackInterface mCallback;
  private DSAdapter.DSViewHolder mholder;

  private String zipfilepath;

  public DownloadTask(Context context, DSAdapter.DSViewHolder holder, DSAdapter.CustomCallbackInterface callback){
    this.mContext = context;
    this.mholder = holder;
    this.mCallback = callback;
  }

  @Override
  protected String doInBackground(String... inputurl){
    Log.d(TAG,"inside doinbackground");
    InputStream input = null;
    OutputStream output = null;
    HttpURLConnection connection = null;
    URL url;
    try{
      url = new URL(inputurl[0]);
      Log.d(TAG,"url:"+url.toString());
    }
    catch(MalformedURLException e){
      e.printStackTrace();
      return null;
    }


    try {

      connection = (HttpURLConnection) url.openConnection();
      connection.connect();

      // expect HTTP 200 OK, so we don't mistakenly save error report
      // instead of the file
      if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
        Log.d(TAG,"download attempt fail");
        return "Server returned HTTP " + connection.getResponseCode()
            + " " + connection.getResponseMessage();

      }

      // this will be useful to display download percentage
      // might be -1: server did not report the length
      int fileLength = connection.getContentLength();
      Log.d(TAG,"filelength: "+Integer.toString(fileLength));

      // download the file
      input = connection.getInputStream();
      File outputpath;
      try{
        zipfilepath = mContext.getFilesDir()+"/1/1.zip";
        Log.d(TAG,"temppath: "+zipfilepath);


        outputpath = new File(zipfilepath);
        outputpath.getParentFile().mkdirs();
        outputpath.createNewFile();
      }
      catch(FileNotFoundException e){
        e.printStackTrace();
        return null;
      }
      catch(IOException e){
        e.printStackTrace();
        return null;
      }

      output = new FileOutputStream(outputpath);

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
          publishProgress((int) (total * 100 / fileLength));
        output.write(data, 0, count);
      }



    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        if (output != null)
          output.close();
        if (input != null)
          input.close();
      } catch (IOException ignored) {
      }

      if (connection != null)
        connection.disconnect();
    }
    Log.d(TAG,"download finished");

    return null;
  }

  @Override
  protected void onProgressUpdate(Integer... progress){
    super.onProgressUpdate(progress);
    Log.d(TAG,"setting progress to "+Integer.toString(progress[0]));
    mholder.downloadpgb.setProgress(progress[0]);

  }

  @Override
  protected void onPostExecute(String url){
    Log.d(TAG,"inside postexecute");
    Toast.makeText(mContext,"download finished",Toast.LENGTH_SHORT).show();
    // need to change the store icon
    mholder.downloadpgb.setVisibility(View.INVISIBLE);
    mholder.downloadpgb.setProgress(0);


    // unzip the file
    File zipfile = new File(zipfilepath);
    Log.d(TAG,"parent: "+zipfile.getParent() + ", filename: "+ zipfile.getName());
    new AsyncTask<Void,Void,Integer>(){

      @Override
      protected Integer doInBackground(Void... v){
        Unzip.unpackZip(zipfile);
        return 0;
      }

      @Override
      protected void onPostExecute(Integer i){
        // now that zipping is finished, we are ready to set the icon to delete icon

        if(i==0){
          mCallback.execute();
        }

      }

    }.execute();


    return;

  }
}
