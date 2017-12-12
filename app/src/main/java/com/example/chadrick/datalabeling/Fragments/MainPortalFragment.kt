package com.example.chadrick.datalabeling.Fragments

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

            val fraglist = fragmentManager.fragments



            val itemtext = parent.getItemAtPosition(position)
            Log.d("kotlin", "itemtext=" + itemtext)
            if (itemtext.equals("Settings")) {

                if (fragmentManager.findFragmentByTag("settings") != null) {

                } else {
                    fragmentManager.beginTransaction().replace(R.id.mainportal_fragmentcontainer,
                            SettingsFragment.instance, "settings").commit()
                }


            } else if (itemtext.equals("Main")) {

                for(frag in fragmentManager.fragments){
                    Log.d("bitcoin","print in main click frag="+frag.toString())
                }

                if (fragmentManager.findFragmentByTag("main") != null) {

                } else {
//                    val frag = DatasetSelectFragment()
                    val frag = UserMainFragment.instance
                    // check if already added


                    if(!frag.isAdded){
                        fragmentManager.beginTransaction().replace(R.id.mainportal_fragmentcontainer,
                                frag, "usermain").commit()
                    }

                }


            }

            drawer_layout.closeDrawers()
//            drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        })

        // show dataset select fragment as the default
//        val frag = DatasetSelectFragment()
        val frag = UserMainFragment.instance
        Log.d("bitcoin","normal fetched usermain frag = "+frag.toString())
        if(!frag.isAdded){
            Log.d("bitcoin","usermain frag does not exist. creating one")
            fragmentManager.beginTransaction().add(R.id.mainportal_fragmentcontainer, frag, "usermain").commit()
            fragmentManager.addOnBackStackChangedListener {
                Log.d("fuck", "inside backstackchangedlistener from Mainportalfragment")

                val fetchedfrag = fragmentManager?.findFragmentByTag("usermain")

                fetchedfrag?.let {
                    if (fetchedfrag.isVisible) {
                        fetchedfrag.onResume()
                    }
                }

                // for controlling the drawer

                val fetchedfrag2 = fragmentManager?.findFragmentByTag("mainportal")

                // print fragments in stack

                var isontop: Boolean = false

                val stacks = fragmentManager.fragments

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
        }
        else{
            Log.d("bitcoin","usermain exist. showing it")
            //if already added
            // fetch the existing fragment and add it
//            val fetchedfrag = fragmentManager.findFragmentByTag("usermain")

            for(frag in fragmentManager.fragments){
                Log.d("bitcoin","print frag = "+frag.toString())
            }

            fragmentManager.beginTransaction().remove(UserMainFragment.instance).commit()
//            childFragmentManager.beginTransaction().remove(UserMainFragment.instance).commit()

            for(frag in fragmentManager.fragments){
                Log.d("bitcoin","print after frag = "+frag.toString())
            }


//
//            val fetchedfrag = UserMainFragment.instance
////            fragmentManager.beginTransaction().show(fetchedfrag).commit();
//            fragmentManager.beginTransaction().replace(R.id.mainportal_fragmentcontainer,
//                    fetchedfrag, "usermain").commit()
        }
    }
}