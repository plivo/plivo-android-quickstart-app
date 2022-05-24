package com.plivo.plivosimplequickstart;

import android.util.Base64;
import android.util.Log;


import com.google.gson.Gson;
import com.plivo.plivosimplequickstart.jwtpojos.JWTPojo;

import java.io.UnsupportedEncodingException;

public class JWTUtils {

    public static void decoded(String JWTEncoded) throws Exception {
        try {
            Log.d("TAG", "decoded: "+JWTEncoded);
            String[] split = JWTEncoded.split("\\.");
            Log.d("TAG", "decoded: ");
            Log.d("TAG", "decoded: "+split.length);
            String jwtJson="";
            if(split.length>2) {
                jwtJson = "{\"header\":" + getJson(split[0]) + ",\"body\":" + getJson(split[1]) + "}";
            }
            /*
            * Organisation organisation
            = gson.fromJson(OrganisationJson,
                            Organisation.class);
            * */

            Log.d("TAG", "decoded: -"+jwtJson);

            JWTPojo jwtPojo = new Gson().fromJson(jwtJson,JWTPojo.class);
            Log.d("TAG", "decoded: jwtPojo "+ jwtPojo.getBody().getPer().getVoice().getIncomingAllow());
            Log.d("JWT_DECODED", "Header: " + getJson(split[0]));
            Log.d("JWT_DECODED", "Body: " + getJson(split[1]));
        } catch (UnsupportedEncodingException e) {
            //Error
            Log.d("TAG", "decoded: "+e.toString());
        }
    }

    private static String getJson(String strEncoded) throws UnsupportedEncodingException{
        byte[] decodedBytes = Base64.decode(strEncoded, Base64.URL_SAFE);
        return new String(decodedBytes, "UTF-8");
    }

}
