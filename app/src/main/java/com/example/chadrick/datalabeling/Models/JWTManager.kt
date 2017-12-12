package com.example.chadrick.datalabeling.Models

import android.content.Context
import android.util.Log
import com.example.chadrick.datalabeling.Util
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileReader

/**
 * Created by chadrick on 17. 12. 11.
 */

class JWTManager {


    var file: File


    var jwt: String = ""
    var userid:String = ""
    var usermail:String=""


    companion object {



        @Volatile private var INSTANCE: JWTManager? = null

        fun getInstance(context: Context?): JWTManager? =
                context?.let { INSTANCE ?: synchronized(this) {
                    INSTANCE ?: JWTManager(context).also { INSTANCE = it }

                } }
        fun getInstance():JWTManager? = INSTANCE

    }


    fun savejwt(newjwt: String) {
        jwt = newjwt
        val jsonobj = Util.decoded(jwt)
        userid =  jsonobj?.getString("userid") ?: ""
        usermail = jsonobj?.getString("user_mail") ?: ""

        file.delete()
        file.writeBytes(newjwt.toByteArray())

    }

    fun deletejwt() {
        file.delete()
    }

    private constructor(context: Context) {


        this.file = File(context.filesDir, "/user.jwt")
        if (file.exists()) {
            val reader = FileReader(file)
            val br = BufferedReader(reader)
            val readstr = StringBuilder()
            var line: String?
            while (true) {
                line = br.readLine()
                line?.let { readstr.append(line) }
                if (line == null) {
                    break
                }

            }
            br.close()
            val readjwt = readstr.toString()

            Log.d("JWTManager", "read jwt=" + readjwt)
            jwt = readjwt

            // parse and extract mail and id info
            val jsonobj = Util.decoded(jwt)
            userid =  jsonobj?.getString("userid") ?: ""
            usermail = jsonobj?.getString("user_mail") ?: ""

        }
    }


}