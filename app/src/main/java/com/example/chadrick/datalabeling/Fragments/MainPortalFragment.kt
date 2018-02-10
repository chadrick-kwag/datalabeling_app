package com.example.chadrick.datalabeling.Fragments

import android.app.Application
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.DrawerLayout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.Volley
import com.example.chadrick.datalabeling.Models.App
import com.example.chadrick.datalabeling.Models.requestqueueSingleton
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
        Log.d("fuck", "creatview of mainPortalFragment")
        return inflater?.inflate(R.layout.mainportalfragment_layout, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {

        var isRecreating = false

        savedInstanceState?.let { isRecreating = true }

        Log.d("bitcoin", "isRecreating=" + isRecreating)


        photourl = arguments.getString("photourl")
        username = arguments.getString("displayname")

        profile_name.setText(username)
        // image request for photourl
        val imagerequest = ImageRequest(photourl, { bitmap -> profile_icon.setImageBitmap(bitmap) },
                0, 0, ImageView.ScaleType.CENTER_CROP, Bitmap.Config.RGB_565
                , { err -> err.printStackTrace() })
//        val queue = Volley.newRequestQueue(App.applicationContext())
        val queue = requestqueueSingleton.getQueue()
        queue.add(imagerequest)


        val menulistarray: ArrayList<String> = ArrayList()
        menulistarray.add("Main")
        menulistarray.add("Settings")

        val menuadapter: ArrayAdapter<String> = ArrayAdapter<String>(App.applicationContext(), R.layout.menulist_item_layout, menulistarray)


        menulist.adapter = menuadapter
        menulist.setOnItemClickListener({ parent, view, position, id ->

            val fraglist = childFragmentManager.fragments


            val itemtext = parent.getItemAtPosition(position)
            Log.d("kotlin", "itemtext=" + itemtext)
            if (itemtext.equals("Settings")) {

                if (childFragmentManager.findFragmentByTag("settings") != null) {

                } else {
                    if (!isRecreating) {
                        childFragmentManager.beginTransaction().replace(R.id.mainportal_fragmentcontainer,
                                SettingsFragment(), "settings").commit()
                    }

                }


            } else if (itemtext.equals("Main")) {

                for (frag in childFragmentManager.fragments) {
                    Log.d("bitcoin", "print in main click frag=" + frag.toString())
                }

                if (childFragmentManager.findFragmentByTag("usermain") != null) {

                } else if (!isRecreating) {
                    // check if already exist in childfragmentmanager
//                    val frag = DatasetSelectFragment()
                    val frag = UserMainFragment()


                    Log.d("bitcoin", "usermainfragment not found in childfragmentmanager")
                    childFragmentManager.beginTransaction().replace(R.id.mainportal_fragmentcontainer,
                            frag, "usermain").commit()


                } else {
                }


            }

            drawer_layout.closeDrawers()
//            drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        })

        // show dataset select fragment as the default
//        val frag = DatasetSelectFragment()

        // check if usermainfragment exists in childfragmentmanager
        val checkfrag = childFragmentManager.findFragmentByTag("usermain")

        if (checkfrag == null && !isRecreating) {

            val frag = UserMainFragment()
            Log.d("bitcoin", "usermain frag does not exist. creating one")
            childFragmentManager.beginTransaction().add(R.id.mainportal_fragmentcontainer, frag, "usermain").commit()
            childFragmentManager.addOnBackStackChangedListener {
                Log.d("fuck", "inside backstackchangedlistener from Mainportalfragment")

                val fetchedfrag = childFragmentManager?.findFragmentByTag("usermain")

                fetchedfrag?.let {
                    if (fetchedfrag.isVisible) {
                        fetchedfrag.onResume()
                    }
                }

                // for controlling the drawer

                val fetchedfrag2 = childFragmentManager?.findFragmentByTag("mainportal")

                // print fragments in stack

                var isontop: Boolean = false

                val stacks = childFragmentManager.fragments

                for (item in stacks) {
                    Log.d("nvidia", "stack: " + item.toString())
                }

                val lastfrag = stacks.last()

                Log.d("nvidia", "lastfrag=" + lastfrag.toString())


                fetchedfrag2?.let {


                    if (lastfrag is MainPortalFragment || lastfrag is UserMainFragment) {
                        Log.d("nvidia", "mainportal on top. enable drawer")
                        drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                    } else {
                        Log.d("nvidia", "mainportal not on top. disable drawer")
                        drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    }

                }


            }
        } else {
            Log.d("bitcoin", "usermainfrag not created in mainportal")
        }

    }
}