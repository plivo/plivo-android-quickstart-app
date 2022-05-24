package com.plivo.plivosimplequickstart.jwtpojos;

import com.google.gson.annotations.SerializedName;


public class Per {

   @SerializedName("voice")
   Voice voice;

    public void setVoice(Voice voice) {
        this.voice = voice;
    }
    public Voice getVoice() {
        return voice;
    }
    
}