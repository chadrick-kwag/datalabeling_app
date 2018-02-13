package com.example.chadrick.datalabeling.Models

import com.example.chadrick.datalabeling.Tasks.dszipDownloadTask

/**
 * Created by chadrick on 17. 12. 4.
 */

class DownloadTaskManager private constructor(){


    private val hashmap : HashMap<DataSet, dszipDownloadTask> = HashMap()
    private object holder { val INSTANCE = DownloadTaskManager()}

    companion object {
        val instance : DownloadTaskManager = holder.INSTANCE
    }

    @Synchronized fun isAlreadyRegistered(ds : DataSet) : Boolean {
        if(hashmap.containsKey(ds)){
            val fetchedtask = hashmap.get(ds)
            if( !(fetchedtask!!.isFinished())) return true
        }
        return false
    }

    @Synchronized fun addDownloadTask(ds: DataSet , param_downloadtask : dszipDownloadTask){
        if(hashmap.containsKey(ds)){
            // if exists and is finished, remove it.
            val task = hashmap.get(ds)
            if(task?.isFinished() ?: false){
                hashmap.remove(ds)
            }
        }

        hashmap.put(ds,param_downloadtask)
    }

    @Synchronized fun remove(ds:DataSet){
        if(hashmap.containsKey(ds)){
            val task = hashmap.get(ds)
            if(task?.isFinished() ?: true == false){
                hashmap.remove(ds)
                task?.cancel(true)
            }
        }
    }

}