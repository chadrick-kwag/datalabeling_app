package com.example.chadrick.datalabeling.Fragments

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.chadrick.datalabeling.MainActivity
import com.example.chadrick.datalabeling.Models.DataSet
import com.example.chadrick.datalabeling.Models.DownloadTaskManager2
import com.example.chadrick.datalabeling.Models.RecentActivityLogManager
import com.example.chadrick.datalabeling.R
import com.example.chadrick.datalabeling.Tasks.dszipDownloadTask
import com.example.chadrick.datalabeling.Util
import kotlinx.android.synthetic.main.datasetprogressfragment2_layout.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by chadrick on 17. 12. 3.
 */

class DatasetProgressFragment2 : Fragment() {

    //    private  ds : DataSet =  DataSet.deserialize(arguments.get("ds") as String)
    private val ds: DataSet by lazy { DataSet.deserialize(arguments.get("ds") as String) }
    private var bgcolor: Int = 0

    private val downloadTaskManger = DownloadTaskManager2.instance
    lateinit var ralogmanager: RecentActivityLogManager

    companion object {
        val TAG: String = this.javaClass.simpleName
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bgcolor = Color.parseColor(arguments.getString("bgcolor"))
        Log.d("chadrick", "bgcolor fetched at oncreate=" + bgcolor)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        ralogmanager = RecentActivityLogManager.getInstance(context)

        return inflater?.inflate(R.layout.datasetprogressfragment2_layout, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        title_tv.text = ds.name
        Log.d("chadrick", "bgcolor=" + bgcolor)
        thumbnail_holder.setBackgroundColor(bgcolor)

        // attach click listener
        download_constraintlayout.setOnClickListener({ view ->
            // execute download task.
            synchronized(downloadTaskManger) {
                if (!downloadTaskManger.isAlreadyRegistered(ds)) {

                    ralogmanager.updateRAitem(ds, System.currentTimeMillis())

                    changetoDownloadingUI()

                    val downloadtask = dszipDownloadTask(param_dataset = ds,
                            param_errorCallback = { downloadErrorCallback() },
                            param_successCallback = { downloadSuccessCallback() },
                            param_unzipcompletecallback = { unzipCompleteCallback() },
                            param_progressUIupdate = this@DatasetProgressFragment2::updateDownloadprogresscircle)
                    downloadTaskManger.addDownloadTask(ds, downloadtask)
                    downloadtask.execute()
                }
            }

        })

        continuebtn_background_iv.setOnClickListener({ view ->

            ralogmanager.updateRAitem(ds, System.currentTimeMillis())

            val imageViewerFragment = ImageViewerFragment()
            imageViewerFragment.passUpdateStatCallback({ updateStats() })
            val b = Bundle()
            b.putString("ds", ds.serialize())

            imageViewerFragment.arguments = b
            val fragmentManager = fragmentManager
            fragmentManager.beginTransaction().add(R.id.fragmentcontainer, imageViewerFragment)
                    .addToBackStack(null).commit()
        })

        uploadbtn_bg_iv.setOnClickListener(listener@ { view ->
            Log.d(TAG, "upload clicked")

            ralogmanager.updateRAitem(ds, System.currentTimeMillis())
            // first check if progress is complete

            // gathering .json files
            val dir = File(ds.getDirstr())
            val jsonfiles = Util.getJsonFileList(dir)

            // check if number matches with imagefiles
            if (jsonfiles!!.size != Util.getImageFileList(dir)!!.size) {
                Log.d(TAG, "jsonfiles number doesn't match with image files number. abort")
                Toast.makeText(context, "progress not complete", Toast.LENGTH_SHORT).show()
                disableUploadBtn()
                return@listener
            }

            Log.d(TAG, "jsonfiles number match")


            // change the icon to progress circle
            uploadprogressbar.visibility = View.VISIBLE
            uploadbtn_icon_iv.visibility = View.INVISIBLE

            // gather the imagefiles
            // there are several ways to do this. but for simplicity, let's just gather the .json files
            // and check the number of files and see if they match with the number of image files


            // if any zip file exists(probably with older date in name), delete it.


            // create output zip file
            val date = SimpleDateFormat("yyMMdd_HHmmss").format(Calendar.getInstance().time)
            val outputzipfile_path = dir.getPath() + File.separator + ds.getName() + "-" + date + ".zip"
            val outputzipfile = File(outputzipfile_path)


            // zip the json files into the output zip file

            if (Util.createZipFilefromFiles(jsonfiles, outputzipfile)) {
                Log.d(TAG, "zipping success")
                Log.d(TAG, "output zip file name: " + outputzipfile.getPath())
                Log.d(TAG, "output zip file size: " + outputzipfile.length())
            } else {
                Log.d(TAG, "zipping failed")
            }

            // send to server
            // first create multipart request
            val request = Util.createRequestFileUpload(outputzipfile,
                    "http://13.124.175.119:4001/upload/labelzip"
            ) {
                restoreUploadBtn()
                Toast.makeText(context, "Upload Success", Toast.LENGTH_SHORT).show()
            }

            val queue = (activity as MainActivity).queue

            if (queue != null) {
                queue.add<NetworkResponse>(request)
            } else {
                Log.d(TAG, "queue is null")
                restoreUploadBtn()
                Toast.makeText(context, "request queue error", Toast.LENGTH_SHORT).show()
                return@listener
            }
        })

        fetchdescription()
        if (checkAlreadyDownloaded()) {
            // if already downloaded, goto statcheck
            showUploadAndContButton()
            updateStats()

        } else {
            // if not downloaded, then show download button.
            showDownloadButton()
        }

        delete_btn.setOnClickListener({ view ->

            val alertbuilder = AlertDialog.Builder(activity)
            alertbuilder.setMessage("Delete this dataset?")
                    .setPositiveButton("Delete", { _, _ ->
                        val workdir = File(ds.dirstr)
                        Log.d(TAG, "fuck deleting filepath=" + workdir.toString())
                        val deleteresult = workdir.deleteRecursively()
                        Log.d(TAG, "fuck delete result = " + deleteresult)

                        showDownloadButton()
                        updateStats()
                    })
                    .setNegativeButton("No", { _, _ -> })
            val alert = alertbuilder.create()
            alert.show()


        })

        goback_btn.setOnClickListener({ view ->
            Log.d(TAG, "goback clicked")
            fragmentManager.popBackStack()
        })

        downloadprogresscircle.progress = 0f
        updateStats()
    }


    override fun onPause() {
        super.onPause()
        // remove any downloading task. if it was in download, delete the
        // dsdir too it if was in the middle of creating it.
        downloadTaskManger.remove(ds)
        val dsdir = File(ds.dirstr)
        if (dsdir.exists()) {
            dsdir.deleteRecursively()
        }

    }

    private fun fetchdescription() {
        // create post request
        val jsonreqobj = JSONObject()
        jsonreqobj.put("id", ds.id)
        val jsonarray = JSONArray("['description']")
        jsonreqobj.put("reqfield", jsonarray)
        val jsonreq = JsonObjectRequest(Request.Method.POST, UserMainFragment.baseurl + "/dsinfo",
                jsonreqobj,
                { response: JSONObject? ->

                    val isSuccess = response?.getBoolean("success") ?: false

                    if (isSuccess) {
                        // is success if true
                        val desc = response?.getString("description")
                        description_tv.text = desc
                    } else {
                        Log.d("chadrick", "received unsuccessful response from server")
                        description_tv.text = "unsuccessful response"
                    }

                },
                { error: VolleyError? ->
                    Log.d("chadrick", "error occured while fetching description. err=" + error.toString())
                    description_tv.text = "failed to get description"
                }
        )
        val queue = Volley.newRequestQueue(context)
        queue.add(jsonreq)
    }

    private fun checkAlreadyDownloaded(): Boolean {
        // if the dir with ds id exists, then we assume that the directory is all set.

        // TODO: make the checking of downloaded_&installed more detailed
        val dsworkdir = File(context.filesDir, ds.id.toString())
        Log.d(TAG, "dsworkdir in checkalreadydownloaded=" + dsworkdir)

        Log.d(TAG, "ds dirstr in dataset=" + ds.dirstr)
        Log.d(TAG, "ds exist in dataset=" + ds.direxist)
        return dsworkdir.exists()
    }

    private fun showDownloadButton() {
        download_constraintlayout.visibility = View.VISIBLE

        uploadbtn_bg_iv.visibility = View.INVISIBLE
        uploadbtn_icon_iv.visibility = View.INVISIBLE
        continuebtn_background_iv.visibility = View.INVISIBLE
        continuebtn_icon_iv.visibility = View.INVISIBLE

        delete_btn.visibility = View.INVISIBLE
    }

    private fun showUploadAndContButton() {
        download_constraintlayout.visibility = View.INVISIBLE

        uploadbtn_bg_iv.visibility = View.VISIBLE
        uploadbtn_icon_iv.visibility = View.VISIBLE
        continuebtn_background_iv.visibility = View.VISIBLE
        continuebtn_icon_iv.visibility = View.VISIBLE

        delete_btn.visibility = View.VISIBLE

    }

    private fun downloadErrorCallback() {
        Toast.makeText(context, "download failed", Toast.LENGTH_SHORT).show()
    }

    private fun downloadSuccessCallback() {
        // moving on to unzipping
        Log.d(TAG, "now unzipping")
    }

    private fun unzipCompleteCallback() {
        Toast.makeText(context, "download success", Toast.LENGTH_SHORT).show()
        changetoDownloadReadyUI()
        showUploadAndContButton()
        updateStats()
    }

    private fun updateStats() {
        val workdir = File(ds.dirstr)

        // if it doesn't exist, it means that dataset is not downloaded in this case, set
        // both the complete and total to '-'

        if (!workdir.exists()) {
            total_stat_tv.text = "-"
            complete_stat_tv.text = "-"
            return
        }

        val imagefiles = Util.getImageFileList(workdir)
        val finishedImageCount: Int = getFinishedCount(imagefiles)
        var percentage: Int = 0

        if (imagefiles.size > 0) {
            percentage = finishedImageCount * 100 / imagefiles.size
        }

        // update ui components
        total_stat_tv.text = imagefiles.size.toString()
        complete_stat_tv.text = percentage.toString() + "%"
    }

    private fun getFinishedCount(imagefiles: ArrayList<File>): Int {
        var count: Int = 0
        for (image in imagefiles) {
            val labelfile = Util.getLabelFilefromImageFile(image)
            if (labelfile.exists()) count++
        }
        return count
    }

    private fun disableUploadBtn() {
        uploadbtn_bg_iv.setBackgroundResource(R.drawable.upload_btn_disable_selector)
    }

    private fun enableUploadBtn() {
        uploadbtn_bg_iv.setBackgroundResource(R.drawable.upload_area_selector)
    }

    private fun restoreUploadBtn() {
        uploadprogressbar.visibility = View.INVISIBLE
        uploadbtn_icon_iv.visibility = View.VISIBLE
    }

    private fun changetoDownloadingUI() {
        downloadprogresscircle.visibility = View.VISIBLE
        download_icon_iv.visibility = View.INVISIBLE
        download_tv.visibility = View.INVISIBLE
    }

    private fun changetoDownloadReadyUI() {
        downloadprogresscircle.visibility = View.INVISIBLE
        download_icon_iv.visibility = View.VISIBLE
        download_tv.visibility = View.VISIBLE

        downloadprogresscircle.progress = 0f
    }

    private fun updateDownloadprogresscircle(progress: Int) {
        downloadprogresscircle?.let {
            it.progress = progress.toFloat()
            Log.d(TAG, "updating progress=" + progress)
        }

    }
}