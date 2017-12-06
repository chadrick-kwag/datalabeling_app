package com.example.chadrick.datalabeling.Models

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by chadrick on 17. 12. 5.
 */

class RecentActivityLogManager {


    lateinit var mcontext: Context

    companion object {

        @Volatile private var INSTANCE: RecentActivityLogManager? = null

        fun getInstance(context: Context): RecentActivityLogManager =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: RecentActivityLogManager(context).also { INSTANCE = it }
                }


        val TAG: String = "RecentActivityLogManager"

        val RAitemMap = HashMap<DataSet, Long>()


        val jsonfilename = "recentactivitylog.json"

        lateinit var jsonfilefullpath: File


    }

    private constructor(context: Context) {
        mcontext = context
        readparse()
    }


    private fun readparse() {
        jsonfilefullpath = File(mcontext.filesDir, jsonfilename)
        Log.d(TAG, "ralogfile=" + jsonfilefullpath)

        if (!jsonfilefullpath.exists()) {
            return
        }

        val inputstream = FileInputStream(jsonfilefullpath)

        var size = inputstream.available()
        val buffer = ByteArray(size)
        inputstream.read(buffer)
        inputstream.close()
        val jsonfilereadraw = String(buffer, Charset.forName("UTF-8"))

        Log.d(TAG, "jsonfile read raw=" + jsonfilereadraw)

        val rootobj = JSONObject(jsonfilereadraw)

        val RAarray = rootobj.getJSONArray("items")



        for (i in 0..RAarray.length() - 1) {


            try {
                val item = RAarray.getJSONObject(i)
                RAitemMap.put(DataSet.deserialize(item.getString("dataset")), item.getLong("recent_access_time"))
            } catch (e: JSONException) {
                // the file is corrupt. delete it.
                Log.d(TAG, "the ra json file is corrupt. delete it")
                jsonfilefullpath.delete()
                return

            }


        }

        // mid check
        Log.d(TAG, "RAitemMap size=" + RAitemMap.size)

        // print all objects
        for (item in RAitemMap) {
            Log.d(TAG, "key=" + item.key + ", value=" + item.value)
        }

    }

    fun updateRAitem(dataset: DataSet, access_datetime: Long) {
        RAitemMap.put(dataset, access_datetime)

//        val sortedMap = RAitemMap.toList().sortedBy { (key,value) -> -value }.toMap()

//        // print the contents of sortedMap
//        Log.d(TAG,"print sorted result")
//        for(item in sortedMap){
//            Log.d(TAG,"key="+item.key+", value="+item.value)
//        }

        Log.d(TAG, "print RAitemMap")
        for (item in RAitemMap) {
            Log.d(TAG, "key.id=" + item.key.id + ", value=" + item.value)
        }

        savetojsonfile()


    }

    fun savetojsonfile() {
        Log.d(TAG, "inside savetojsonfile")

        // convert RAitemmap to json obj

        // create individual jsonojects for each RAitemmap item

        val convertedJsonobjs = ArrayList<JSONObject>()
        for (item in RAitemMap) {
            val jsonobj = JSONObject()
            jsonobj.put("dataset", item.key.serialize())
            jsonobj.put("recent_access_time", item.value)
            convertedJsonobjs.add(jsonobj)
        }


        val itemjsonarray = JSONArray(convertedJsonobjs)

        Log.d(TAG, "print itemjsonarray = " + itemjsonarray.toString())

        val rootobj = JSONObject()
        rootobj.put("items", itemjsonarray)

        // print completed jsonobj
        Log.d(TAG, "temp created root jsonobj: " + rootobj.toString())

        if (jsonfilefullpath.exists()) {
            jsonfilefullpath.delete()
        }

        val fileWriter = FileWriter(jsonfilefullpath)
        val output = BufferedWriter(fileWriter)
        output.write(rootobj.toString())
        output.close()

    }

    fun getsortedlist(): ArrayList<DataSet> {
        val pairlist = RAitemMap.toList().sortedBy { (key, value) -> -value }
        val dsidlist = ArrayList<DataSet>()
        pairlist.forEach { (key, value) -> dsidlist.add(key) }
        return dsidlist
    }


}