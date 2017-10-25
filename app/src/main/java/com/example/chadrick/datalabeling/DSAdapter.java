package com.example.chadrick.datalabeling;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.io.File;
import java.util.List;

/**
 * Created by chadrick on 17. 10. 8.
 */

public class DSAdapter extends RecyclerView.Adapter<DSAdapter.DSViewHolder> {

  private List<DataSet> DSlist;
  private Context mContext;

  private String TAG = "DSAdapter";

  public enum statoptions {
    DELETE,
    DOWNLOAD
  }


  public class DSViewHolder extends RecyclerView.ViewHolder {
    public TextView name;
    public ImageView storeiv;

    public CircularProgressBar downloadpgb;
    public RelativeLayout mainclickarea;

    public DSViewHolder(View view) {
      super(view);
      name = (TextView) view.findViewById(R.id.name);
      storeiv = (ImageView) view.findViewById(R.id.storeActionImageView);
      downloadpgb = (CircularProgressBar) view.findViewById(R.id.progressBar);
      mainclickarea = (RelativeLayout) view.findViewById(R.id.mainclickarea);
    }
  }

  public DSAdapter(Context context, List<DataSet> input) {
    this.mContext = context;
    this.DSlist = input;
  }


  @Override
  public DSViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.dslist_row, parent, false);
    //dynamically set the width of mainclickarea width
    int totalWidth = itemView.getWidth();
    RelativeLayout mainclickarea = (RelativeLayout) itemView.findViewById(R.id.mainclickarea);

    return new DSViewHolder(itemView);
  }

  @Override
  public void onBindViewHolder(DSViewHolder holder, int position) {
    DataSet ds = DSlist.get(position);
    holder.name.setText(ds.getName());

    // the default is download icon.
    // if exists, then display the delete icon instead
    if (ds.getDirexist()) {
      switchstaticon(holder, statoptions.DELETE, ds);
    } else {
      // download icon. add appropriate click listener
      switchstaticon(holder, statoptions.DOWNLOAD, ds);
    }


    // set clicklistener to mainclickarea
    holder.mainclickarea.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        // check if the dataset exists;
        // we determine this by examining if the dir exists and the zip file does not exist.
        File testzipfile = new File(mContext.getFilesDir() + "/" + Integer.toString(ds.getId()) + "/" + Integer.toString(ds.getId()) + ".zip");
        File targetdir = new File(mContext.getFilesDir() + "/" + Integer.toString(ds.getId()));
        Boolean isproperdir = false;

        if (targetdir.exists() || !testzipfile.exists()) {
          // see if there are any .png files
          File[] files = targetdir.listFiles();
          for (File listitem : files) {
            String pathstr = listitem.toString();
            String extension = pathstr.substring(pathstr.lastIndexOf(".") + 1, pathstr.length());
            if (extension.equals("png") || extension.equals("jpg")) {
              Log.d(TAG, "png or jpg file found!");
              // now we can set the clicklistener to proceed to the next stage.
              isproperdir = true;
              break;

            }
          }

        }

        // if no dataset, then inform the user to download first
        if (isproperdir) {
          // if true then go to next fragment
          Log.d(TAG, "going to next fragment");

          FragmentManager fragmentManager = ((AppCompatActivity) mContext).getSupportFragmentManager();
          DatasetProgressFragment newfrag = new DatasetProgressFragment();
          Bundle pass = new Bundle();
          Log.d(TAG, "test print of ds serialize before adding to bundle:" + ds.serialize());
          pass.putString("ds", ds.serialize());
          newfrag.setArguments(pass);
          fragmentManager.beginTransaction().add(R.id.fragmentcontainer, newfrag).addToBackStack("name1").commit();
          Log.d(TAG, "fragment changed to DatasetProgressFragment");


        } else {
          Log.d(TAG, "dataset not exist. alert the user.");
          Toast.makeText(mContext, "download first", Toast.LENGTH_SHORT).show();

        }


      }
    });


  }

  @Override
  public int getItemCount() {
    return DSlist.size();
  }


  // interfaces
  interface CustomCallbackInterface {
    void execute();
  }

  public void switchstaticon(DSViewHolder holder, statoptions newstat, DataSet ds) {
    if (newstat == statoptions.DOWNLOAD) {


      holder.storeiv.setBackgroundResource(R.drawable.ic_file_download_black_24dp);
      holder.storeiv.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          Log.d(TAG, "will download");
          // show progressbar
          holder.downloadpgb.setVisibility(View.VISIBLE);

          DownloadTask downloadTask = new DownloadTask(mContext, holder, new CustomCallbackInterface() {
            @Override
            public void execute() {

              switchstaticon(holder, statoptions.DELETE, ds);
            }
          });
          downloadTask.execute("http://13.124.175.119:4001/dszips/1.zip");

        }
      });


    } else if (newstat == statoptions.DELETE) {
      holder.storeiv.setBackgroundResource(R.drawable.ic_delete_black_24dp);

      holder.storeiv.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {

          File dir = new File(mContext.getFilesDir(), Integer.toString(ds.getId()));
          Log.d(TAG, "dir:" + dir.toString());
          if (dir.exists() && dir.isDirectory()) {
            Log.d(TAG, "dir exists. deleting.");
            // assume that all children are files and not dir.
            File[] containfiles = dir.listFiles();
            for(int i=0;i<containfiles.length;i++){
              Log.d(TAG,"deleting "+containfiles[i].getPath());
              containfiles[i].delete();
            }


            // after deleting, then change the icon and the clicklistener
            switchstaticon(holder, statoptions.DOWNLOAD, ds);
          } else {
            Log.d(TAG, "dir does not exist");
          }
        }
      });

    } else {
      return;
    }
  }


}
