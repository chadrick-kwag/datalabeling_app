package com.example.chadrick.datalabeling;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.io.File;
import java.util.List;

/**
 * Created by chadrick on 17. 10. 8.
 */

public class DSAdapter extends RecyclerView.Adapter<DSAdapter.DSViewHolder> {

  private List<DataSet> DSlist;
  private Context mContext;

  private String TAG="DSAdapter";

  public enum statoptions{
    DELETE,
    DOWNLOAD
  }


  public class DSViewHolder extends RecyclerView.ViewHolder{
    public TextView name;
    public ImageView storeiv;

    public CircularProgressBar downloadpgb;

    public DSViewHolder(View view){
      super(view);
      name = (TextView) view.findViewById(R.id.name);
      storeiv = (ImageView) view.findViewById(R.id.storeActionImageView);
      downloadpgb = (CircularProgressBar) view.findViewById(R.id.progressBar);
    }
  }

  public DSAdapter(Context context, List<DataSet> input){
    this.mContext = context;
    this.DSlist = input;
  }


  @Override
  public DSViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
    View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.dslist_row,parent,false);
    return new DSViewHolder(itemView);
  }

  @Override
  public void onBindViewHolder(DSViewHolder holder, int position){
    DataSet ds = DSlist.get(position);
    holder.name.setText(ds.getName());

    // the default is download icon.
    // if exists, then display the delete icon instead
    if(ds.getDirexist()){
      switchstaticon(holder,statoptions.DELETE,ds);
//      holder.storeiv.setBackgroundResource(R.drawable.ic_delete_black_24dp);
//      // in this case, we need to add a different clicklistener
//      holder.storeiv.setOnClickListener(new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//
//          File dir = new File(mContext.getFilesDir(),Integer.toString(ds.getId()));
//          Log.d(TAG,"dir:"+dir.toString());
//          if(dir.exists()){
//            Log.d(TAG,"dir exists. deleting.");
//            dir.delete();
//            // after deleting, then change the icon and the clicklistener
//          }
//          else{
//            Log.d(TAG,"dir does not exist");
//          }
//        }
//      });
    }
    else{
      // download icon. add appropriate click listener

      switchstaticon(holder,statoptions.DOWNLOAD,ds);

//      holder.storeiv.setOnClickListener(new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//          Log.d(TAG,"will download");
//
//          DownloadTask downloadTask = new DownloadTask(mContext, new CustomCallbackInterface(){
//            @Override
//            public void execute(){
//              holder.storeiv.setBackgroundResource(R.drawable.ic_delete_black_24dp);
//            }
//          });
//          downloadTask.execute("http://13.124.175.119:4001/dszips/1.zip");
//
//
//        }
//      });

    }


  }

  @Override
  public int getItemCount(){
    return DSlist.size();
  }


  // interfaces
  interface CustomCallbackInterface{
    void execute();
  }

  public void switchstaticon(DSViewHolder holder,statoptions newstat, DataSet ds){
    if(newstat==statoptions.DOWNLOAD){


      holder.storeiv.setBackgroundResource(R.drawable.ic_file_download_black_24dp);
      holder.storeiv.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          Log.d(TAG,"will download");
          // show progressbar
          holder.downloadpgb.setVisibility(View.VISIBLE);

          DownloadTask downloadTask = new DownloadTask(mContext, holder.downloadpgb, new CustomCallbackInterface(){
            @Override
            public void execute(){
              switchstaticon(holder,statoptions.DELETE,ds);
            }
          });
          downloadTask.execute("http://13.124.175.119:4001/dszips/1.zip");

        }
      });



    }
    else if(newstat == statoptions.DELETE){
      holder.storeiv.setBackgroundResource(R.drawable.ic_delete_black_24dp);

      holder.storeiv.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {

          File dir = new File(mContext.getFilesDir(),Integer.toString(ds.getId()));
          Log.d(TAG,"dir:"+dir.toString());
          if(dir.exists()){
            Log.d(TAG,"dir exists. deleting.");
            dir.delete();
            // after deleting, then change the icon and the clicklistener
            switchstaticon(holder,statoptions.DOWNLOAD,ds);
          }
          else{
            Log.d(TAG,"dir does not exist");
          }
        }
      });

    }
    else{
      return;
    }
  }



}
