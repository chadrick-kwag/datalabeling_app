package com.example.chadrick.datalabeling.CustomComponents

import android.content.Context
import android.graphics.*
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.ImageView
import com.example.chadrick.datalabeling.Util

/**
 * Created by chadrick on 18. 2. 12.
 */

class RenderedRectsImageView @JvmOverloads constructor(context: Context,
                                                       attrs: AttributeSet? = null,
                                                       defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {


    val savedContext = context

    internal var matrix: Matrix
    private var inverseMatrix: Matrix? = null

    // We can be in one of these 3 states
    internal val NONE = 0
    internal val DRAG = 1
    internal val ZOOM = 2
    var mode = NONE

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


    lateinit var dataImageViewPager: DataImageViewPager
    lateinit var drawBtnpressedcallback: () -> Boolean
    lateinit var canvas : Canvas
    private val paint = Paint()
    private val selectedpaint = Paint()
    private val transparentpaint = Paint()
    private var touchEnable = true
    private var savedrect: Rect? = null


//    private var addRectCallback: Consumer<Rect>? = null
    lateinit var addRectCallback : (Rect)->Unit
    lateinit var saveLabelCallback: ()->Boolean
//    private var saveLabelCallback: Runnable? = null
    lateinit var checkRectSelectCallback: (Point) -> Boolean
    lateinit var unselectAnyIfExistCallback: () -> Unit

    private val sendpoint = Point()

    private val TAG = this.javaClass.simpleName

    private val MOVE_CLICK_LIMIT = 5

    init {
        super.setClickable(true)
        mScaleDetector = ScaleGestureDetector(context, ScaleListener())
        matrix = Matrix()
        m = FloatArray(9)
        imageMatrix = matrix
        scaleType = ImageView.ScaleType.MATRIX

        // setup paint
        paint.color = Color.rgb(255, 63, 20)
        paint.strokeWidth = 5f
        paint.style = Paint.Style.STROKE

        // setup selected paint
        selectedpaint.color = Color.rgb(0, 255, 0)
        selectedpaint.strokeWidth = 5f
        selectedpaint.style = Paint.Style.STROKE

        // setup transparent paint
        transparentpaint.color = Color.TRANSPARENT
        transparentpaint.strokeWidth = 5f
        transparentpaint.style = Paint.Style.STROKE

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
            if (drawBtnpressedcallback != null) {
                drawbtnpressed = drawBtnpressedcallback()
            } else {
                Log.d(TAG, "drawbtnpressedcallback is null")
                drawbtnpressed = false
            }

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {

                    Log.d(TAG, "mainIV x:" + event.x + ", y:" + event.y)

                    if (drawbtnpressed) {

                        // calcuate the inversematrix
                        inverseMatrix = Matrix(matrix)
                        inverseMatrix?.let { it.invert(it) }

                        // then get the absolute x,y values
                        event.transform(inverseMatrix)

                        // when draw btn is not pressed save the last,start with current scale.
                        last.set(event.x, event.y)
                        start.set(last)

                        mode = DRAG
                    } else {

                        last.set(event.x, event.y)
                        start.set(last)
                        mode = DRAG

                        // we still need the fullscalestart saved so that we can use this
                        // in ACTION_UP in order to pass on to check selected rect.

                        inverseMatrix = Matrix(matrix)
                        inverseMatrix?.let { it.invert(it) }

                        // then get the absolute x,y values
                        event.transform(inverseMatrix)

                        sendpoint.set(event.x.toInt(), event.y.toInt())

                    }
                }

                MotionEvent.ACTION_MOVE -> run {
                    if (mode == DRAG)  {

                        if (drawbtnpressed) {
                            // we are not drawing temp rects here. so do nothing.

                        } else {
                            val deltaX = curr.x - last.x
                            val deltaY = curr.y - last.y

                            // if deltaX and deltaY are not enough then we don't move
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

                        event.transform(inverseMatrix)
                        last.set(event.x, event.y)

                        // get the final rect
                        // but don't draw it yet. just keep it.
                        // the drawing will be done when the user clicks yes.
                        savedrect = Util.convertToRect(start, last)
                        Log.d(TAG, "onTouch: savedrect updated")

                    } else {

                        val xDiff = Math.abs(curr.x - start.x).toInt()
                        val yDiff = Math.abs(curr.y - start.y).toInt()
                        if (xDiff < MOVE_CLICK_LIMIT && yDiff < MOVE_CLICK_LIMIT) {

                            Log.d(TAG, "onTouch: inside click case")
                            Log.d(TAG, "onTouch: sendpoint x=" + sendpoint.x + ", y=" + sendpoint.y)
                            val isRectSelectExist = checkRectSelectCallback(sendpoint)
                            if (isRectSelectExist) {
                                Log.d(TAG, "onTouch: rect select exist")
                            } else {
                                Log.d(TAG, "onTouch: rect select not exist")

                                // if no rect exist, then unselect any selected rectangles and redraw
                                unselectAnyIfExistCallback()

                            }
                            performClick()
                        }


                    }
                }

                MotionEvent.ACTION_POINTER_UP -> mode = NONE
                else -> Log.d(TAG, "touch event falling to default")
            }

            imageMatrix = matrix
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

            // if saveScale is minScale, then we enable viewpager swipe
            // if not, (which would be when the slightset zoom in has occured,
            // then we disable the swipe of viewpager.


            if (saveScale == minScale) {
                Log.i(TAG, "enable swipe")
                dataImageViewPager.isSwipeEnabled = true
            } else {
                Log.i(TAG, "disable swipe")
                dataImageViewPager.isSwipeEnabled = false
            }


            if (origWidth * saveScale <= viewWidth || origHeight * saveScale <= viewHeight) {
                matrix.postScale(mScaleFactor, mScaleFactor, (viewWidth / 2).toFloat(),
                        (viewHeight / 2).toFloat())
            } else {
                matrix.postScale(mScaleFactor, mScaleFactor,
                        detector.focusX, detector.focusY)
            }

            fixTrans()
            return true
        }
    }

    internal fun fixTrans() {
        matrix.getValues(m)
        val transX = m[Matrix.MTRANS_X]
        val transY = m[Matrix.MTRANS_Y]

        val fixTransX = getFixTrans(transX, viewWidth.toFloat(), origWidth * saveScale)
        val fixTransY = getFixTrans(transY, viewHeight.toFloat(), origHeight * saveScale)

        if (fixTransX != 0f || fixTransY != 0f) {
            matrix.postTranslate(fixTransX, fixTransY)
        }
    }

    internal fun getFixTrans(trans: Float, viewSize: Float, contentSize: Float): Float {
        val minTrans: Float
        val maxTrans: Float

        if (contentSize <= viewSize) {
            minTrans = 0f
            maxTrans = viewSize - contentSize
        } else {
            minTrans = viewSize - contentSize
            maxTrans = 0f
        }

        if (trans < minTrans) {
            return -trans + minTrans
        }
        return if (trans > maxTrans) {
            -trans + maxTrans
        } else 0f
    }

    internal fun getFixDragTrans(delta: Float, viewSize: Float, contentSize: Float): Float {
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
                || viewWidth == 0 || viewHeight == 0) {
            return
        }
        oldMeasuredHeight = viewHeight
        oldMeasuredWidth = viewWidth

        if (saveScale == 1f) {
            // Fit to screen.
            val scale: Float

            val drawable = drawable
            if (drawable == null || drawable.intrinsicWidth == 0
                    || drawable.intrinsicHeight == 0) {
                return
            }
            val bmWidth = drawable.intrinsicWidth
            val bmHeight = drawable.intrinsicHeight

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
            imageMatrix = matrix
        }
        fixTrans()
    }


    fun passCanvas(canvas: Canvas) {
        this.canvas = canvas
    }


    fun setTouchEnable(value: Boolean) {
        this.touchEnable = value
    }


    fun drawRect() {
        canvas.drawRect(savedrect, paint)

        savedrect?.let { addRectCallback(it) }
//        addRectCallback.accept(savedrect)
        saveLabelCallback()

        Log.d(TAG, "call savedlabelfile from mainIV")

    }

    fun drawSelectedRect(rect: Rect) {
        canvas.drawRect(rect, selectedpaint)
        Log.d(TAG, "drawSelectedRect: finished")
    }


    fun drawUnselectedRect(rect: Rect) {
        canvas.drawRect(rect, paint)
        Log.d(TAG, "drawUnselectedRect: finished")
    }

    fun drawDeleteRect(rect: Rect) {
        canvas.drawRect(rect, transparentpaint)
        Log.d(TAG, "drawDeleteRect: finished")
    }




}