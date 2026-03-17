package com.metkring.lapmart.fragment;



import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.metkring.lapmart.R;
import com.metkring.lapmart.adapter.ProductAdapter;
import com.metkring.lapmart.adapter.ProductDetailImageAdapter;
import com.metkring.lapmart.adapter.SimilarProductAdapter;
import com.metkring.lapmart.databinding.FragmentProductDetailBinding;
import com.metkring.lapmart.model.Product;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class ProductDetailFragment extends Fragment {

    private FragmentProductDetailBinding binding;
    private FirebaseFirestore db;

    private int quantity = 1;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProductDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        View bottomNav = getActivity().findViewById(R.id.bottom_navigation_view);
        if (bottomNav != null) {
            bottomNav.setVisibility(View.GONE);
        }
        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            getActivity().getOnBackPressedDispatcher().onBackPressed();
        });

        if (getArguments() != null) {
            String productId = getArguments().getString("product_id");
            if (productId != null) {
                loadProductDetails(productId);
            }
        }

        quantityChange();
    }

    private void quantityChange(){
        binding.btnIncreaseQuantity.setOnClickListener(v -> {
            quantity++;
            binding.tvQuantity.setText(String.valueOf(quantity));
        });

        binding.btnDecreaseQuantity.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                binding.tvQuantity.setText(String.valueOf(quantity));
            } else {
                Toasty.error(getContext(), "Minimum quantity is 1", Toast.LENGTH_SHORT, true).show();
            }
        });
    }

    private void loadProductDetails(String productId) {
        db.collection("products").document(productId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Product product = documentSnapshot.toObject(Product.class);

                        if (product != null) {
                            product.setId(documentSnapshot.getId());
                            updateUI(product);

                            binding.detailProgressBar.setVisibility(View.GONE);
                            binding.scrollView.setVisibility(View.VISIBLE);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUI(Product product) {
        binding.txtDetailTitle.setText(product.getModel());
        binding.txtDetailPrice.setText("Rs." + String.format("%,.2f", product.getPrice()));

        if (product.getDescription() != null) {
            String rawDesc = product.getDescription();

            String cleanDesc = rawDesc.replace("\\n", " ")
                    .replace("\n", " ");

            String formattedDesc = cleanDesc.replace("–", "\n–");

            binding.txtDetailDesc.setText(formattedDesc.trim());
        }

        if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
            ProductDetailImageAdapter adapter = new ProductDetailImageAdapter(product.getImageUrls());
            binding.detailViewPager.setAdapter(adapter);

            binding.dotsIndicator.attachTo(binding.detailViewPager);
        }

        loadSimilarProducts(product.getBrand(), product.getId());

    }

    private void loadSimilarProducts(String brand, String currentProductId) {
        List<Product> similarList = new ArrayList<>();

        db.collection("products")
                .whereEqualTo("brand", brand)
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        if (!doc.getId().equals(currentProductId)) {
                            Product product = doc.toObject(Product.class);
                            product.setId(doc.getId());
                            similarList.add(product);
                        }
                    }
                    if (similarList.isEmpty()) {
                        binding.layoutSimilarProducts.setVisibility(View.GONE);
                    } else {
                        binding.layoutSimilarProducts.setVisibility(View.VISIBLE);
                        setupSimilarRecyclerView(similarList);
                    }
                });
    }

    private void setupSimilarRecyclerView(List<Product> list) {
        binding.similarProductsRecyclerView.setLayoutManager(
                new LinearLayoutManager(getContext(),
                        LinearLayoutManager.HORIZONTAL, false)
        );

        SimilarProductAdapter adapter = new SimilarProductAdapter(list);
        binding.similarProductsRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}