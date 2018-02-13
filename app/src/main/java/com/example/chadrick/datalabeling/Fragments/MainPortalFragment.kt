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
    lateinit var usermainfrag: UserMainFragment


    companion object {
        fun newInstance(): MainPortalFragment {
            return MainPortalFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // when created, add a backstack listener to the main activity backstack to call
        // updateElements of usermainfragment whenever the mainportalfragment is resurfaced.
        fragmentManager.addOnBackStackChangedListener {
            // if the fragment to show is mainportalfragment,
            // then call UserMainFragment#updateElement
            usermainfrag?.updateElements()
        }

        return inflater?.inflate(R.layout.mainportalfragment_layout, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {

        var isRecreating = false

        savedInstanceState?.let { isRecreating = true }



        photourl = arguments.getString("photourl")
        username = arguments.getString("displayname")

        profile_name.setText(username)
        // image request for photourl
        val imagerequest = ImageRequest(photourl, { bitmap -> profile_icon.setImageBitmap(bitmap) },
                0, 0, ImageView.ScaleType.CENTER_CROP, Bitmap.Config.RGB_565
                , { err -> err.printStackTrace() })
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
            if (itemtext.equals("Settings")) {

                if (childFragmentManager.findFragmentByTag("settings") != null) {

                } else {
                    if (!isRecreating) {
                        childFragmentManager.beginTransaction().replace(R.id.mainportal_fragmentcontainer,
                                SettingsFragment(), "settings").commit()
                    }

                }


            } else if (itemtext.equals("Main")) {

                if (childFragmentManager.findFragmentByTag("usermain") != null) {
                } else if (!isRecreating) {
                    childFragmentManager.beginTransaction().replace(R.id.mainportal_fragmentcontainer,
                            usermainfrag, "usermain").commit()
                }
            }

            drawer_layout.closeDrawers()

        })


        // check if usermainfragment exists in childfragmentmanager
        val checkfrag = childFragmentManager.findFragmentByTag("usermain")

        if (checkfrag == null && !isRecreating) {

            // this is when the mainportalfrag is created("entered") for the very first time

            val frag = UserMainFragment()
            usermainfrag = frag
            childFragmentManager.beginTransaction().add(R.id.mainportal_fragmentcontainer, frag, "usermain").commit()
            childFragmentManager.addOnBackStackChangedListener {

                val fetchedfrag = childFragmentManager?.findFragmentByTag("usermain")

                fetchedfrag?.let {
                    if (fetchedfrag.isVisible) {
                        fetchedfrag.onResume()
                    }
                }

                // for controlling the drawer


                val fetchedfrag2 = childFragmentManager?.findFragmentByTag("mainportal")
                val stacks = childFragmentManager.fragments
                val lastfrag = stacks.last()

                fetchedfrag2?.let {

                    if (lastfrag is MainPortalFragment || lastfrag is UserMainFragment) {
                        drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                    } else {
                        drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    }

                }


            }
        }

    }
}