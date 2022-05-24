package com.plivo.plivosimplequickstart.jwtpojos;

import com.google.gson.annotations.SerializedName;

   
public class Header {

   @SerializedName("alg")
   String alg;

   @SerializedName("cty")
   String cty;

   @SerializedName("typ")
   String typ;


    public void setAlg(String alg) {
        this.alg = alg;
    }
    public String getAlg() {
        return alg;
    }
    
    public void setCty(String cty) {
        this.cty = cty;
    }
    public String getCty() {
        return cty;
    }
    
    public void setTyp(String typ) {
        this.typ = typ;
    }
    public String getTyp() {
        return typ;
    }
    
}