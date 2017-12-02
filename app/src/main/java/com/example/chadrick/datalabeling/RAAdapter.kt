package com.example.chadrick.datalabeling

import android.app.FragmentManager
import android.content.Context
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
import kotlinx.android.synthetic.main.usermain_dataset_thumb_layout.view.*
import java.util.*

/**
 * Created by chadrick on 17. 12. 2.
 */

class RAAdapter : RecyclerView.Adapter<RAAdapter.RAViewHolder>() {


    lateinit var DSlist : ArrayList<DataSet>
    private val colorgen = BGColorRandomPicker()
    lateinit var context : Context


    companion object {
        fun newInstance(context: Context, dslist : ArrayList<DataSet>) : RAAdapter{
            val obj : RAAdapter = RAAdapter()
            obj.DSlist = dslist
            obj.context = context
            return obj
        }
    }



    override fun getItemCount(): Int {
        return DSlist.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RAViewHolder {
        val itemview : View = LayoutInflater.from( parent?.context).inflate(R.layout.usermain_dataset_thumb_layout, parent, false)

        return RAViewHolder.newInstance(itemview)
    }

    override fun onBindViewHolder(holder: RAViewHolder?, position: Int) {
        val dataset : DataSet = DSlist.get(position)
        // set the image for holder.imageview
        holder?.titleTextView?.setText(dataset.name)
        val colstr = colorgen.getRandomColor()
        Log.d("chadrick","colstr="+colstr)
        holder?.imageview?.setBackgroundColor(Color.parseColor(colstr) )
        holder?.imageview?.setOnClickListener({v:View ->
            val frag = DatasetProgressFragment()
            val bundle = Bundle()
            bundle.putString("ds",dataset.serialize())
            frag.arguments = bundle
            (context as AppCompatActivity).supportFragmentManager.beginTransaction()
                    .add(R.id.fragmentcontainer,frag)
                    .addToBackStack("name1")
                    .commit()

        })

    }


    class RAViewHolder(iv : View) : RecyclerView.ViewHolder(iv) {

        lateinit var imageview : ImageView
        lateinit var titleTextView : TextView

        companion object {
            fun newInstance(iv: View) : RAViewHolder {
                val raviewholder : RAViewHolder = RAViewHolder(iv)
                raviewholder.imageview = iv.thumbnailimageview
                raviewholder.titleTextView = iv.thumbnailTitle
                return raviewholder
            }
        }

    }
}