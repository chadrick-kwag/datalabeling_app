package com.example.chadrick.datalabeling.CustomComponents

import android.content.Context
import android.graphics.*
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import android.view.MotionEvent
import com.example.chadrick.datalabeling.Util

/**
 * Created by chadrick on 18. 2. 12.
 */

class RectDrawImageView2 @JvmOverloads constructor(context: Context,
                                                   attrs: AttributeSet? = null,
                                                   defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {
    val paint: Paint = Paint()
    val start = PointF()
    val last = PointF()



    var TouchEnabled = true
    private var isMove = false
    private var isDown = false


    lateinit var drawBtnpressedcallback: () -> Boolean
    lateinit var bitmap: Bitmap
    lateinit var canvas: Canvas
    lateinit var rectReadyCallback: (Rect) -> Unit


    init {

        paint.color = Color.rgb(255, 0, 0)
        paint.strokeWidth = 5f
        paint.style = Paint.Style.STROKE





        setOnTouchListener local@ { view, motionEvent ->
            // if touch is disabled, do nothing.
            if (!TouchEnabled) {
                return@local true
            }

            // what we need is get the x,y of event.
            // set start, last and draw on the canvas
            // but there is difference in the size of canvas and the IV's size
            // since we are inside the IV itself, I think we can access the size of IV much better

            // check if the drawBtn is pressed or not.
            val isDrawBtnPressed: Boolean = drawBtnpressedcallback()
//
//
//            if (drawBtnpressedcallback != null) {
//                drawbtnpressed = drawBtnpressesdcallback()
//            } else {
//                drawbtnpressed = false
//            }

            // deal with the event only when drawbtn is pressed.

            if (!isDrawBtnPressed && !isMove) {
                return@local false
            }

            // prepare the adjusted x,y coordinates
            val curr = PointF()
            curr.set(motionEvent.x, motionEvent.y)

            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {

                    isDown = true

                    start.set(motionEvent.x, motionEvent.y)
                    last.set(start)
                }
                MotionEvent.ACTION_MOVE -> run {
                    if (!isMove) {
                        //              Log.d(TAG,"maskIV, motion move event started");
                    }

                    isMove = true

                    //            Log.d(TAG,"action move detected");

                    if (!isDrawBtnPressed) {
                        // probably fall in here when the user released the drawbtn
                        // while dragging the rectangle.
                        // we just want to quit the drawing of the rectangle
                        bitmap.eraseColor(Color.TRANSPARENT)

                        return@run
                    }

                    //remove the previous drawn rectangle
                    //            Log.d(TAG,"maskIV erase in action_move");
                    bitmap.eraseColor(Color.TRANSPARENT)

                    last.set(curr.x, curr.y)

                    // draw rectangle based on start, last
                    val rect = Util.convertToRect(start, last)

                    canvas.drawRect(rect, paint)
                }
                MotionEvent.ACTION_UP -> {

                    isMove = false
                    isDown = false

                    // erase the rect draw in move case.
                    //            bitmap.eraseColor(Color.TRANSPARENT);
                    last.set(curr.x, curr.y)
                    // draw the rectangle. and then ask the user if this rectangle is going to be saved or not.
                    val rect2 = Util.convertToRect(start, last)

                    rectReadyCallback(rect2)

                    canvas.drawRect(rect2, paint)
                }
            }

            invalidate()

            // indicate that touch event is still no handled. we need this event to go down
            // to the lower level.

            return@local true
        }
    }


    fun passWH(width: Int, height: Int) {
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        canvas = Canvas(bitmap)
        this.setImageBitmap(bitmap)
    }

    fun eraseall() {
        bitmap.eraseColor(Color.TRANSPARENT)
    }


}