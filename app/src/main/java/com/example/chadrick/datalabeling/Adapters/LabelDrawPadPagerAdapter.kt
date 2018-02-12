package com.example.chadrick.datalabeling.Adapters

import android.content.Context
import android.graphics.Rect
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.example.chadrick.datalabeling.CustomViewPager
import com.example.chadrick.datalabeling.Models.LabelDrawPad2
import java.io.File

/**
 * Created by chadrick on 18. 2. 12.
 */

class LabelDrawPadPagerAdapter(context: Context, imagefiles : ArrayList<File>,
                               labeldrawpadPager : CustomViewPager,
                               drawBtnpressedcallback: ()->Boolean,
                               rectReadyCallback : (Rect)->Unit,
                               rectSelectedCallback : ()->Unit,
                               hideDeleteBtnCallback : ()->Unit,
                               screenwidth : Int,
                               screenheight: Int
                               ) : PagerAdapter(){



    private val context = context
    private val imagefiles = imagefiles
    private val labeldrawpadPager = labeldrawpadPager
    private val drawBtnpressedcallback = drawBtnpressedcallback
    private val rectReadyCallback = rectReadyCallback
    private val rectSelectedCallback = rectSelectedCallback
    private val hideDeleteBtnCallback = hideDeleteBtnCallback
    private val screenwidth = screenwidth
    private val screenheight = screenheight


    private val labelDrawPadHashMap : HashMap<Int, LabelDrawPad2> = HashMap<Int, LabelDrawPad2>()


    override fun getCount(): Int {
        return imagefiles.size
    }

    override fun isViewFromObject(view: View?, `object`: Any?): Boolean {
        return view == (`object` as FrameLayout)
    }

    override fun instantiateItem(container: ViewGroup?, position: Int): Any {
//        val rootview = super.instantiateItem(container, position)
        val inflator = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

//        val labeldrawpad = LabelDrawPad.LabelDrawPadBuilder(inflator,container,position)
//                .setDrawBtnPressedCallback(drawBtnpressedcallback)
//                .setCustomViewPager(labeldrawpadPager)
//                .setMaskRectReadyCallback(rectReadyCallback)
//                .setImageFile(imagefiles.get(position))
//                .setRectSelectedCallback(rectSelectedCallback)
//                .setHideDeleteBtnCallback(hideDeleteBtnCallback)
//                .build()

        val labeldrawpad = LabelDrawPad2(inflater = inflator,
                container = container,
                drawBtnPressedCallback = drawBtnpressedcallback,
                rectReadyCallback = rectReadyCallback,
                imageFile = imagefiles[position],
                customViewPager = labeldrawpadPager,
                rectSelectedCallback = rectSelectedCallback,
                hideDeleteBtnCallback = hideDeleteBtnCallback
                )
        labelDrawPadHashMap.put(position,labeldrawpad)

        val rootview = labeldrawpad.rootview
        (container as ViewPager).addView(rootview)

        return rootview


    }

    override fun destroyItem(container: ViewGroup?, position: Int, `object`: Any?) {
//        super.destroyItem(container, position, `object`)

        (container as ViewPager).removeView(`object` as FrameLayout)

        labelDrawPadHashMap.remove(position)
    }

    fun getLabelDrawPad(position: Int): LabelDrawPad2?{
        return labelDrawPadHashMap[position]
    }


}