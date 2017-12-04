package com.example.chadrick.datalabeling

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.chadrick.datalabeling.Models.SMitem
import kotlinx.android.synthetic.main.sm_plaintype_layout.view.*
import kotlinx.android.synthetic.main.sm_toggletype_layout.view.*

/**
 * Created by chadrick on 17. 12. 4.
 */

class SettingsMenuAdapter(menuitemlist: ArrayList<SMitem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val menuitemlist = menuitemlist

    companion object {
        val TAG: String = this.javaClass.simpleName
    }

    class toggleTypeViewHolder(iv: View) : RecyclerView.ViewHolder(iv) {
        val title = iv.toggletype_layout_title_tv
        val switch = iv.toggletype_layout_switch

    }

    class plainTypeViewHolder(iv: View) : RecyclerView.ViewHolder(iv) {
        val title = iv.plaintype_layout_title_tv
    }

    override fun getItemCount(): Int {
        return menuitemlist.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            SMitem.TYPE_TOGGLE -> return toggleTypeViewHolder(LayoutInflater.from(parent?.context)
                    .inflate(R.layout.sm_toggletype_layout, parent, false))
            SMitem.TYPE_PLAIN -> return plainTypeViewHolder(LayoutInflater.from(parent?.context)
                    .inflate(R.layout.sm_plaintype_layout, parent, false))
            else -> {
                Log.d(TAG, "unrecognized viewtype detected")
                return plainTypeViewHolder(LayoutInflater.from(parent?.context)
                        .inflate(R.layout.sm_plaintype_layout, parent, false))
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val item = menuitemlist.get(position)

        when(holder?.itemViewType){
            SMitem.TYPE_TOGGLE -> {
                val toggleVH = holder as toggleTypeViewHolder
                toggleVH.title.text = item.title


            }
            SMitem.TYPE_PLAIN -> {
                val plainVH = holder as plainTypeViewHolder
                plainVH.title.text = item.title
                if(item.titleColor!= Color.WHITE){
                    plainVH.title.setTextColor(item.titleColor)
                }
            }
            else -> {
                Log.d(TAG,"unrecognized viewtype deteced in onBindViewHolder")
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = menuitemlist.get(position)
        return item.type
    }


}