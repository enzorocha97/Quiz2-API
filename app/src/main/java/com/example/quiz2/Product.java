package com.example.quiz2;

import com.google.gson.annotations.SerializedName;

public class Product {
    @SerializedName("product_name")
    private String productName;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("brands")
    private String brands;

    @SerializedName("code")
    private String code;

    @SerializedName("ingredients_text")
    private String ingredients;

    @SerializedName("allergens")
    private String allergens;

    @SerializedName("nutriscore_grade")
    private String nutriscore;

    public String getProductName() {
        return productName != null ? productName : "Sin nombre";
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getBrands() {
        return brands != null ? brands : "Marca desconocida";
    }

    public String getCode() {
        return code;
    }

    public String getIngredients() {
        return ingredients != null ? ingredients : "No disponible";
    }

    public String getAllergens() {
        return (allergens != null && !allergens.isEmpty()) ? allergens : "Ninguno detectado";
    }

    public String getNutriscore() {
        return nutriscore != null ? nutriscore.toUpperCase() : "N/A";
    }
}
