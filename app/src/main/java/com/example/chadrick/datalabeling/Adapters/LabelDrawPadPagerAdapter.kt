package com.example.chadrick.datalabeling.Adapters

import android.content.Context
import android.graphics.Rect
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.example.chadrick.datalabeling.CustomComponents.DataImageViewPager
import com.example.chadrick.datalabeling.Models.LabelDrawPad
import java.io.File

/**
 * Created by chadrick on 18. 2. 12.
 */

class LabelDrawPadPagerAdapter(context: Context, imagefiles : ArrayList<File>,
                               dataImageViewPager : DataImageViewPager,
                               drawBtnpressedcallback: ()->Boolean,
                               rectReadyCallback : (Rect)->Unit,
                               rectSelectedCallback : ()->Unit,
                               hideDeleteBtnCallback : ()->Unit,
                               screenwidth : Int,
                               screenheight: Int
                               ) : PagerAdapter(){



    private val context = context
    private val imagefiles = imagefiles
    private val dataImageViewPager = dataImageViewPager
    private val drawBtnpressedcallback = drawBtnpressedcallback
    private val rectReadyCallback = rectReadyCallback
    private val rectSelectedCallback = rectSelectedCallback
    private val hideDeleteBtnCallback = hideDeleteBtnCallback


    private val labelDrawPadHashMap : HashMap<Int, LabelDrawPad> = HashMap<Int, LabelDrawPad>()


    override fun getCount(): Int {
        return imagefiles.size
    }

    override fun isViewFromObject(view: View?, `object`: Any?): Boolean {
        return view == (`object` as FrameLayout)
    }

    override fun instantiateItem(container: ViewGroup?, position: Int): Any {
        val inflator = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater


        val labeldrawpad = LabelDrawPad(inflater = inflator,
                container = container,
                drawBtnPressedCallback = drawBtnpressedcallback,
                rectReadyCallback = rectReadyCallback,
                imageFile = imagefiles[position],
                dataImageViewPager = dataImageViewPager,
                rectSelectedCallback = rectSelectedCallback,
                hideDeleteBtnCallback = hideDeleteBtnCallback
                )
        labelDrawPadHashMap.put(position,labeldrawpad)

        val rootview = labeldrawpad.rootview
        (container as ViewPager).addView(rootview)

        return rootview


    }

    override fun destroyItem(container: ViewGroup?, position: Int, `object`: Any?) {

        (container as ViewPager).removeView(`object` as FrameLayout)

        labelDrawPadHashMap.remove(position)
    }

    fun getLabelDrawPad(position: Int): LabelDrawPad?{
        return labelDrawPadHashMap[position]
    }


}