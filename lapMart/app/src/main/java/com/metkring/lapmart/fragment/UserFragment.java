package com.metkring.lapmart.fragment;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.metkring.lapmart.R;
import com.metkring.lapmart.adapter.ProductAdapter;
import com.metkring.lapmart.databinding.FragmentUserBinding;
import com.metkring.lapmart.model.Product;

import java.util.ArrayList;
import java.util.List;

public class UserFragment extends Fragment {

    private FragmentUserBinding binding;
    private ProductAdapter adapter;
    private List<Product> productList;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentUserBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        backOption();
        productList = new ArrayList<>();
        loadProduct(view);
    }

    private void loadProduct(View view) {
        RecyclerView productRv = binding.productRecyclerView;
        productRv.setLayoutManager(new GridLayoutManager(view.getContext(), 2));

        adapter = new ProductAdapter(productList);
        productRv.setAdapter(adapter);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("products").get().addOnSuccessListener(queryDocumentSnapshots -> {
                    binding.homeProgressBar.setVisibility(View.GONE);
                    productList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Product product = document.toObject(Product.class);

                        if (product != null) {
                            product.setId(document.getId());
                            productList.add(product);
                        }
                    }
                    adapter.notifyDataSetChanged();

                })
                .addOnFailureListener(e -> {
                    binding.homeProgressBar.setVisibility(View.GONE);
                    Log.e("FirestoreError", "Error getting documents: " + e.getMessage());
                });
    }

    private void backOption(){
        requireActivity().getOnBackPressedDispatcher()
                .addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getActivity() != null) {
                    BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation_view);

                    if (bottomNav.getSelectedItemId() != R.id.bottom_nav_home) {
                        bottomNav.setSelectedItemId(R.id.bottom_nav_home);
                    } else {
                        setEnabled(false);
                        requireActivity().onBackPressed();
                    }
                }
            }
        });
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