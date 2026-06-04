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
import com.squareup.picasso.Picasso;
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
        setupClickListeners();
        loadHomeImage();
    }

    private void loadHomeImage() {
        Picasso.get()
                .load("https://cdn-icons-png.flaticon.com/512/12401/12401714.png")
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.stat_notify_error)
                .into(binding.imgHome);
    }

    private void setupClickListeners() {
        binding.btnCheckBarcode.setOnClickListener(v -> {
            if (binding.etBarcode.getText() != null) {
                String code = binding.etBarcode.getText().toString().trim();
                if (!code.isEmpty()) {
                    fetchProductDetails(code);
                }
            }
        });

        View.OnClickListener backToHome = v -> {
            showHome();
            binding.bottomNavigation.setSelectedItemId(R.id.nav_home);
        };
        binding.btnBackFromSearch.setOnClickListener(backToHome);
        binding.btnBackFromBarcode.setOnClickListener(backToHome);
    }

    private void setupNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                showHome();
                return true;
            } else if (id == R.id.nav_search) {
                showSearch();
                return true;
            } else if (id == R.id.nav_barcode) {
                showBarcodeVerifier();
                return true;
            }
            return false;
        });
    }

    private void showHome() {
        binding.homeLayout.setVisibility(View.VISIBLE);
        binding.searchCard.setVisibility(View.GONE);
        binding.barcodeCard.setVisibility(View.GONE);
        binding.titleResults.setVisibility(View.GONE);
        binding.recyclerView.setVisibility(View.GONE);
    }

    private void showSearch() {
        binding.homeLayout.setVisibility(View.GONE);
        binding.searchCard.setVisibility(View.VISIBLE);
        binding.barcodeCard.setVisibility(View.GONE);
        binding.titleResults.setVisibility(View.VISIBLE);
        binding.recyclerView.setVisibility(View.VISIBLE);
        binding.titleResults.setText(R.string.explore_title);
    }

    private void showBarcodeVerifier() {
        binding.homeLayout.setVisibility(View.GONE);
        binding.searchCard.setVisibility(View.GONE);
        binding.barcodeCard.setVisibility(View.VISIBLE);
        binding.titleResults.setVisibility(View.VISIBLE);
        binding.recyclerView.setVisibility(View.VISIBLE);
        binding.titleResults.setText(R.string.verifier_title);
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
