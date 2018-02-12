package com.example.chadrick.datalabeling.CustomComponents

import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.example.chadrick.datalabeling.Callback
import java.lang.ref.WeakReference

/**
 * Created by chadrick on 18. 2. 12.
 */

class DataImageImageView @JvmOverloads constructor(context: Context,
                                                   attrs: AttributeSet? = null,
                                                   defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {
    internal var matrix: Matrix
    lateinit var inverseMatrix: Matrix


    // We can be in one of these 3 states
    internal val NONE = 0
    internal val DRAG = 1
    internal val ZOOM = 2
    internal var mode = NONE

    // Remember some things for zooming
    internal var last = PointF()
    internal var start = PointF()
    internal var minScale = 1f
    internal var maxScale = 5f
    internal var m: FloatArray

    internal var viewWidth: Int = 0
    internal var viewHeight: Int = 0
    internal val CLICK = 3
    internal var saveScale = 1f
    private var origWidth: Float = 0.toFloat()
    private var origHeight: Float = 0.toFloat()
    internal var oldMeasuredWidth: Int = 0
    internal var oldMeasuredHeight: Int = 0

    internal var mScaleDetector: ScaleGestureDetector

    private val savedcontext = context
    private var touchEnable = true

    lateinit var drawBtnpressedcallback: () -> Boolean


    private val MOVE_CLICK_LIMIT = 5

    init {
        super.setClickable(true)
        mScaleDetector = ScaleGestureDetector(savedcontext, ScaleListener())
        matrix = Matrix()
        m = FloatArray(9)

        setImageMatrix(matrix)
        setScaleType(ImageView.ScaleType.MATRIX)


        setOnTouchListener(OnTouchListener { v, event ->
            //        Log.d(TAG,"touch handler of touchIV");

            // if touch is disabled, do nothing.
            if (!touchEnable) {
                return@OnTouchListener true
            }

            mScaleDetector.onTouchEvent(event)
            val curr = PointF(event.x, event.y)


            // check if the drawBtn is pressed or not.
            val drawbtnpressed: Boolean

            if (::drawBtnpressedcallback.isInitialized) {
                drawbtnpressed = drawBtnpressedcallback()
            } else {
                drawbtnpressed = false
            }


            when (event.action) {
                MotionEvent.ACTION_DOWN -> {


                    if (drawbtnpressed) {

                        // calcuate the inversematrix
                        inverseMatrix = Matrix(matrix)
                        inverseMatrix.invert(inverseMatrix)

                        // then get the absolute x,y values
                        event.transform(inverseMatrix)

                        last.set(event.x, event.y)
                        start.set(last)
                        mode = DRAG


                    } else {
                        last.set(curr)
                        start.set(last)
                        mode = DRAG
                    }
                }

                MotionEvent.ACTION_MOVE -> run {
                    if (mode == DRAG) {

                        if (drawbtnpressed) {
                            // we are not drawing temp rects here. so do nothing.


                        } else {
                            val deltaX = curr.x - last.x
                            val deltaY = curr.y - last.y

                            if (deltaX < MOVE_CLICK_LIMIT && deltaY < MOVE_CLICK_LIMIT) {
                                // do not move if it is a small movement.
                                return@run
                            }


                            val fixTransX = getFixDragTrans(deltaX, viewWidth.toFloat(),
                                    origWidth * saveScale)
                            val fixTransY = getFixDragTrans(deltaY, viewHeight.toFloat(),
                                    origHeight * saveScale)
                            matrix.postTranslate(fixTransX, fixTransY)
                            fixTrans()
                            last.set(curr.x, curr.y)

                        }

                    }
                }

                MotionEvent.ACTION_UP -> {
                    mode = NONE
                    if (drawbtnpressed) {


                    } else {
                        val xDiff = Math.abs(curr.x - start.x).toInt()
                        val yDiff = Math.abs(curr.y - start.y).toInt()
                        if (xDiff < MOVE_CLICK_LIMIT && yDiff < MOVE_CLICK_LIMIT)
                            performClick()
                    }
                }

                MotionEvent.ACTION_POINTER_UP -> mode = NONE
            }


//            setImageMatrix(matrix)
            imageMatrix= matrix
            invalidate()
            true // indicate event was handled
        })
    }


    inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            mode = ZOOM
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            var mScaleFactor = detector.scaleFactor
            val origScale = saveScale
            saveScale *= mScaleFactor
            if (saveScale > maxScale) {
                saveScale = maxScale
                mScaleFactor = maxScale / origScale
            } else if (saveScale < minScale) {
                saveScale = minScale
                mScaleFactor = minScale / origScale
            }


            if (origWidth * saveScale <= viewWidth || origHeight * saveScale <= viewHeight)
                matrix.postScale(mScaleFactor, mScaleFactor, (viewWidth / 2).toFloat(),
                        (viewHeight / 2).toFloat())
            else
                matrix.postScale(mScaleFactor, mScaleFactor,
                        detector.focusX, detector.focusY)

            fixTrans()
            return true
        }
    }


    fun fixTrans() {
        matrix.getValues(m)
        val transX = m[Matrix.MTRANS_X]
        val transY = m[Matrix.MTRANS_Y]

        val fixTransX = getFixTrans(transX, viewWidth.toFloat(), origWidth * saveScale)
        val fixTransY = getFixTrans(transY, viewHeight.toFloat(), origHeight * saveScale)

        if (fixTransX != 0f || fixTransY != 0f)
            matrix.postTranslate(fixTransX, fixTransY)
    }

    fun getFixTrans(trans: Float, viewSize: Float, contentSize: Float): Float {
        val minTrans: Float
        val maxTrans: Float

        if (contentSize <= viewSize) {
            minTrans = 0f
            maxTrans = viewSize - contentSize
        } else {
            minTrans = viewSize - contentSize
            maxTrans = 0f
        }

        if (trans < minTrans)
            return -trans + minTrans
        return if (trans > maxTrans) -trans + maxTrans else 0f
    }

    fun getFixDragTrans(delta: Float, viewSize: Float, contentSize: Float): Float {
        return if (contentSize <= viewSize) {
            0f
        } else delta
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        viewWidth = View.MeasureSpec.getSize(widthMeasureSpec)
        viewHeight = View.MeasureSpec.getSize(heightMeasureSpec)

        //
        // Rescales image on rotation
        //
        if (oldMeasuredHeight == viewWidth && oldMeasuredHeight == viewHeight
                || viewWidth == 0 || viewHeight == 0)
            return
        oldMeasuredHeight = viewHeight
        oldMeasuredWidth = viewWidth

        if (saveScale == 1f) {
            // Fit to screen.
            val scale: Float

            val drawable = getDrawable()
            if (drawable == null || drawable!!.getIntrinsicWidth() == 0
                    || drawable!!.getIntrinsicHeight() == 0)
                return
            val bmWidth = drawable!!.getIntrinsicWidth()
            val bmHeight = drawable!!.getIntrinsicHeight()

            Log.d("bmSize", "bmWidth: $bmWidth bmHeight : $bmHeight")

            val scaleX = viewWidth.toFloat() / bmWidth.toFloat()
            val scaleY = viewHeight.toFloat() / bmHeight.toFloat()
            scale = Math.min(scaleX, scaleY)
            matrix.setScale(scale, scale)

            // Center the image
            var redundantYSpace = viewHeight.toFloat() - scale * bmHeight.toFloat()
            var redundantXSpace = viewWidth.toFloat() - scale * bmWidth.toFloat()
            redundantYSpace /= 2.toFloat()
            redundantXSpace /= 2.toFloat()

            matrix.postTranslate(redundantXSpace, redundantYSpace)

            origWidth = viewWidth - 2 * redundantXSpace
            origHeight = viewHeight - 2 * redundantYSpace
            setImageMatrix(matrix)
        }
        fixTrans()
    }



    fun setTouchEnable(value: Boolean) {
        this.touchEnable = value
    }
}