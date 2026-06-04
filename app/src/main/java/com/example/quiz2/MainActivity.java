package com.example.quiz2;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.quiz2.databinding.ActivityMainBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ProductAdapter adapter;
    private OpenFoodFactsApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupRetrofit();
        setupRecyclerView();
        setupSearchView();
        setupNavigation();
        
        binding.btnCheckBarcode.setOnClickListener(v -> {
            if (binding.etBarcode.getText() != null) {
                String code = binding.etBarcode.getText().toString().trim();
                if (!code.isEmpty()) {
                    fetchProductDetails(code);
                }
            }
        });
    }

    private void setupNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_search) {
                binding.searchCard.setVisibility(View.VISIBLE);
                binding.barcodeCard.setVisibility(View.GONE);
                binding.titleResults.setText(R.string.explore_title);
                return true;
            } else if (id == R.id.nav_barcode) {
                binding.searchCard.setVisibility(View.GONE);
                binding.barcodeCard.setVisibility(View.VISIBLE);
                binding.titleResults.setText(R.string.verifier_title);
                return true;
            }
            return false;
        });
    }

    private void setupRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://world.openfoodfacts.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(OpenFoodFactsApi.class);
    }

    private void setupRecyclerView() {
        adapter = new ProductAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
        adapter.setOnProductClickListener(product -> fetchProductDetails(product.getCode()));
    }

    private void setupSearchView() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchProducts(query);
                binding.searchView.clearFocus();
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) { return false; }
        });
    }

    private void searchProducts(String query) {
        binding.progressBar.setVisibility(View.VISIBLE);
        api.searchProducts(query).enqueue(new Callback<SearchResponse>() {
            @Override
            public void onResponse(@NonNull Call<SearchResponse> call, @NonNull Response<SearchResponse> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setProducts(response.body().getProducts());
                }
            }
            @Override
            public void onFailure(@NonNull Call<SearchResponse> call, @NonNull Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, R.string.network_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchProductDetails(String barcode) {
        binding.progressBar.setVisibility(View.VISIBLE);
        api.getProductByBarcode(barcode).enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProductResponse> call, @NonNull Response<ProductResponse> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().getStatus() == 1) {
                    showProductDialog(response.body().getProduct());
                } else {
                    Toast.makeText(MainActivity.this, R.string.no_product_found, Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<ProductResponse> call, @NonNull Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, R.string.error_loading_details, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showProductDialog(Product product) {
        String message = getString(R.string.dialog_verified_msg, product.getBrands(), product.getAllergens());
        new AlertDialog.Builder(this)
                .setTitle(product.getProductName())
                .setMessage(message)
                .setPositiveButton(R.string.dialog_ok, null)
                .show();
    }
}
