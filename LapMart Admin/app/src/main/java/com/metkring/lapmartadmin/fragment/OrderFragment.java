package com.metkring.lapmartadmin.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.metkring.lapmartadmin.R;
import com.metkring.lapmartadmin.activity.MainActivity;
import com.metkring.lapmartadmin.adapter.AdminOrderAdapter;
import com.metkring.lapmartadmin.databinding.FragmentOrderBinding;
import com.metkring.lapmartadmin.model.Order;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class OrderFragment extends Fragment {

    private FragmentOrderBinding binding;
    private FirebaseFirestore db;
    private AdminOrderAdapter adapter;
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

        db = FirebaseFirestore.getInstance();
        allOrdersList = new ArrayList<>();
        filteredOrderList = new ArrayList<>();

        binding.btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        setupRecyclerView();
        setupTabLayout();
        loadAllOrders();

        binding.btnBack.setOnClickListener(v -> navigateBackToHome());

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        navigateBackToHome();
                    }
                });
    }

    private void navigateBackToHome() {
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

        adapter = new AdminOrderAdapter(filteredOrderList, requireContext(),
                this::showStatusPopupMenu);

        binding.rvOrders.setAdapter(adapter);
    }

    private void loadAllOrders() {
        binding.orderProgressBar.setVisibility(View.VISIBLE);
        binding.rvOrders.setVisibility(View.GONE);
        binding.layoutEmptyOrders.setVisibility(View.GONE);

        db.collection("orders")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (!isAdded() || binding == null) return;

                    binding.orderProgressBar.setVisibility(View.GONE);

                    if (error != null) {
                        Log.e("OrdersFragment", "Error loading orders", error);
                        return;
                    }

                    allOrdersList.clear();
                    if (value != null) {
                        for (DocumentSnapshot doc : value) {
                            Order order = doc.toObject(Order.class);
                            if (order != null) {
                                order.setOrderId(doc.getId());
                                allOrdersList.add(order);
                            }
                        }
                    }

                    filterOrdersByStatus(binding.tabLayoutOrders.getSelectedTabPosition());
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
            case 0: targetStatus = "Pending"; break;
            case 1: targetStatus = "Processing"; break;
            case 2: targetStatus = "Shipped"; break;
            case 3: targetStatus = "Delivered"; break;
        }

        for (Order order : allOrdersList) {
            if (order.getStatus() != null && order.getStatus().equalsIgnoreCase(targetStatus)) {
                filteredOrderList.add(order);
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

    private void showStatusPopupMenu(View anchorView, Order order) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), anchorView);
        popupMenu.getMenu().add("Pending");
        popupMenu.getMenu().add("Processing");
        popupMenu.getMenu().add("Shipped");
        popupMenu.getMenu().add("Delivered");

        popupMenu.setOnMenuItemClickListener(item -> {
            String newStatus = item.getTitle().toString();
            if (!newStatus.equalsIgnoreCase(order.getStatus())) {
                updateOrderStatusInFirebase(order.getOrderId(), newStatus);
            }
            return true;
        });

        popupMenu.show();
    }

    private void updateOrderStatusInFirebase(String orderId, String newStatus) {
        binding.orderProgressBar.setVisibility(View.VISIBLE);

        db.collection("orders").document(orderId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    if (isAdded() && binding != null) {
                        binding.orderProgressBar.setVisibility(View.GONE);
                        Toasty.success(requireContext(), "Order status updated to "
                                + newStatus, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded() && binding != null) {
                        binding.orderProgressBar.setVisibility(View.GONE);
                        Toasty.error(requireContext(), "Failed to update status",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}