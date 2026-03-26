package com.metkring.lapmartadmin.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.metkring.lapmartadmin.R;
import com.metkring.lapmartadmin.databinding.FragmentHomeBinding;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private FirebaseFirestore db;
    private NumberFormat currencyFormatter;
    private SimpleDateFormat timeFormatter;
    private SimpleDateFormat dayFormatter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        showBottomNavigation();

        db = FirebaseFirestore.getInstance();
        currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "LK"));
        timeFormatter = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        dayFormatter = new SimpleDateFormat("EEE", Locale.getDefault());
        binding.ProgressBar.setVisibility(View.VISIBLE);

        setupBarChart();

        loadDashboardData();
    }

    private void setupBarChart() {
        binding.barChart.getDescription().setEnabled(false);
        binding.barChart.getLegend().setEnabled(false);
        binding.barChart.setDrawGridBackground(false);
        binding.barChart.getAxisRight().setEnabled(false);
        binding.barChart.setTouchEnabled(false);

        XAxis xAxis = binding.barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
    }

    private void loadDashboardData() {
        db.collection("users").addSnapshotListener((value,
                                                    error) -> {
            if (error != null || binding == null) return;

            int totalCustomers = value != null ? value.size() : 0;
            binding.tvTotalCustomer.setText(String.valueOf(totalCustomers));
            binding.tvCustomerUpdatedTime.setText("Updated: " + timeFormatter.format(new Date()));
        });

        db.collection("orders").addSnapshotListener((value,
                                                     error) -> {
            if (error != null || binding == null) {
                Log.e("HomeFragment", "Error loading orders", error);
                return;
            }

            int totalOrders = 0;
            final double[] totalIncome = {0};
            final double[] totalProfit = {0};

            Map<String, Double> weeklySalesMap = new HashMap<>();

            Calendar cal = Calendar.getInstance();
            for (int i = 0; i < 7; i++) {
                String dayName = dayFormatter.format(cal.getTime());
                weeklySalesMap.put(dayName, 0.0);
                cal.add(Calendar.DAY_OF_YEAR, -1);
            }

            Calendar sevenDaysAgo = Calendar.getInstance();
            sevenDaysAgo.add(Calendar.DAY_OF_YEAR, -7);
            Date sevenDaysAgoDate = sevenDaysAgo.getTime();

            if (value != null) {
                totalOrders = value.size();

                final int[] processedOrdersCount = {0};
                final int totalOrdersToProcess = value.size();

                if (totalOrdersToProcess == 0) {
                    updateDashboardUI(0, 0, 0, weeklySalesMap);
                    return;
                }

                for (QueryDocumentSnapshot doc : value) {
                    Double amount = doc.getDouble("totalAmount");
                    if (amount != null) {
                        totalIncome[0] += amount;
                    }

                    Timestamp timestamp = doc.getTimestamp("timestamp");
                    if (timestamp != null && amount != null) {
                        Date orderDate = timestamp.toDate();
                        if (orderDate.after(sevenDaysAgoDate)) {
                            String orderDayName = dayFormatter.format(orderDate);
                            if (weeklySalesMap.containsKey(orderDayName)) {
                                double currentDayTotal = weeklySalesMap.get(orderDayName);
                                weeklySalesMap.put(orderDayName, currentDayTotal + amount);
                            }
                        }
                    }

                    List<Map<String, Object>> items = (List<Map<String, Object>>) doc.get("items");
                    if (items != null && !items.isEmpty()) {

                        final int[] processedItemsCount = {0};
                        final int totalItemsInOrder = items.size();

                        for (Map<String, Object> item : items) {
                            String productId = (String) item.get("productId");
                            Object qtyObj = item.get("quantity");
                            Object priceObj = item.get("price");

                            final int quantity = (qtyObj instanceof Long) ? ((Long) qtyObj).intValue() : 0;
                            final double sellingPrice = (priceObj instanceof Double) ? (Double) priceObj :
                                    (priceObj instanceof Long) ? ((Long) priceObj).doubleValue() : 0.0;

                            if (productId != null) {
                                db.collection("products").document(productId).get()
                                        .addOnSuccessListener(productDoc -> {
                                            if (productDoc.exists()) {
                                                Double buyingPrice = productDoc.getDouble(
                                                        "buyingPrice");
                                                if (buyingPrice != null) {
                                                    double itemProfit =
                                                            (sellingPrice - buyingPrice) * quantity;
                                                    totalProfit[0] += itemProfit;
                                                }
                                            }

                                            processedItemsCount[0]++;
                                            if (processedItemsCount[0] == totalItemsInOrder) {
                                                processedOrdersCount[0]++;
                                                if (processedOrdersCount[0] == totalOrdersToProcess) {
                                                    updateDashboardUI(totalOrdersToProcess,
                                                            totalIncome[0], totalProfit[0],
                                                            weeklySalesMap);
                                                }
                                            }
                                        }).addOnFailureListener(e -> {
                                            processedItemsCount[0]++;
                                            if (processedItemsCount[0]
                                                    == totalItemsInOrder) {
                                                processedOrdersCount[0]++;
                                                if (processedOrdersCount[0]
                                                        == totalOrdersToProcess) {
                                                    updateDashboardUI(totalOrdersToProcess,
                                                            totalIncome[0], totalProfit[0],
                                                            weeklySalesMap);
                                                }
                                            }
                                        });
                            } else {
                                processedItemsCount[0]++;
                                if (processedItemsCount[0] == totalItemsInOrder) {
                                    processedOrdersCount[0]++;
                                    if (processedOrdersCount[0] == totalOrdersToProcess) {
                                        updateDashboardUI(totalOrdersToProcess, totalIncome[0],
                                                totalProfit[0], weeklySalesMap);
                                    }
                                }
                            }
                        }
                    } else {
                        processedOrdersCount[0]++;
                        if (processedOrdersCount[0] == totalOrdersToProcess) {
                            updateDashboardUI(totalOrdersToProcess, totalIncome[0],
                                    totalProfit[0], weeklySalesMap);
                        }
                    }
                }
            }
        });
    }

    private void updateDashboardUI(int totalOrders, double totalIncome,
                                   double totalProfit, Map<String, Double> weeklySalesMap) {
        if (binding == null) return;

        String currentTime = "Updated: " + timeFormatter.format(new Date());

        binding.tvTotalOrder.setText(String.valueOf(totalOrders));
        binding.tvOrdersUpdatedTime.setText(currentTime);

        binding.tvTotalIncome.setText(currencyFormatter.format(totalIncome));
        binding.tvIncomeUpdatedTime.setText(currentTime);

        binding.tvTotalProfit.setText(currencyFormatter.format(totalProfit));
        binding.tvProfitUpdatedTime.setText(currentTime);

        updateChart(weeklySalesMap);
    }

    private void updateChart(Map<String, Double> weeklySalesMap) {
        if (binding == null) return;

        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();

        for (int i = 6; i >= 0; i--) {
            calendar.setTime(new Date());
            calendar.add(Calendar.DAY_OF_YEAR, -i);

            String dayName = dayFormatter.format(calendar.getTime());
            labels.add(dayName);

            Double dayTotal = weeklySalesMap.get(dayName);
            float salesAmount = (dayTotal != null) ? dayTotal.floatValue() : 0f;

            entries.add(new BarEntry(6 - i, salesAmount));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Weekly Sales");
        dataSet.setColor(Color.parseColor("#2196F3"));
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);

        binding.barChart.setData(barData);
        binding.barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));

        binding.barChart.animateY(1500);
        binding.barChart.invalidate();

        binding.ProgressBar.setVisibility(View.GONE);
    }

    private void showBottomNavigation() {
        if (getActivity() != null) {
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation_view);
            if (bottomNav != null) {
                bottomNav.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}