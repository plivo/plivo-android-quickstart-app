package com.plivo.plivosimplequickstart.jwtpojos;

import com.google.gson.annotations.SerializedName;

   
public class Voice {

   @SerializedName("incoming_allow")
   boolean incomingAllow;

   @SerializedName("outgoing_allow")
   boolean outgoingAllow;


    public void setIncomingAllow(boolean incomingAllow) {
        this.incomingAllow = incomingAllow;
    }
    public boolean getIncomingAllow() {
        return incomingAllow;
    }
    
    public void setOutgoingAllow(boolean outgoingAllow) {
        this.outgoingAllow = outgoingAllow;
    }
    public boolean getOutgoingAllow() {
        return outgoingAllow;
    }
    
}