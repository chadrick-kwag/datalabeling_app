package com.example.chadrick.datalabeling.Models

import android.graphics.Color

/**
 * Created by chadrick on 17. 12. 4.
 */

class SMitem(type: Int, title: String, clickable: Boolean = false, titleColor: Int = 0, clickAction: () -> Unit = {}) {
    val type = type
    val title = title
    val clickable = clickable
    val titleColor = titleColor
    val clickAction = clickAction

    companion object {
        val TYPE_PLAIN: Int = 0
        val TYPE_TOGGLE: Int = 1
    }
}