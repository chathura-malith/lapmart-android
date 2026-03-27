package com.metkring.lapmart.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.metkring.lapmart.R;
import com.metkring.lapmart.adapter.ProductAdapter;
import com.metkring.lapmart.databinding.FragmentFilterBinding;
import com.metkring.lapmart.model.Product;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class FilterFragment extends Fragment {

    private FragmentFilterBinding binding;
    private FirebaseFirestore db;
    private ProductAdapter adapter;
    private List<Product> productList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFilterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        hideBottomNavigation();
        db = FirebaseFirestore.getInstance();
        productList = new ArrayList<>();

        binding.rvFilteredProducts.setLayoutManager(
                new GridLayoutManager(requireContext(), 2));
        adapter = new ProductAdapter(productList);
        binding.rvFilteredProducts.setAdapter(adapter);

        loadBrandsToDropdown();
        loadRamsToDropdown();
        loadProcessorsToDropdown();

        binding.priceRangeSlider.addOnChangeListener(
                (slider, value, fromUser) -> {
                    List<Float> values = slider.getValues();
                    binding.tvMinPrice.setText("Rs." + String.format("%,.0f", values.get(0)));
                    binding.tvMaxPrice.setText("Rs." + String.format("%,.0f", values.get(1)));
                });

        binding.btnApplyFilter.setOnClickListener(v -> applyFilters());
        binding.btnClearFilter.setOnClickListener(v -> clearFilters());
        binding.btnBack.setOnClickListener(v -> handleBackNavigation());

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new androidx.activity.OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        handleBackNavigation();
                    }
                });
    }

    private void handleBackNavigation() {
        if (binding.rvFilteredProducts.getVisibility() == View.VISIBLE ||
                binding.layoutNoProducts.getVisibility() == View.VISIBLE) {
            binding.rvFilteredProducts.setVisibility(View.GONE);
            binding.layoutNoProducts.setVisibility(View.GONE);
            binding.filterScrollView.setVisibility(View.VISIBLE);
        } else {
            if (getActivity() != null) {
                BottomNavigationView bottomNav = getActivity().findViewById(
                        R.id.bottom_navigation_view);
                if (bottomNav != null) {
                    bottomNav.setSelectedItemId(R.id.bottom_nav_home);
                }
                getActivity().getSupportFragmentManager().popBackStack();
            }
        }
    }

    private void hideBottomNavigation() {
        if (getActivity() != null) {
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation_view);
            if (bottomNav != null) {
                bottomNav.setVisibility(View.GONE);
            }
        }
    }
    private void loadBrandsToDropdown() {
        List<String> brandList = new ArrayList<>();
        brandList.add("All Brands");
        db.collection("brands").get().addOnSuccessListener(
                queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("name");
                        if (name != null) brandList.add(name);
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_dropdown_item_1line, brandList);
                    binding.dropdownBrand.setAdapter(adapter);
                });
    }

    private void loadRamsToDropdown() {
        List<String> ramList = new ArrayList<>();
        ramList.add("All");
        db.collection("rams").get().addOnSuccessListener(
                queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("name");
                        if (name != null) ramList.add(name);
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_dropdown_item_1line, ramList);
                    binding.dropdownRam.setAdapter(adapter);
                });
    }

    private void loadProcessorsToDropdown() {
        List<String> procList = new ArrayList<>();
        procList.add("All");
        db.collection("processors").get().addOnSuccessListener(
                queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("name");
                        if (name != null) procList.add(name);
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_dropdown_item_1line, procList);
                    binding.dropdownProcessor.setAdapter(adapter);
                });
    }

    private void applyFilters() {
        binding.filterScrollView.setVisibility(View.GONE);
        binding.filterProgressBar.setVisibility(View.VISIBLE);
        binding.rvFilteredProducts.setVisibility(View.GONE);
        binding.layoutNoProducts.setVisibility(View.GONE);

        String selectedBrand = binding.dropdownBrand.getText().toString().trim();
        String selectedRam = binding.dropdownRam.getText().toString().trim();
        String selectedProcessor = binding.dropdownProcessor.getText().toString().trim();
        List<Float> priceValues = binding.priceRangeSlider.getValues();
        double minPrice = priceValues.get(0);
        double maxPrice = priceValues.get(1);

        Query query = db.collection("products");

        if (!selectedBrand.equals("All Brands") && !selectedBrand.isEmpty()) {
            query = query.whereEqualTo("brand", selectedBrand);
        }

        if (!selectedRam.equals("All") && !selectedRam.isEmpty()) {
            query = query.whereEqualTo("ram", selectedRam);
        }

        if (!selectedProcessor.equals("All") && !selectedProcessor.isEmpty()) {
            query = query.whereEqualTo("processor", selectedProcessor);
        }

        query = query.whereGreaterThanOrEqualTo("price", minPrice)
                .whereLessThanOrEqualTo("price", maxPrice);

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productList.clear();
                    binding.filterProgressBar.setVisibility(View.GONE);

                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Product product = document.toObject(Product.class);
                        if (product != null) {
                            product.setId(document.getId());
                            productList.add(product);
                        }
                    }

                    adapter.notifyDataSetChanged();

                    if (productList.isEmpty()) {
                        binding.layoutNoProducts.setVisibility(View.VISIBLE);
                        Toasty.info(requireContext(), "No products matched your filters.",
                                Toasty.LENGTH_SHORT).show();
                    } else {
                        binding.rvFilteredProducts.setVisibility(View.VISIBLE);
                        Toasty.success(requireContext(), "Found " + productList.size()
                                + " products!", Toasty.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    binding.filterProgressBar.setVisibility(View.GONE);
                    binding.filterScrollView.setVisibility(View.VISIBLE);
                    Log.i("CategoryFragment", "Filter Error: " + e.getMessage());
                });
    }

    private void clearFilters() {
        binding.dropdownBrand.setText("All Brands", false);
        binding.dropdownRam.setText("All", false);
        binding.dropdownProcessor.setText("All", false);

        binding.priceRangeSlider.setValues(0.0f, 1000000.0f);
        binding.tvMinPrice.setText("0");
        binding.tvMaxPrice.setText("1,000,000");

        binding.rvFilteredProducts.setVisibility(View.GONE);
        binding.layoutNoProducts.setVisibility(View.GONE);
        binding.filterScrollView.setVisibility(View.VISIBLE);

        productList.clear();
        adapter.notifyDataSetChanged();

        Toasty.info(requireContext(), "Filters Cleared").show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}