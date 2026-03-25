package com.metkring.lapmartadmin.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.metkring.lapmartadmin.R;
import com.metkring.lapmartadmin.activity.MainActivity;
import com.metkring.lapmartadmin.adapter.StockAdapter;
import com.metkring.lapmartadmin.databinding.FragmentStockBinding;
import com.metkring.lapmartadmin.model.Product;

import java.util.ArrayList;
import java.util.List;

public class StockFragment extends Fragment {

    private FragmentStockBinding binding;
    private StockAdapter adapter;
    private List<Product> productList;
    private FirebaseFirestore db;

    private ListenerRegistration stockListener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentStockBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        hideBottomNavigation();

        db = FirebaseFirestore.getInstance();
        productList = new ArrayList<>();

        binding.rvStock.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new StockAdapter(getContext(), productList, this::showUpdateConfirmationDialog);
        binding.rvStock.setAdapter(adapter);

        loadProductsFromFirestore();

        binding.btnBack.setOnClickListener(v -> navigateBackToHome());

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        navigateBackToHome();
                    }
                });

        binding.btnAddNewProduct.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new AddProductFragment())
                    .addToBackStack(null)
                    .commit();

        });

        return view;
    }

    private void showUpdateConfirmationDialog(Product product) {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_confirm_update, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.
                    ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        androidx.appcompat.widget.AppCompatButton btnCancel =
                dialogView.findViewById(R.id.btnDialogCancel);
        androidx.appcompat.widget.AppCompatButton btnConfirm =
                dialogView.findViewById(R.id.btnDialogConfirm);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();

            Bundle bundle = new Bundle();
            bundle.putSerializable("product_to_update", product);

            AddProductFragment addProductFragment = new AddProductFragment();
            addProductFragment.setArguments(bundle);

            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, addProductFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        dialog.show();
    }

    private void navigateBackToHome() {
        showBottomNavigation();
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();

            BottomNavigationView bottomNav = mainActivity.findViewById(R.id.bottom_navigation_view);
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.bottom_nav_home);
            }
        }
    }

    private void showBottomNavigation() {
        if (getActivity() != null) {
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation_view);
            if (bottomNav != null) {
                bottomNav.setVisibility(View.VISIBLE);
            }
        }
    }

    private void loadProductsFromFirestore() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.rvStock.setVisibility(View.GONE);
        binding.layoutEmptyState.setVisibility(View.GONE);

        stockListener = db.collection("products")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException error) {
                        if (binding == null) return;
                        if (error != null) {
                            Log.e("StockFragment", "Error loading products", error);
                            binding.progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Failed to load products",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        productList.clear();

                        if (value != null && !value.isEmpty()) {
                            for (QueryDocumentSnapshot doc : value) {
                                Product product = doc.toObject(Product.class);
                                productList.add(product);
                            }

                            adapter.notifyDataSetChanged();

                            binding.progressBar.setVisibility(View.GONE);
                            binding.layoutEmptyState.setVisibility(View.GONE);
                            binding.rvStock.setVisibility(View.VISIBLE);

                        } else {
                            binding.progressBar.setVisibility(View.GONE);
                            binding.rvStock.setVisibility(View.GONE);
                            binding.layoutEmptyState.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private void hideBottomNavigation() {
        if (getActivity() != null) {
            BottomNavigationView bottomNavigationView =
                    getActivity().findViewById(R.id.bottom_navigation_view);
            if (bottomNavigationView != null) {
                bottomNavigationView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (stockListener != null) {
            stockListener.remove();
            stockListener = null;
        }
        binding = null;
    }
}