package com.example.chadrick.datalabeling.Models

import java.util.*

/**
 * Created by chadrick on 17. 12. 3.
 */
class BGColorRandomPicker {

    companion object {
        private val colors: List<String> = listOf("#ff00ff","#00ff00","#0ff0f0","#f00fff")
    }

    fun getRandomColor() : String {
        val random = Random()
        return colors.get( random.nextInt(colors.size))
    }
}