package com.example.chadrick.datalabeling.Fragments

import android.graphics.Point
import android.graphics.Rect
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.example.chadrick.datalabeling.FullScreenImageAdapter
import com.example.chadrick.datalabeling.Models.DataSet
import com.example.chadrick.datalabeling.Models.ZoomOutPageTransformer
import com.example.chadrick.datalabeling.R
import com.example.chadrick.datalabeling.Util
import kotlinx.android.synthetic.main.datasetprogressfragment2_layout.*
import kotlinx.android.synthetic.main.imageviewerfrag_layout.*
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.util.*

/**
 * Created by chadrick on 18. 2. 11.
 */

class LabelingFragment() : Fragment() {
    private var isDrawBtnPressed: Boolean = false
    private lateinit var pagerAdapter: FullScreenImageAdapter
    private var currentPageIndex: Int = 0
    private var totalImgNumber: Int = 0
    private lateinit var updateStatCallback : ()->Unit
    private lateinit var receivedRect : Rect

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater?.inflate(R.layout.imageviewerfrag_layout, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initButtonActions()
        initPagerAdapter()
        initPager()

    }

    fun updateIsDrawBtnPressed(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_UP -> isDrawBtnPressed = false
            MotionEvent.ACTION_DOWN -> isDrawBtnPressed = true
        }

        return false

    }

    fun initButtonActions(){
        drawbtn.setOnTouchListener { v, event -> updateIsDrawBtnPressed(event) }
        yesbtn.setOnClickListener { v->
            val labelpad = pagerAdapter.getLabelDrawPad(currentPageIndex)
            labelpad.enableTouches()

            // hide yes and no btn from layout
            yesbtn.visibility = View.INVISIBLE
            nobtn.visibility = View.INVISIBLE

            // enable draw btn
            drawbtn.visibility = View.VISIBLE

            // actually draw rectangle in mainIV
            labelpad.drawRect()

            // clear the temp rectangle in maskIV
            labelpad.eraseDrawnRect()
        }

        nobtn.setOnClickListener { v->
            val labelpad = pagerAdapter.getLabelDrawPad(currentPageIndex)
            labelpad.enableTouches()

            // hide yes and no btn from layout
            yesbtn.visibility = View.INVISIBLE
            nobtn.visibility = View.INVISIBLE
            // enable draw btn
            drawbtn.visibility = View.VISIBLE
            //erase maskIV
            labelpad.eraseDrawnRect()
        }

        deletebtn.setOnClickListener{ v->
            val labelpad = pagerAdapter.getLabelDrawPad(currentPageIndex)
            labelpad.deleteSelectedRect()

            delete_btn.visibility = View.INVISIBLE
        }
    }

    fun initPagerAdapter(){
        // fetch ds
        var ds : DataSet
        try {
            ds = DataSet.deserialize(arguments.getString("ds"))
        }
        catch ( e : JSONException ){
            return
        }

        // get filelist
        val dir = File(ds.dirstr)
        val imagefiles = Util.getImageFileList(dir)

        if(imagefiles.size==0){
            return
        }

        totalImgNumber = imagefiles.size

        // sort the images by name
        Collections.sort(imagefiles)

        val display = activity.windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val screenwidth = size.x
        val screenheight = size.y

        pagerAdapter = FullScreenImageAdapter(context, imagefiles, customviewpager,
                this::getIsDrawBtnPressed, screenwidth, screenheight)

        // setup pagerAdapter callbacks
        pagerAdapter.passRectReadyCallback { rect->
            // make sure that drawbtnpressed is reset to false
            // so that subsequent draws can be processed
            isDrawBtnPressed = false
            // just to make sure the ui of draw btn is restored
            drawbtn.setBackgroundResource(R.color.buttonreleasedcolor)

            // set the receivedRect
            receivedRect = rect
            // disable the touch handler in the two IVs
            // first access the appropriate page
            val labelDrawPad = pagerAdapter.getLabelDrawPad(currentPageIndex)
            // fetch the two IVs
            labelDrawPad.disableTouches()
            // show yes/no btns
            yesbtn.visibility = View.VISIBLE
            nobtn.visibility = View.VISIBLE
            drawbtn.visibility = View.INVISIBLE
        }

        pagerAdapter.passRectSelectedCallback(this::rectSelectedCallback)
        pagerAdapter.passHideDeleteBtnCallback( this::hideDeleteBtnCallback)


    }

    fun initPager(){
        val zoomOutPageTransformer = ZoomOutPageTransformer()

        customviewpager.setPageTransformer(true, zoomOutPageTransformer)
        customviewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                delete_btn.visibility = View.INVISIBLE
                // and deselect any selected rects
                val labelDrawPad = pagerAdapter.getLabelDrawPad(position)
                labelDrawPad.removeSelectedRect()
                // redraw the rects
                labelDrawPad.redrawrects()
            }

            override fun onPageSelected(position: Int) {
                // update the page position
                currentPageIndex = position
                updatePageNumberText(position)
            }
        })

        customviewpager.adapter= pagerAdapter
        customviewpager.currentItem = 0
        updatePageNumberText(0)
    }

    fun getIsDrawBtnPressed(): Boolean{
        return isDrawBtnPressed
    }

    fun updatePageNumberText(position: Int){
        val newstring = Integer.toString(currentPageIndex + 1) + "/" + Integer.toString(totalImgNumber)
        pagenumber.text = newstring
    }

    override fun onPause() {
        // updatestat
        updateStatCallback()

        super.onPause()
    }

    fun passUpdateStatCallback(callback: ()->Unit){
        updateStatCallback = callback
    }

    private fun rectSelectedCallback(){
        deletebtn.visibility = View.VISIBLE
    }

    private fun hideDeleteBtnCallback(){
        deletebtn.visibility = View.INVISIBLE
    }


}