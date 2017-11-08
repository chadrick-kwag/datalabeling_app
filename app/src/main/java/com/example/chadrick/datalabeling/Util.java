package com.example.chadrick.datalabeling;

import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by chadrick on 17. 10. 13.
 */

public class Util {


  public static ArrayList<File> getImageFileList(File dir) {

    //sanity check
    if (!dir.exists()) {
      Log.d("util", "ds dir doesn't exist");
      return null;
    }

    File[] files = dir.listFiles();
    ArrayList<File> imagefilelist = new ArrayList<>();
    for (File itemfile : files) {
      String pathstr = itemfile.toString();
      String extension = pathstr.substring(pathstr.lastIndexOf(".") + 1, pathstr.length());
      if (extension.equals("png") || extension.equals("jpg")) {
        Log.d("util", files.toString() + " -> png or jpg file added to list");
        imagefilelist.add(itemfile);
      }
    }

    return imagefilelist;
  }


  public static ArrayList<File> getJsonFileList(File dir){
    if (!dir.exists()) {
      Log.d("util", "ds dir doesn't exist");
      return null;
    }

    File[] files = dir.listFiles();
    ArrayList<File> imagefilelist = new ArrayList<>();
    for (File itemfile : files) {
      String pathstr = itemfile.toString();
      String extension = pathstr.substring(pathstr.lastIndexOf(".") + 1, pathstr.length());
      if (extension.equals("json") ) {
//        Log.d("util", files.toString() + " -> png or jpg file added to list");
        imagefilelist.add(itemfile);
      }
    }

    return imagefilelist;
  }


  public static Rect convertToRect(PointF start, PointF last) {
    int x1, x2, y1, y2;
    if (start.x > last.x) {
      x1 = (int) last.x;
      x2 = (int) start.x;
    } else {
      x2 = (int) last.x;
      x1 = (int) start.x;
    }

    if (start.y > last.y) {
      y1 = (int) last.y;
      y2 = (int) start.y;
    } else {
      y2 = (int) last.y;
      y1 = (int) start.y;
    }

    Rect rect = new Rect(x1, y1, x2, y2);
    return rect;
  }

  public static String getOnlyFilename(String filenamewithext) {
    int pos = filenamewithext.lastIndexOf(".");
    if (pos > 0) {
      return filenamewithext.substring(0, pos);
    } else {
      return null;
    }
  }


  public static File getLabelFilefromImageFile(File imagefile) {
    String imagefilename = Util.getOnlyFilename(imagefile.getName());
    String parentpath = imagefile.getParent();

    return new File(parentpath + File.separator + imagefilename + ".json");

  }

  /**
   * return true if succeed. return false if error occured.
   */
  public static boolean createZipFilefromFiles(ArrayList<File> filelist, File outputfile) {
    try {
      byte[] buffer = new byte[1024];
      FileOutputStream fileOutputStream = new FileOutputStream(outputfile);
      ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);
      for (int i = 0; i < filelist.size(); i++) {
        File targetfile = filelist.get(i);
        FileInputStream fileInputStream = new FileInputStream(targetfile);
        zipOutputStream.putNextEntry(new ZipEntry(targetfile.getName()));

        int length;
        while ((length = fileInputStream.read(buffer)) > 0) {
          zipOutputStream.write(buffer);
        }

        zipOutputStream.closeEntry();
        fileInputStream.close();

      }

      zipOutputStream.close();

    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return false;
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }

    return true;

  }


  public static VolleyMultipartRequest createRequestFileUpload(File uploadfile, String url, Runnable finishedcallback){

    VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, url,
        new Response.Listener<NetworkResponse>() {
      @Override
      public void onResponse(NetworkResponse response) {

        if(response.statusCode==200){
          Log.d("whatup","statcode=200");
        }


        try{
          String result = new String(response.data,"UTF-8");
          JSONObject resultobj = new JSONObject(result);

          int resultint = resultobj.getInt("result");
          if(resultint==1){
            Log.d("whatup","successfully uploaded");

          }
          else{
            Log.d("whatup","statuscode 200 but upload error");
          }

        }
        catch(UnsupportedEncodingException e){
          e.printStackTrace();

        }
        catch(JSONException e){
          e.printStackTrace();
        }


//        String resultResponse = new String(response.data);
//        try {
//          JSONObject result = new JSONObject(resultResponse);
//          String status = result.getString("status");
//          String message = result.getString("message");
//
//          if (status.equals("200")) {
//            // tell everybody you have succed upload image and post strings
//            Log.i("Messsage:", message);
//          } else {
//            Log.i("Unexpected", message);
//          }
//        } catch (JSONException e) {
//          e.printStackTrace();
//        }

        // when done, restore UI
        finishedcallback.run();

      }
    }, new Response.ErrorListener() {
      @Override
      public void onErrorResponse(VolleyError error) {
        NetworkResponse networkResponse = error.networkResponse;
        String errorMessage = "Unknown error";
        if (networkResponse == null) {
          if (error.getClass().equals(TimeoutError.class)) {
            errorMessage = "Request timeout";
          } else if (error.getClass().equals(NoConnectionError.class)) {
            errorMessage = "Failed to connect server";
          }
        } else {
          String result = new String(networkResponse.data);
          try {
            JSONObject response = new JSONObject(result);
            String status = response.getString("status");
            String message = response.getString("message");

            Log.e("Error Status", status);
            Log.e("Error Message", message);

            if (networkResponse.statusCode == 404) {
              errorMessage = "Resource not found";
            } else if (networkResponse.statusCode == 401) {
              errorMessage = message+" Please login again";
            } else if (networkResponse.statusCode == 400) {
              errorMessage = message+ " Check your inputs";
            } else if (networkResponse.statusCode == 500) {
              errorMessage = message+" Something is getting wrong";
            }
          } catch (JSONException e) {
            e.printStackTrace();
          }
        }
        Log.i("Error", errorMessage);
        error.printStackTrace();

        // when done, restore UI
        finishedcallback.run();

      }
    }) {
//      @Override
//      protected Map<String, String> getParams() {
//        Map<String, String> params = new HashMap<>();
//        params.put("api_token", "gh659gjhvdyudo973823tt9gvjf7i6ric75r76");
//        params.put("name", mNameInput.getText().toString());
//        params.put("location", mLocationInput.getText().toString());
//        params.put("about", mAvatarInput.getText().toString());
//        params.put("contact", mContactInput.getText().toString());
//        return params;
//      }

      @Override
      protected Map<String, DataPart> getByteData() {
        Map<String, DataPart> params = new HashMap<>();
        // file name could found file base or direct access from real path
        // for now just get bitmap data from ImageView
        params.put("labelzip", new DataPart(uploadfile.getName(), fileToBytearray(uploadfile), "application/zip"));

        return params;
      }
    };

    return multipartRequest;
  }


  public static byte[] fileToBytearray(File file){
    byte[] bytes = null;
    try
    {
      FileInputStream inputStream = new FileInputStream(file);
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      byte[] b = new byte[1024];
      int bytesRead =0;

      while ((bytesRead = inputStream.read(b)) != -1)
      {
        bos.write(b, 0, bytesRead);
      }

      bytes = bos.toByteArray();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    return bytes;
  }


}
