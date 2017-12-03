package com.example.chadrick.datalabeling.Fragments

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.JsonRequest
import com.android.volley.toolbox.Volley
import com.example.chadrick.datalabeling.Models.DataSet
import com.example.chadrick.datalabeling.R
import kotlinx.android.synthetic.main.datasetprogressfragment2_layout.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Created by chadrick on 17. 12. 3.
 */

class DatasetProgressFragment2 : Fragment() {

//    private  ds : DataSet =  DataSet.deserialize(arguments.get("ds") as String)
    private val ds : DataSet by lazy { DataSet.deserialize(arguments.get("ds") as String) }
    private var bgcolor : Int =0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bgcolor = Color.parseColor(arguments.getString("bgcolor"))
        Log.d("chadrick","bgcolor fetched at oncreate="+bgcolor)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater?.inflate(R.layout.datasetprogressfragment2_layout, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        title_tv.text = ds.name
        Log.d("chadrick","bgcolor="+bgcolor)
        thumbnail_holder.setBackgroundColor(bgcolor)

        fetchdescription()
        if(checkAlreadyDownloaded()){
            // if already downloaded, goto statcheck

        }
        else{
            // if not downloaded, then show download button.
        }
    }

    private fun fetchdescription(){
        // create post request
        val jsonreqobj = JSONObject()
        jsonreqobj.put("id", ds.id)
        val jsonarray = JSONArray("['description']")
        jsonreqobj.put("reqfield", jsonarray)
        val jsonreq = JsonObjectRequest(Request.Method.POST, UserMainFragment.baseurl + "/dsinfo",
                jsonreqobj,
                { response :JSONObject? ->

                    val isSuccess = response?.getBoolean("success") ?: false

                    if (isSuccess){
                        // is success if true
                        val desc = response?.getString("description")
                        description_tv.text = desc
                    }
                    else{
                        Log.d("chadrick","received unsuccessful response from server")
                        description_tv.text = "unsuccessful response"
                    }

                },
                { error: VolleyError? ->
                    Log.d("chadrick","error occured while fetching description. err="+error.toString())
                    description_tv.text = "failed to get description"
                }
                )
        val queue = Volley.newRequestQueue(context)
        queue.add(jsonreq)
    }

    private fun checkAlreadyDownloaded(): Boolean{
        // if the dir with ds id exists, then we assume that the directory is all set.

        // TODO: make the checking of downloaded_&installed more detailed
        val dsworkdir = File( context.filesDir, ds.name )
        return dsworkdir.exists()
    }

    private fun showDownloadButton(){

    }

    private fun showUploadAndContButton(){

    }
}