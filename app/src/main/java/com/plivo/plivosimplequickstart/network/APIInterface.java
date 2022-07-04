package com.plivo.plivosimplequickstart.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIInterface {

    @Headers({
            "Content-Type: application/json",
            "Authorization: Basic TUFZMlJKTlpLWk5KTVdPVEc0TlQ6WWpJM1pXVmpPV0poTW1Kak5USXhNakJtTkdJeVlUUmtZVGd3TUdSaA==",
    })
    @POST("v1/Account/MAY2RJNZKZNJMWOTG4NT/JWT/Token")
    Call<TokenResponse> getToken(@Body BodyInput bodyInput);

}
