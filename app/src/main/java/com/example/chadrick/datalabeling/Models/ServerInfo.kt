package com.example.chadrick.datalabeling.Models

import android.content.Context
import java.io.*

/**
 * Created by chadrick on 17. 12. 4.
 */

class ServerInfo private constructor() {


    private object holder {val INSTANCE = ServerInfo()}
    lateinit var serveraddress: String

    companion object {
        val instance = holder.INSTANCE

    }

    fun config(configfilestream: InputStream){
        val reader = BufferedReader(InputStreamReader(configfilestream))
        // assumes the first line is the serveraddress
        serveraddress = reader.readLine().trim()
    }

}