package com.plivo.plivosimplequickstart.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIInterface {

    @Headers({
            "Content-Type: application/json",
            "Authorization: Basic TUFEQ0hBTkRSRVNIMDJUQU5LMDY6T1Rsak5tVm1PR1ZrTkdaaE5qSmxPV0l5TVdNMFpESTBaalF3WkRkaw==",
    })
    @POST("v1/Account/MADCHANDRESH02TANK06/JWT/Token")
    Call<TokenResponse> getToken(@Body BodyInput bodyInput);

}
