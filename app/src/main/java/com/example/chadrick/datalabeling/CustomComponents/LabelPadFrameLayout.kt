package com.example.chadrick.datalabeling.CustomComponents

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout

/**
 * Created by chadrick on 18. 2. 12.
 */


class LabelPadFrameLayout @JvmOverloads constructor(context:Context,
                                                    attrs: AttributeSet? = null,
                                                    defStyleAttr : Int =0
                                                    ): FrameLayout(context,attrs,defStyleAttr){

     lateinit var baseIV : DataImageImageView
     lateinit var maskIV : RectDrawImageView
     lateinit var rectIV : RenderedRectsImageView


    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
//        return super.dispatchTouchEvent(ev)

        // copy ev to e2,e3. If we don't do this, then the ev coordinates will not be equally
        // dispatched to each IVs.
        val e2 = MotionEvent.obtain(ev)
        val e3 = MotionEvent.obtain(ev)

        baseIV.dispatchTouchEvent(ev)
        rectIV.dispatchTouchEvent(e2)
        maskIV.dispatchTouchEvent(e3)

        // this order is identical to which IV's touch listener is triggered
        // the rectIV should be called before maskIV

        return true
    }


}