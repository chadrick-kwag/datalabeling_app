package com.example.chadrick.datalabeling;

/**
 * Created by chadrick on 17. 10. 8.
 */

public class DataSet {
  private int id;
  private String name;
  private Boolean direxist;

  public DataSet(){

  }

  public DataSet(int id, String name, Boolean direxist){
    this.id = id;
    this.name = name;
    this.direxist = direxist;
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
}
