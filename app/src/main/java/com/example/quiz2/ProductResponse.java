package com.example.quiz2;

import com.google.gson.annotations.SerializedName;

public class ProductResponse {
    @SerializedName("product")
    private Product product;

    @SerializedName("status")
    private int status;

    public Product getProduct() {
        return product;
    }

    public int getStatus() {
        return status;
    }
}
