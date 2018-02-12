package com.example.chadrick.datalabeling.Models

import android.graphics.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import com.example.chadrick.datalabeling.CustomComponents.*
import com.example.chadrick.datalabeling.CustomViewPager
import com.example.chadrick.datalabeling.R
import com.example.chadrick.datalabeling.Util
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.util.*

/**
 * Created by chadrick on 18. 2. 12.
 */

class LabelDrawPad(inflater: LayoutInflater,
                   container: ViewGroup?,
                   drawBtnPressedCallback: () -> Boolean,
                   rectReadyCallback: (Rect) -> Unit,
                   imageFile: File,
                   customViewPager: CustomViewPager,
                   rectSelectedCallback: () -> Unit,
                   hideDeleteBtnCallback: () -> Unit
) {
    private val inflater = inflater
    private val container = container

    lateinit var rootview: View

    private val drawBtnPressedCallback = drawBtnPressedCallback

    lateinit var baseIV: DataImageImageView
    lateinit var rectIV: RenderedRectsImageView
    lateinit var drawIV: RectDrawImageView

    var image_width: Int = 0
    var image_height: Int = 0


    private val rectReadyCallback = rectReadyCallback

    private val customViewPager = customViewPager

    lateinit var mainCanvasPaint: Paint

    private val imageFile = imageFile
    lateinit var labelFile: File
    private var baseBitmap: Bitmap? = null
    private var rectBitmap: Bitmap? = null
    private val rectSelectedCallback = rectSelectedCallback
    private val hideDeleteBtnCallback = hideDeleteBtnCallback


    private val rectArrayList = ArrayList<Rect>()

    private val RECT_SELECT_AREA_PADDING = 50

    private var selectedRect: Rect? = null


    init {
        initElements()
    }


    private fun initElements() {

        rootview = inflater.inflate(R.layout.labeldrawpad_layout, container,
                false)
        baseIV = rootview.findViewById(R.id.baseiv)
        rectIV = rootview.findViewById(R.id.rectiv)
        drawIV = rootview.findViewById(R.id.drawiv)

        // need to register the IVs so that it can receive touch events
        val labelpadframelayout = rootview
                .findViewById<LabelPadFrameLayout>(R.id.labelpadframelayout)
        labelpadframelayout.baseIV = baseIV
        labelpadframelayout.maskIV = drawIV
        labelpadframelayout.rectIV = rectIV

        // create paint for maincanvas
        mainCanvasPaint = Paint()
        mainCanvasPaint.color = Color.rgb(255, 0, 0)
        mainCanvasPaint.strokeWidth = 5f
        mainCanvasPaint.style = Paint.Style.STROKE

        // dynamic width/height measure for drawIV
        val viewTreeObserver = drawIV.viewTreeObserver
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {

            override fun onGlobalLayout() {

                drawIV.passWH(drawIV.width, drawIV.height)
                drawIV.viewTreeObserver.removeOnGlobalLayoutListener(this)

            }
        })
        // create bitmap with imageFile and set up baseCanvas
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        options.inMutable = true
        val originalbitmap = BitmapFactory.decodeFile(imageFile.toString(), options)
        image_width = originalbitmap.width
        image_height = originalbitmap.height

        baseBitmap = Bitmap.createBitmap(originalbitmap.width, originalbitmap.height,
                Bitmap.Config.ARGB_8888)
        val basecanvas = Canvas(baseBitmap)
        basecanvas.drawBitmap(originalbitmap, 0f, 0f, null)
        // create blank bitmap
        rectBitmap = Bitmap
                .createBitmap(originalbitmap.width, originalbitmap.height, Bitmap.Config.ARGB_8888)
        val rectCanvas = Canvas(rectBitmap)
        // set baseIV
        baseIV.setImageBitmap(baseBitmap)
        baseIV.drawBtnPressedCallback = drawBtnPressedCallback


        // set rectIV
        rectIV.setImageBitmap(rectBitmap)
        rectIV.canvas = rectCanvas
        rectIV.customViewPager = customViewPager
        rectIV.addRectCallback = { rect -> rectArrayList.add(rect) }
        rectIV.saveLabelCallback = this::saveLabelFile
        rectIV.drawBtnpressedcallback = drawBtnPressedCallback
        rectIV.checkRectSelectCallback = this::checkRectSelect
        rectIV.unselectAnyIfExistCallback = this::unselectAnyIfExist

        drawIV.drawBtnpressedcallback = drawBtnPressedCallback
        drawIV.rectReadyCallback = rectReadyCallback

        // generate label json file based on image file
        val imagefilename = Util.getOnlyFilename(imageFile.name)
        val parentpath = imageFile.parent
        val printstr = parentpath + File.separator + imagefilename + ".json"
        labelFile = File(printstr)
        // check if labelfile exist. if exist, read it and draw them. if not exist, then do nothing.
        if (!labelFile.exists()) {
            // when json file doesn't exist

        } else {
            // if json file exists, then read it into a json object
            val readtext = StringBuilder()
            try {
                val bufferedReader = BufferedReader(FileReader(labelFile))
                var line: String?

                line = bufferedReader.readLine()

                while (line != null) {
                    readtext.append(line)
                    line = bufferedReader.readLine()
                }

            } catch (e: IOException) {
                e.printStackTrace()

            }

            val readresult = readtext.toString()
            try {
                val parsed = JSONObject(readresult)
                val bitmapfilename = parsed.getString("imgfile")
                val objects = parsed.getJSONArray("objects")
                for (i in 0 until objects.length()) {
                    val item = objects.getJSONObject(i)
                    val name = item.getString("name")
                    val rect = item.getJSONObject("rect")
                    val x1 = rect.getInt("x1")
                    val y1 = rect.getInt("y1")
                    val x2 = rect.getInt("x2")
                    val y2 = rect.getInt("y2")
                    val rectfromjson = Rect(x1, y1, x2, y2)
                    rectArrayList.add(rectfromjson)

                }

            } catch (e: JSONException) {
                e.printStackTrace()
            }

            // parsing the jsonfile finished.
            // now draw each rect in the maincanvas
            for (i in rectArrayList.indices) {
                val rectToDraw = rectArrayList.get(i)
                rectCanvas.drawRect(rectToDraw, mainCanvasPaint)
            }
            // invalidate to make sure that the new drawing are displayed
            rectIV.invalidate()

        }
    }


    private fun saveLabelFile(): Boolean {
        // when called, rewrite the labelfile with current info
        // first create a jsonobject and populate with current info
        val newRoot = JSONObject()
        try {
            newRoot.put("imgfile", imageFile.name)
            newRoot.put("w", image_width)
            newRoot.put("h", image_height)
            // create JSONarray object that contains all the current rects
            val rectarray = JSONArray()
            for (i in rectArrayList.indices) {
                val rect = rectArrayList[i]
                val `object` = JSONObject()
                `object`.put("name", "car")
                val jsonrect = JSONObject()
                jsonrect.put("x1", rect.left)
                jsonrect.put("y1", rect.top)
                jsonrect.put("x2", rect.right)
                jsonrect.put("y2", rect.bottom)
                `object`.put("rect", jsonrect)
                rectarray.put(`object`)
            }
            newRoot.put("objects", rectarray)

        } catch (e: JSONException) {
            e.printStackTrace()
            return false
        }

        // newroot is ready. write it to labelFile
        try {
            val bufferedWriter = BufferedWriter(FileWriter(labelFile))
            bufferedWriter.write(newRoot.toString())
            bufferedWriter.flush()
            bufferedWriter.close()
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }

        // finished overwriting the labelfile
        // job finished
        // return true since we have successfully written it.
        return true

    }

    fun enableTouches() {

        baseIV.setTouchEnable(true)
        rectIV.setTouchEnable(true)
        drawIV.TouchEnabled = true
        //    drawIV.setTouchEnable(true);
    }

    fun disableTouches() {

        baseIV.setTouchEnable(false)
        rectIV.setTouchEnable(false)
        drawIV.TouchEnabled = false
        //    drawIV.setTouchEnable(false);
    }

    private fun checkRectSelect(point: Point): Boolean {
        // check with all rects. the first hit will be set as selected and
        // this method will return true
        // if there are no hits, then return false
        val touch_x = point.x
        val touch_y = point.y
        for (rect in rectArrayList) {
            val biggerRect = Rect(rect.left - RECT_SELECT_AREA_PADDING,
                    rect.top - RECT_SELECT_AREA_PADDING,
                    rect.right + RECT_SELECT_AREA_PADDING,
                    rect.bottom + RECT_SELECT_AREA_PADDING)
            val smallerRect = Rect(rect.left + RECT_SELECT_AREA_PADDING,
                    rect.top + RECT_SELECT_AREA_PADDING,
                    rect.right - RECT_SELECT_AREA_PADDING,
                    rect.bottom - RECT_SELECT_AREA_PADDING)
            // assume that there are no conflicts with the sizes of the bigger and smaller rect
            var biggercontain = false
            var smallercontain = false
            if (biggerRect.contains(touch_x, touch_y)) {
                biggercontain = true
            }
            if (smallerRect.contains(touch_x, touch_y)) {
                smallercontain = true
            }
            if (biggercontain  && !smallercontain) {
                // okay so a hit was found, and we know which rectangle it is.
                // we need to redraw it, and it will eventually be done with
                // rectIV
                // the rectangle of interest is already drawn.
                // instead of redrawing the whole thing(including othe rectangles),
                // why not just overdraw the rectangle of interest with anther paint?
                // first draw unselected rect of the previous existing selectedRect

                selectedRect?.let { rectIV.drawUnselectedRect(it) }
                selectedRect = rect

                selectedRect?.let { rectIV.drawSelectedRect(it) }
                // display delete btn
                rectSelectedCallback()
                return true
            }

        }
        return false
    }

    fun drawRect() {

        rectIV.drawRect()
    }

    fun eraseDrawnRect() {

        drawIV.eraseall()
    }

    /**
     * this will delete the selectedRect from the list
     * and redraw canvas without that rect
     */
    fun deleteSelectedRect() {
        // delete the selectedrect
        if (selectedRect == null) {
            return
        }
        // remove it from rectarray
        selectedRect?.let { rectArrayList.remove(it) }
        // nullify the selectedrect variable
        selectedRect = null
        redrawrects()
        // updatelabelfile
        saveLabelFile()

    }

    /**
     * this method literally sets the selectedRect to null
     */
    fun removeSelectedRect() {

        this.selectedRect = null
    }

    /**
     * this method will redraw the rects with unselected color
     */
    fun redrawrects() {

        rectBitmap?.eraseColor(Color.TRANSPARENT)
        for (rect in rectArrayList) {
            rectIV.drawUnselectedRect(rect)
        }
    }

    private fun unselectAnyIfExist() {

        if (selectedRect == null) {
            return
        }
        removeSelectedRect()
        redrawrects()
        hideDeleteBtnCallback()

    }

}