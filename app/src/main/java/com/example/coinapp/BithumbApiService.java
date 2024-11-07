package com.example.coinapp;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface BithumbApiService {
    @GET("/v1/market/all")
    Call<JsonArray> getMarketData(@Query("isDetails") boolean isDetails);

    @GET("public/candlestick/{coin_pair}")
    Call<JsonObject> getCandleData(@Path("coin_pair") String coinPair);

}
