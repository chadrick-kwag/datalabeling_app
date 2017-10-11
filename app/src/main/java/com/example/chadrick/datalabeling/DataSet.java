package com.example.chadrick.datalabeling;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by chadrick on 17. 10. 8.
 */

public class DataSet {
  private int id;
  private String name;
  private Boolean direxist;
  private String dirstr;
  private String zipfilestr;

  public DataSet(){

  }

  public DataSet(JSONObject jsonObject){
    Log.d("dataset","dataset init with jsonobject, input: "+jsonObject.toString());
    try{
      this.id = jsonObject.getInt("id");
      this.name = jsonObject.getString("name");
      this.direxist = jsonObject.getBoolean("direxist");
      this.dirstr= jsonObject.getString("dirstr");
      this.zipfilestr = jsonObject.getString("zipfilestr");
    }
    catch(JSONException e){
      Log.d("dataset","dataset init from jsonobject failed");
      e.printStackTrace();

    }

    //print init values
    Log.d("dataset","id: "+Integer.toString(this.id));
    Log.d("dataset","name: "+this.name);
    Log.d("dataset","direxist: "+Boolean.toString(this.direxist));
    Log.d("dataset","dirstr: "+this.dirstr);
    Log.d("dataset","zipfilestr: "+this.zipfilestr);

  }

  public DataSet(int id, String name, Boolean direxist, String basefiledir){
    this.id = id;
    this.name = name;
    this.direxist = direxist;
    this.dirstr= basefiledir+"/"+ Integer.toString(id);
    this.zipfilestr = this.dirstr + "/" + Integer.toString(id) + ".zip";

  }

  public int getId(){
    return this.id;
  }

  public String getName(){
    return this.name;
  }

  public Boolean getDirexist(){
    return this.direxist;
  }

  public void setId(int id){
    this.id = id;
  }

  public void setName(String name){
    this.name = name;
  }

  public String getZipfilestr(){
    return this.zipfilestr;
  }

  public String getDirstr(){
    return this.dirstr;
  }

  public String serialize(){
    JSONObject jsonobj = new JSONObject();
    try{
      jsonobj.put("id",this.id);
      jsonobj.put("name", this.name);
      jsonobj.put("direxist",this.direxist);
      jsonobj.put("dirstr",this.dirstr);
      jsonobj.put("zipfilestr",this.zipfilestr);
      Log.d("dataset","jsonobj check print: "+jsonobj.toString());


    }
    catch(JSONException e){
      e.printStackTrace();
      return null;
    }

    return jsonobj.toString();

  }

  public static DataSet deserialize(String input) throws JSONException{

    JSONObject jsonobj;

    jsonobj = new JSONObject(input);


    DataSet retDS = new DataSet(jsonobj);
    return retDS;
  }
}
