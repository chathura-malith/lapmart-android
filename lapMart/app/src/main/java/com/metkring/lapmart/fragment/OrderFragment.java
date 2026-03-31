package com.metkring.lapmart.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.metkring.lapmart.R;
import com.metkring.lapmart.adapter.OrderAdapter;
import com.metkring.lapmart.databinding.FragmentOrderBinding;
import com.metkring.lapmart.model.Order;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class OrderFragment extends Fragment {

    private FragmentOrderBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private OrderAdapter adapter;
    private List<Order> allOrdersList;
    private List<Order> filteredOrderList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentOrderBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        allOrdersList = new ArrayList<>();
        filteredOrderList = new ArrayList<>();

        hideBottomNavigation();

        binding.btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        binding.btnShopNow.setOnClickListener(v -> {
            if (getActivity() != null) {
                BottomNavigationView bottomNav = getActivity().findViewById
                        (R.id.bottom_navigation_view);
                if (bottomNav != null) {
                    bottomNav.setSelectedItemId(R.id.bottom_nav_home);
                }
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        setupRecyclerView();
        setupTabLayout();

        if (mAuth.getCurrentUser() != null) {
            loadUserOrders();
        } else {
            loadFragment(new SignInFragment());
        }
    }

    private void loadFragment(Fragment fragment) {
        getParentFragmentManager().beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                )
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
    private void setupTabLayout() {
        binding.tabLayoutOrders.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterOrdersByStatus(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerView() {
        binding.rvOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new OrderAdapter(filteredOrderList, requireContext());
        binding.rvOrders.setAdapter(adapter);
    }

    private void loadUserOrders() {
        binding.orderProgressBar.setVisibility(View.VISIBLE);
        binding.rvOrders.setVisibility(View.GONE);
        binding.layoutEmptyOrders.setVisibility(View.GONE);

        String uid = mAuth.getCurrentUser().getUid();

        db.collection("orders")
                .whereEqualTo("userId", uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allOrdersList.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Order order = doc.toObject(Order.class);
                        if (order != null) {
                            order.setOrderId(doc.getId());
                            allOrdersList.add(order);
                        }
                    }

                    binding.orderProgressBar.setVisibility(View.GONE);
                    filterOrdersByStatus(0);

                    if (allOrdersList.isEmpty()) {
                        binding.layoutEmptyOrders.setVisibility(View.VISIBLE);
                        binding.rvOrders.setVisibility(View.GONE);
                    } else {
                        binding.layoutEmptyOrders.setVisibility(View.GONE);
                        binding.rvOrders.setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    binding.orderProgressBar.setVisibility(View.GONE);
                    Log.d("OrderFragment", "Error loading orders", e);
                });
    }

    private void filterOrdersByStatus(int tabPosition) {
        filteredOrderList.clear();

        if (allOrdersList.isEmpty()) {
            updateEmptyState();
            return;
        }

        String targetStatus = "";

        switch (tabPosition) {
            case 0: targetStatus = "All"; break;
            case 1: targetStatus = "Pending"; break;
            case 2: targetStatus = "Processing"; break;
            case 3: targetStatus = "Shipped"; break;
            case 4: targetStatus = "Delivered"; break;
        }

        if (targetStatus.equals("All")) {
            filteredOrderList.addAll(allOrdersList);
        } else {
            for (Order order : allOrdersList) {
                if (order.getStatus() != null && order.getStatus().equalsIgnoreCase(targetStatus)) {
                    filteredOrderList.add(order);
                }
            }
        }

        updateEmptyState();
    }

    private void updateEmptyState() {
        if (filteredOrderList.isEmpty()) {
            binding.layoutEmptyOrders.setVisibility(View.VISIBLE);
            binding.rvOrders.setVisibility(View.GONE);
        } else {
            binding.layoutEmptyOrders.setVisibility(View.GONE);
            binding.rvOrders.setVisibility(View.VISIBLE);
        }
        adapter.notifyDataSetChanged();
    }

    private void hideBottomNavigation() {
        if (getActivity() != null) {
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation_view);
            if (bottomNav != null) {
                bottomNav.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}