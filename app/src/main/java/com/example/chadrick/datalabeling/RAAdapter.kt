package com.example.chadrick.datalabeling

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.chadrick.datalabeling.Fragments.DatasetProgressFragment
import com.example.chadrick.datalabeling.Models.BGColorRandomPicker
import com.example.chadrick.datalabeling.Models.DataSet
import com.example.chadrick.datalabeling.Models.WeakRefHolder
import com.example.chadrick.datalabeling.Models.thumbnailcachedownload
import kotlinx.android.synthetic.main.usermain_dataset_thumb_layout.view.*
import java.io.File
import java.util.*

/**
 * Created by chadrick on 17. 12. 2.
 */

class RAAdapter : RecyclerView.Adapter<RAAdapter.RAViewHolder>() {


    lateinit var DSlist: ArrayList<DataSet>
    private val colorgen = BGColorRandomPicker()
    var context: Context by WeakRefHolder()
    lateinit var getfragmentmanagercallback: () -> android.support.v4.app.FragmentManager


    companion object {
        fun newInstance(context: Context, dslist: ArrayList<DataSet>, fragmanagercallback: () -> android.support.v4.app.FragmentManager): RAAdapter {
            val obj: RAAdapter = RAAdapter()
            obj.DSlist = dslist
            obj.context = context
            obj.getfragmentmanagercallback = fragmanagercallback
            return obj
        }

        val TAG: String = "RAAdapter"
    }


    override fun getItemCount(): Int {
        return DSlist.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RAViewHolder {
        val itemview: View = LayoutInflater.from(parent?.context).inflate(R.layout.usermain_dataset_thumb_layout, parent, false)

        return RAViewHolder.newInstance(itemview)
    }

    override fun onBindViewHolder(holder: RAViewHolder?, position: Int) {
        val dataset: DataSet = DSlist.get(position)
        // set the image for holder.imageview
        holder?.titleTextView?.setText(dataset.name)
        val colstr = colorgen.getRandomColor()
        Log.d("chadrick", "colstr=" + colstr)
        holder?.color = Color.parseColor(colstr)

        // check if thumbnail exists
        // first check if ds dir exist
        val dsdir = File(dataset.dirstr)
        val thumbnailpath = dataset.dirstr + "/info/thumbnail.jpg"
        val thumbnailfile = File(thumbnailpath)

        if (dsdir.exists() && thumbnailfile.exists()) {
            // check if thumbnail.png exist

            Log.d(TAG, "thumbnail file for dsid=" + dataset.id + " exists.")
            val options = BitmapFactory.Options()
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            val thumbnailbitmap = BitmapFactory.decodeFile(thumbnailpath, options)
            holder?.imageview?.setImageBitmap(thumbnailbitmap)

        } else {
            Log.d(TAG, "dsid=" + dataset.id + "dsdir doesn't exist")

            // check if a thumbnail exists in the thumbnailcache dir.

            val cachesavefilepath = context.filesDir.toString() + "/thumbnailcache/" + dataset.id.toString() + ".jpg"

            Log.d(TAG,"check cache image file size for dsid="+dataset.id+" , size="+ cachesavefilepath.length)

            if (File(cachesavefilepath).exists()) {
                val loadbitmap = BitmapFactory.decodeFile(cachesavefilepath)
                holder?.imageview?.setImageBitmap(loadbitmap)
                Log.d(TAG, "cache image exists and setting it as background. dsid=" + dataset.id)
            } else {
                val cachedownload = thumbnailcachedownload(holder?.imageview, dataset.id, File(cachesavefilepath))
                cachedownload.execute()
                Log.d(TAG, "thumbnail cache download executed for dsid=" + dataset.id)
            }


        }

//        holder?.imageview?.setBackgroundColor(holder.color)
        holder?.imageview?.setOnClickListener({ v: View ->
            //            val frag = DatasetProgressFragment()
            val frag = DatasetProgressFragment()
            val bundle = Bundle()
            bundle.putString("ds", dataset.serialize())
            Log.d("chadrick", "put bgcolor=" + colstr)
            bundle.putString("bgcolor", colstr)
            frag.arguments = bundle


//            context.
//            getfragmentmanagercallback().beginTransaction()
//                    .add(R.id.fragmentcontainer,frag)
//                    .addToBackStack("name1")
//                    .commit()
            (context as AppCompatActivity).supportFragmentManager.beginTransaction()
                    .add(R.id.fragmentcontainer, frag)
                    .addToBackStack("name1")
                    .commit()

        })

    }


    class RAViewHolder(iv: View) : RecyclerView.ViewHolder(iv) {

        lateinit var imageview: ImageView
        lateinit var titleTextView: TextView
        var color: Int = 0

        companion object {
            fun newInstance(iv: View): RAViewHolder {
                val raviewholder: RAViewHolder = RAViewHolder(iv)
                raviewholder.imageview = iv.thumbnailimageview
                raviewholder.titleTextView = iv.thumbnailTitle
                return raviewholder
            }
        }

    }
}