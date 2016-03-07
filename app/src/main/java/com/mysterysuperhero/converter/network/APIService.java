package com.mysterysuperhero.converter.network;


import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by dmitri on 06.03.16.
 */
public interface APIService {

    class Currency {
        private String  id;
        private String description;

        public String getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }
    }

    @Headers({
            "X-Mashape-Key: xvtJ6rRJAVmshbFasYHld3ERssImp1SWJWfjsnVUzLXgfE7U53",
            "Accept: application/json"
    })
    @GET("/availablecurrencies")
    Call<ArrayList<Currency>> getAvailableCurrency();

    class Conversion {
        String  from;
        String to;
        float from_amount;
        float to_amount;
    }

    @Headers({"X-Mashape-Key: xvtJ6rRJAVmshbFasYHld3ERssImp1SWJWfjsnVUzLXgfE7U53",
            "Accept: application/json"})
    @GET("/")
    Call<Conversion> convert(@Query("from") String from, @Query("from_amount") String from_amount, @Query("to") String to);
}
