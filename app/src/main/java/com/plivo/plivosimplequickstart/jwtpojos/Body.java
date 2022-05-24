package com.plivo.plivosimplequickstart.jwtpojos;

import com.google.gson.annotations.SerializedName;



public class Body {

   @SerializedName("app")
   String app;

   @SerializedName("exp")
   int exp;

   @SerializedName("iss")
   String iss;

   @SerializedName("nbf")
   int nbf;

   @SerializedName("per")
   Per per;

   @SerializedName("sub")
   String sub;


    public void setApp(String app) {
        this.app = app;
    }
    public String getApp() {
        return app;
    }
    
    public void setExp(int exp) {
        this.exp = exp;
    }
    public int getExp() {
        return exp;
    }
    
    public void setIss(String iss) {
        this.iss = iss;
    }
    public String getIss() {
        return iss;
    }
    
    public void setNbf(int nbf) {
        this.nbf = nbf;
    }
    public int getNbf() {
        return nbf;
    }
    
    public void setPer(Per per) {
        this.per = per;
    }
    public Per getPer() {
        return per;
    }
    
    public void setSub(String sub) {
        this.sub = sub;
    }
    public String getSub() {
        return sub;
    }
    
}