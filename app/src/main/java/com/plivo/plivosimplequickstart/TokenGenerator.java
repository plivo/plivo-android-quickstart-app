package com.plivo.plivosimplequickstart;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.plivo.endpoint.AccessTokenListener;

import org.json.JSONObject;

import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TokenGenerator implements AccessTokenListener {
    private final Context context;
    private PlivoBackEnd.BackendListener listener = null;
    private String TAG = TokenGenerator.class.getSimpleName();
    private HashMap payload;
    private String certId = "";

    public TokenGenerator(Context context) {
        this.context = context;
    }

    public void setListener(PlivoBackEnd.BackendListener listener) {
        this.listener = listener;
    }

    public void loginForIncoming(HashMap map,String certificateId) {
        this.payload = map;
        this.certId = certificateId;
    }

    public AccessTokenListener getListener() {
        return this;
    }

    @Override
    public void getAccessToken() {
        Log.d(TAG, "onTokenExpired: ");
        generateToken();
        if (listener != null)
            listener.getAccessToken();
    }


    private void generateToken() {
        Pref.newInstance(context).setBoolean(Constants.IS_LOGIN_WITH_USERNAME, true);
        new Thread(() -> {
            String sub;
            sub = Pref.newInstance(context).getString(Constants.LOGIN_USERNAME);
            if (sub.isEmpty()) sub = "plivoUser";
            long nbf = System.currentTimeMillis() / 1000;
            long exp = nbf + 240;
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("application/json");
            Log.d(TAG, "generateToken: sub: " + sub);
            Log.d(TAG, "generateToken: nbf: " + nbf);
            RequestBody body = RequestBody.create(mediaType, "{\n    \"iss\": \"MAY2RJNZKZNJMWOTG4NT\",\n    \"sub\": \"" + sub + "\",\n    \"per\": {\n        \"voice\": {\n            \"incoming_allow\": true,\n            \"outgoing_allow\": true\n        }\n    },\n    \"exp\": " + exp + "\n}\n");
            Request request = new Request.Builder()
                    .url("https://api.plivo.com/v1/Account/MAY2RJNZKZNJMWOTG4NT/JWT/Token")
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Basic TUFZMlJKTlpLWk5KTVdPVEc0TlQ6WWpJM1pXVmpPV0poTW1Kak5USXhNakJtTkdJeVlUUmtZVGd3TUdSaA==")
                    .build();
            try {
                /*runOnUiThread(() -> {
                    progressDialog.setMessage("generating..");
                    progressDialog.show();
                });*/
                Response response = client.newCall(request).execute();
//                progressDialog.dismiss();
                String responseData = response.body().string();
                Log.d(TAG, "run: generateToken " + responseData);

                if (response.code() == 200) {
                    JSONObject jsonResponse = new JSONObject(responseData);
                    String token = jsonResponse.getString("token");
                    Log.d(TAG, "run: generateToken jwtToken" + token);
                    Pref.newInstance(context).setString(Constants.JWT_ACCESS_TOKEN, token);
                    FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(instanceIdResult -> {
                        Log.d(TAG, "generateToken: device-token" + instanceIdResult.getToken());
                        if ( payload != null) {
                            ((App) context).backend().loginForIncomingWithJwt(instanceIdResult.getToken(), token, certId, payload);
                        } else {
                            ((App) context).backend().loginWithJwtToken(instanceIdResult.getToken(), token);
                        }
                    });
                } else {
                    Toast.makeText(context, response.message(), Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Log.d(TAG, "run: generateToken " + e);
                e.printStackTrace();
            }
        }).start();

    }
}
