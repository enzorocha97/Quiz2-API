package com.example.quiz2;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface OpenFoodFactsApi {
    @GET("cgi/search.pl?json=1")
    Call<SearchResponse> searchProducts(@Query("search_terms") String query);

    @GET("api/v0/product/{barcode}.json")
    Call<ProductResponse> getProductByBarcode(@Path("barcode") String barcode);
}
