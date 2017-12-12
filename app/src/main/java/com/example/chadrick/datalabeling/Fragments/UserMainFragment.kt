package com.example.chadrick.datalabeling.Fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.example.chadrick.datalabeling.Models.DataSet
import com.example.chadrick.datalabeling.Models.RecentActivityLogManager
import com.example.chadrick.datalabeling.Models.ServerInfo
import com.example.chadrick.datalabeling.R
import com.example.chadrick.datalabeling.RAAdapter
import kotlinx.android.synthetic.main.usermainfragment_layout.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Created by chadrick on 17. 12. 2.
 */

class UserMainFragment : Fragment() {


    //    private val baseurl1 = "http://13.124.175.119:4001"
    private val serverFectchedDSlist: ArrayList<DataSet> = ArrayList<DataSet>()
    lateinit var allDSrecyclerviewAdapter: RAAdapter
    lateinit var RArvAdapter: RAAdapter

    private val RAlist: ArrayList<DataSet> = ArrayList<DataSet>()

    private object Holder {
        val INSTANCE = UserMainFragment()
    }

    lateinit var recentactivitylogmanager: RecentActivityLogManager

    companion object {
        //        val instance : UserMainFragment by lazy { Holder.INSTANCE}
        val instance: UserMainFragment = Holder.INSTANCE
        //        val baseurl = "http://13.124.175.119:4001"
        val baseurl = ServerInfo.instance.serveraddress

        val TAG = "UserMainFragment"

    }


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val root: View? = inflater?.inflate(R.layout.usermainfragment_layout, container, false)

        // setup adapter for RA
        allDSrecyclerviewAdapter = RAAdapter.newInstance(context, serverFectchedDSlist)
        recentactivitylogmanager = RecentActivityLogManager.getInstance(context)

        RArvAdapter = RAAdapter.newInstance(context, RAlist)


        return root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {


        super.onViewCreated(view, savedInstanceState)
        AllDSrecyclerview.adapter = allDSrecyclerviewAdapter
        AllDSrecyclerview.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        RArecyclerview.adapter = RArvAdapter
        RArecyclerview.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

    }


    override fun onResume() {
        Log.d(TAG, "fuck: inside onresume")
        super.onResume()
        populateDSrv()
        populateRAlist()
    }


    private fun populateDSrv() {
        serverFectchedDSlist.clear()

        // prepare jsonarray request
        val reqobj: JSONArray = JSONArray()
        reqobj.put(JSONObject("{'ds_request_id':'anything'}"))


        val jsonarrayreq: JsonArrayRequest = JsonArrayRequest(Request.Method.POST,
                baseurl + "/dslist",
                reqobj,
                { response: JSONArray ->

                    for (i in 0..(response.length() - 1)) {
                        val item = response.getJSONObject(i)
                        var direxist: Boolean = false
                        val filename = item.get("id").toString()
                        val file: File = File(context.filesDir, filename)

                        if (file.exists()) {
                            direxist = true
                        }

                        val ds: DataSet = DataSet(item.getInt("id"), item.getString("title"), direxist, context.filesDir.toString())

                        serverFectchedDSlist.add(ds)

                        allDSrecyclerviewAdapter.notifyDataSetChanged()


                    }
                },
                { error -> Toast.makeText(context, "failed to fetch dslist", Toast.LENGTH_SHORT).show() }

        )

        val queue = Volley.newRequestQueue(context)

        queue.add(jsonarrayreq)


    }

    fun populateRAlist() {
        Log.d(TAG, "inside populateRAlist")
        RAlist.clear()

        val newlist = recentactivitylogmanager.getsortedlist()
        for (item in newlist) {
            RAlist.add(item)
        }

        if (RAlist.size == 0) {
            RArecyclerview.visibility = View.INVISIBLE
            nonefound_tv.visibility = View.VISIBLE
        } else {
            RArecyclerview.visibility = View.VISIBLE
            nonefound_tv.visibility = View.INVISIBLE
        }

        RArvAdapter.notifyDataSetChanged()

    }


}