package com.metkring.lapmart.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.Query;
import com.metkring.lapmart.R;
import com.metkring.lapmart.adapter.BrandAdapter;
import com.metkring.lapmart.adapter.ProductAdapter;
import com.metkring.lapmart.databinding.FragmentHomeBinding;
import com.metkring.lapmart.helper.CartManager;
import com.metkring.lapmart.model.Brand;
import com.metkring.lapmart.model.Product;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class HomeFragment extends Fragment implements OnMapReadyCallback {
    private int activeRequests = 0;
    private FragmentHomeBinding binding;
    private BrandAdapter brandAdapter;
    private ProductAdapter adapter;
    private List<Product> productList;
    private GoogleMap mMap;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        productList = new ArrayList<>();
        loadBrand(view);

        call();
        loadMap(view);
        loadProduct(view, "All");

        binding.searchBtn.setOnClickListener(v -> {
            String searchText = binding.searchEditText.getText().toString().trim();
            hideKeyboard();
            if (!searchText.isEmpty()) {
                searchInDatabase(searchText);
            } else {
                loadProduct(view, "All");
            }
        });

        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().isEmpty()) {
                    hideKeyboard();
                    loadProduct(getView(), "All");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

    }

    private void searchInDatabase(String searchText) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String lowerCaseSearchText = searchText.toLowerCase().trim();

        setLoading(true);

        db.collection("products")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    setLoading(false);
                    productList.clear();

                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Product product = document.toObject(Product.class);
                        if (product != null) {
                            product.setId(document.getId());

                            boolean matchBrand = product.getBrand() != null && product.getBrand().
                                    toLowerCase().contains(lowerCaseSearchText);
                            boolean matchModel = product.getModel() != null && product.getModel().
                                    toLowerCase().contains(lowerCaseSearchText);
                            boolean matchProcessor = product.getProcessor() != null && product.
                                    getProcessor().toLowerCase().contains(lowerCaseSearchText);

                            if (matchBrand || matchModel || matchProcessor) {
                                productList.add(product);
                            }
                        }
                    }

                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                        if (productList.isEmpty()) {
                            binding.productRecyclerView.setVisibility(View.GONE);
                            binding.emptyStateLayout.setVisibility(View.VISIBLE);
                        } else {
                            binding.productRecyclerView.setVisibility(View.VISIBLE);
                            binding.emptyStateLayout.setVisibility(View.GONE);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Log.e("FirestoreError", "Search Error: " + e.getMessage());
                });
    }


    private void loadBrand(View view) {
        List<Brand> brandsList = new ArrayList<>();

        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        binding.brandRecyclerView.setLayoutManager(layoutManager);


        brandAdapter = new BrandAdapter(brandsList, brandName -> {
            loadProduct(view, brandName);
        });
        binding.brandRecyclerView.setAdapter(brandAdapter);

        setLoading(true);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("brands")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    setLoading(false);
                    if (!queryDocumentSnapshots.isEmpty()) {
                        brandsList.clear();
                        brandsList.add(new Brand("All"));

                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            Brand brand = document.toObject(Brand.class);
                            if (brand != null) {
                                brandsList.add(brand);
                            }
                        }
                        brandAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Log.e("FirestoreError", "Brands Error: " + e.getMessage());
                });
    }

    private void loadMap(View view) {
        setLoading(true);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void call() {
        ImageButton callBtn = binding.branchCallBtn;

        callBtn.setOnClickListener(v -> {
            String phoneNumber = "0371234567";

            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
        });
    }

    private void loadProduct(View view, String brandName) {
        setLoading(true);
        RecyclerView productRv = binding.productRecyclerView;
        productRv.setLayoutManager(new GridLayoutManager(view.getContext(), 2));

        adapter = new ProductAdapter(productList);
        productRv.setAdapter(adapter);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query;

        if (brandName.equals("All")) {
            query = db.collection("products").
                    orderBy("timestamp", Query.Direction.DESCENDING);
        } else {
            query = db.collection("products").whereEqualTo("brand", brandName);
        }

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
                    setLoading(false);
                    productList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Product product = document.toObject(Product.class);

                        if (product != null){
                            product.setId(document.getId());
                            productList.add(product);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    if (productList.isEmpty()) {
                        binding.productRecyclerView.setVisibility(View.GONE);
                        binding.emptyStateLayout.setVisibility(View.VISIBLE);
                    } else {
                        binding.productRecyclerView.setVisibility(View.VISIBLE);
                        binding.emptyStateLayout.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Log.e("FirestoreError", "Error getting documents: " + e.getMessage());
                });
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        LatLng nikaweratiya = new LatLng(7.7494, 80.1174);
        mMap.addMarker(new MarkerOptions().position(nikaweratiya).
                title("LapMart - Nikaweratiya Branch"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(nikaweratiya, 15f));

        mMap.getUiSettings().setZoomControlsEnabled(true);
        setLoading(false);
    }

    private void hideKeyboard() {
        View view = this.getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            activeRequests++;
        } else {
            if (activeRequests > 0) activeRequests--;
        }

        if (activeRequests > 0) {
            binding.homeProgressBar.setVisibility(View.VISIBLE);
        } else {
            binding.homeProgressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            View bottomNav = getActivity().findViewById(R.id.bottom_navigation_view);
            if (bottomNav != null) {
                bottomNav.setVisibility(View.VISIBLE);
            }
        }
    }

}