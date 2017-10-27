package com.example.chadrick.datalabeling.Tasks;

import android.os.AsyncTask;
import com.example.chadrick.datalabeling.DSAdapter;
import com.example.chadrick.datalabeling.Unzip;
import java.io.File;

/**
 * Created by chadrick on 17. 10. 28.
 */

public class UnzipTask extends AsyncTask<Void,Void,Integer> {

  private File zipfile;
  private DSAdapter.CustomCallbackInterface mCallback;

  public UnzipTask(File zipfile, DSAdapter.CustomCallbackInterface callback){
    this.zipfile = zipfile;
    this.mCallback = callback;
  }

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

}
