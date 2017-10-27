package com.example.chadrick.datalabeling.Models;

import android.content.Context;
import com.example.chadrick.datalabeling.DownloadTask;
import java.util.HashMap;
import java.util.function.Consumer;

/**
 * Created by chadrick on 17. 10. 28.
 */

public class DownloadTaskManager {

  // this class should be a singleton.
  private static volatile DownloadTaskManager singleinstance = new DownloadTaskManager();



  // a hashmap to keep a reference for each ds id.

  private HashMap<DataSet, DownloadTask> downloadTaskHashMap = new HashMap<>();

  private DownloadTaskManager(){}

  public static DownloadTaskManager getInstance(){
    return singleinstance;
  }

  public synchronized boolean isAlreadyRegistered(DataSet dataSet){
    // for the given dataset, checks if it already has a not yet finished downloadtask registered to it.
    // it such a downloadtask exists, then return true
    // otherwise, return false

    if(downloadTaskHashMap.containsKey(dataSet)){
      DownloadTask fetchedDownloadTask = downloadTaskHashMap.get(dataSet);
      if(!fetchedDownloadTask.isFinished()){
        return true;
      }
    }

    return false;
  }

  public synchronized void addDownloadTask(DataSet dataSet, DownloadTask downloadTask){
    // check if a downloadtask for this dataset exists already.
    // if it exists, then check if it is finished. if finished, then delete it
    if(downloadTaskHashMap.containsKey(dataSet)){
      DownloadTask fetchedDownloadTask = downloadTaskHashMap.get(dataSet);
      if(downloadTask!=null){
        if(downloadTask.isFinished()){
          downloadTaskHashMap.remove(dataSet);
        }
      }
    }

    // add the new downloadtask, dataset pair to the hashmap
    downloadTaskHashMap.put(dataSet,downloadTask);

  }

}
