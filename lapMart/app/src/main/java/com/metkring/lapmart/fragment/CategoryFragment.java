package com.metkring.lapmart.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.metkring.lapmart.adapter.ProductAdapter;
import com.metkring.lapmart.databinding.FragmentCategoryBinding;
import com.metkring.lapmart.model.Product;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class CategoryFragment extends Fragment {

    private FragmentCategoryBinding binding;
    private FirebaseFirestore db;
    private ProductAdapter adapter;
    private List<Product> productList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCategoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        productList = new ArrayList<>();

        // RecyclerView එක Setup කිරීම (කලින් Home එකේ වගේම Grid 2ක්)
        binding.rvFilteredProducts.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        adapter = new ProductAdapter(productList);
        binding.rvFilteredProducts.setAdapter(adapter);

        loadBrandsToDropdown();
        loadRamsToDropdown();
        loadProcessorsToDropdown();

        // 🔴 Slider එක අදිනකොට ගාණ පෙන්වන්න
        binding.priceRangeSlider.addOnChangeListener((slider, value, fromUser) -> {
            List<Float> values = slider.getValues();
            binding.tvMinPrice.setText("Rs." + String.format("%,.0f", values.get(0)));
            binding.tvMaxPrice.setText("Rs." + String.format("%,.0f", values.get(1)));
        });

        // 🔴 Apply Filter එබුවම
        binding.btnApplyFilter.setOnClickListener(v -> applyFilters());

        // 🔴 Clear Filters එබුවම
        binding.btnClearFilter.setOnClickListener(v -> clearFilters());
    }

    // --- Dropdowns වලට දත්ත ලෝඩ් කිරීම --- (කලින් කේතයමයි)
    private void loadBrandsToDropdown() {
        List<String> brandList = new ArrayList<>();
        brandList.add("All Brands");
        db.collection("brands").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                String name = doc.getString("name");
                if (name != null) brandList.add(name);
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, brandList);
            binding.dropdownBrand.setAdapter(adapter);
        });
    }

    private void loadRamsToDropdown() {
        List<String> ramList = new ArrayList<>();
        ramList.add("All");
        db.collection("rams").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                String name = doc.getString("name");
                if (name != null) ramList.add(name);
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, ramList);
            binding.dropdownRam.setAdapter(adapter);
        });
    }

    private void loadProcessorsToDropdown() {
        List<String> procList = new ArrayList<>();
        procList.add("All");
        db.collection("processors").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                String name = doc.getString("name");
                if (name != null) procList.add(name);
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, procList);
            binding.dropdownProcessor.setAdapter(adapter);
        });
    }

    // --- 🔴 ෆිල්ටර් කරන ප්‍රධාන කොටස (Core Logic) ---
    private void applyFilters() {
        // UI වෙනස්කම් (Loading පෙන්වන්න)
        binding.filterScrollView.setVisibility(View.GONE); // ෆිල්ටර් කොටස හංගනවා
        binding.filterProgressBar.setVisibility(View.VISIBLE);
        binding.rvFilteredProducts.setVisibility(View.GONE);
        binding.layoutNoProducts.setVisibility(View.GONE);

        // යූසර් තෝරපු දේවල් ගන්නවා
        String selectedBrand = binding.dropdownBrand.getText().toString().trim();
        String selectedRam = binding.dropdownRam.getText().toString().trim();
        String selectedProcessor = binding.dropdownProcessor.getText().toString().trim();
        List<Float> priceValues = binding.priceRangeSlider.getValues();
        double minPrice = priceValues.get(0);
        double maxPrice = priceValues.get(1);

        // Firestore Query එක හදනවා
        Query query = db.collection("products");

        // 1. Brand එක "All Brands" නෙවෙයි නම් විතරක් Query එකට එකතු කරනවා
        if (!selectedBrand.equals("All Brands") && !selectedBrand.isEmpty()) {
            query = query.whereEqualTo("brand", selectedBrand);
        }

        // 2. RAM එක "All" නෙවෙයි නම්
        if (!selectedRam.equals("All") && !selectedRam.isEmpty()) {
            query = query.whereEqualTo("ram", selectedRam);
        }

        // 3. Processor එක "All" නෙවෙයි නම්
        if (!selectedProcessor.equals("All") && !selectedProcessor.isEmpty()) {
            query = query.whereEqualTo("processor", selectedProcessor);
        }

        // 4. Price එකට Filter කිරීම
        query = query.whereGreaterThanOrEqualTo("price", minPrice)
                .whereLessThanOrEqualTo("price", maxPrice);

        // දත්ත අරගෙන එනවා
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

                    // ප්‍රතිඵල නැත්නම් Empty State එක පෙන්වනවා
                    if (productList.isEmpty()) {
                        binding.layoutNoProducts.setVisibility(View.VISIBLE);
                        Toasty.info(requireContext(), "No products matched your filters.", Toasty.LENGTH_SHORT).show();
                    } else {
                        binding.rvFilteredProducts.setVisibility(View.VISIBLE);
                        Toasty.success(requireContext(), "Found " + productList.size() + " products!", Toasty.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    binding.filterProgressBar.setVisibility(View.GONE);
                    binding.filterScrollView.setVisibility(View.VISIBLE); // Error ආවොත් ආයෙත් ෆිල්ටර් එක පෙන්නනවා
                    Log.e("CategoryFragment", "Filter Error: " + e.getMessage());
                    Toasty.error(requireContext(), "Error applying filters!" + e.getMessage(), Toasty.LENGTH_LONG).show();
                });
    }

    // --- ෆිල්ටර් අයින් කරන කොටස ---
    private void clearFilters() {
        // Dropdowns මුලට ගේනවා
        binding.dropdownBrand.setText("All Brands", false);
        binding.dropdownRam.setText("All", false);
        binding.dropdownProcessor.setText("All", false);

        // Slider එක මුලට ගේනවා
        binding.priceRangeSlider.setValues(0.0f, 1000000.0f);
        binding.tvMinPrice.setText("0");
        binding.tvMaxPrice.setText("1,000,000");

        // RecyclerView එක හංගලා ආයෙත් Filter කොටස පෙන්වනවා
        binding.rvFilteredProducts.setVisibility(View.GONE);
        binding.layoutNoProducts.setVisibility(View.GONE);
        binding.filterScrollView.setVisibility(View.VISIBLE);

        productList.clear();
        adapter.notifyDataSetChanged();

        Toasty.normal(requireContext(), "Filters Cleared").show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}