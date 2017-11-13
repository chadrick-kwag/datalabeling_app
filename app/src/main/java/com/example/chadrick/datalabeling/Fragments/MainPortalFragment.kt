package com.example.chadrick.datalabeling.Fragments

import android.graphics.Bitmap
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.Volley
import com.example.chadrick.datalabeling.R
import kotlinx.android.synthetic.main.mainportalfragment_layout.*

/**
 * Created by chadrick on 17. 11. 13.
 */

class MainPortalFragment : Fragment() {

    lateinit var photourl: String
    lateinit var username: String

    companion object {
        fun newInstance(): MainPortalFragment {
            return MainPortalFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.mainportalfragment_layout, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        photourl = arguments.getString("photourl")
        username = arguments.getString("displayname")

        profile_name.setText(username)
        // image request for photourl
        val imagerequest = ImageRequest(photourl, { bitmap -> profile_icon.setImageBitmap(bitmap) },
                0, 0, ImageView.ScaleType.CENTER_CROP, Bitmap.Config.RGB_565
                , { err -> err.printStackTrace() })
        val queue = Volley.newRequestQueue(context);
        queue.add(imagerequest)

        val menulistarray: ArrayList<String> = ArrayList()
        menulistarray.add("Main")
        menulistarray.add("Settings")

        val menuadapter: ArrayAdapter<String> = ArrayAdapter<String>(context, R.layout.menulist_item_layout, menulistarray)


        menulist.adapter = menuadapter
        menulist.setOnItemClickListener({ parent, view, position, id ->

            val itemtext = parent.getItemAtPosition(position)
            Log.d("kotlin", "itemtext=" + itemtext)
            if (itemtext.equals("Settings")) {
                fragmentManager.beginTransaction().add(R.id.mainportal_fragmentcontainer,
                        SettingsFragment.newInstance()).commit()

            } else if (itemtext.equals("Main")) {
                Log.d("chadrick","creating dataselect from navigationbar")
                val frag = DatasetSelectFragment()
                fragmentManager.beginTransaction().add(R.id.mainportal_fragmentcontainer,
                        frag).commit()
            }
        })

        // show dataset select fragment as the default
        Log.d("chadrick","creating dataselectfragment")
        val frag = DatasetSelectFragment()
        fragmentManager.beginTransaction().add(R.id.mainportal_fragmentcontainer, frag).commit()

    }


}