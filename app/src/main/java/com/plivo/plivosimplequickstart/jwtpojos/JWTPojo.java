package com.plivo.plivosimplequickstart.jwtpojos;

import com.google.gson.annotations.SerializedName;


public class JWTPojo {

   @SerializedName("header")
   Header header;

   @SerializedName("body")
   Body body;


    public void setHeader(Header header) {
        this.header = header;
    }
    public Header getHeader() {
        return header;
    }
    
    public void setBody(Body body) {
        this.body = body;
    }
    public Body getBody() {
        return body;
    }
    
}