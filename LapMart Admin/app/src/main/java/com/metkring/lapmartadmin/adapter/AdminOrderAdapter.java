package com.metkring.lapmartadmin.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.metkring.lapmartadmin.R;
import com.metkring.lapmartadmin.model.Order;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.OrderViewHolder> {

    private final List<Order> orderList;
    private final Context context;
    private final OnStatusClickListener statusClickListener;
    private final SimpleDateFormat dateFormat;
    private final NumberFormat currencyFormat;

    public interface OnStatusClickListener {
        void onStatusClick(View anchorView, Order order);
    }

    public AdminOrderAdapter(
            List<Order> orderList, Context context, OnStatusClickListener statusClickListener
    ) {
        this.orderList = orderList;
        this.context = context;
        this.statusClickListener = statusClickListener;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy - hh:mm a",
                Locale.getDefault());
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en",
                "LK"));
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_admin, parent,
                false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.tvOrderId.setText("#" + order.getOrderId());

        if (order.getBillingAddress() != null) {
            holder.tvCustomerName.setText(order.getBillingAddress().getFullName());
        } else {
            holder.tvCustomerName.setText("Unknown Customer");
        }

        if (order.getTimestamp() != null) {
            Date date = order.getTimestamp().toDate();
            holder.tvOrderDate.setText(dateFormat.format(date));
        }

        holder.tvOrderTotal.setText(currencyFormat.format(order.getTotalAmount()));

        List<Map<String, Object>> items = order.getItems();
        int itemCount = items != null ? items.size() : 0;
        holder.tvItemCount.setText(itemCount + (itemCount == 1 ? " Item Ordered" :
                " Items Ordered"));

        String status = order.getStatus() != null ? order.getStatus() : "Pending";
        holder.tvOrderStatus.setText(status + " ▼");

        switch (status.toLowerCase()) {
            case "processing":
                holder.tvOrderStatus.setBackgroundResource(R.drawable.badge_bg_processing);
                holder.tvOrderStatus.setTextColor(Color.parseColor("#1976D2"));
                break;
            case "shipped":
                holder.tvOrderStatus.setBackgroundResource(R.drawable.badge_bg_shipped);
                holder.tvOrderStatus.setTextColor(Color.parseColor("#7B1FA2"));
                break;
            case "delivered":
                holder.tvOrderStatus.setBackgroundResource(R.drawable.badge_bg_delivered);
                holder.tvOrderStatus.setTextColor(Color.parseColor("#388E3C"));
                break;
            default: // Pending
                holder.tvOrderStatus.setBackgroundResource(R.drawable.badge_bg_pending);
                holder.tvOrderStatus.setTextColor(Color.parseColor("#E65100"));
                break;
        }

        holder.tvOrderStatus.setOnClickListener(v -> {
            if (statusClickListener != null) {
                statusClickListener.onStatusClick(v, order);
            }
        });

        List<Map<String, Object>> imageItemsList = new ArrayList<>();
        if (items != null) {
            for (Map<String, Object> item : items) {
                if (item.containsKey("productImage")) {
                    imageItemsList.add(item);
                }
            }
        }

        if (!imageItemsList.isEmpty()) {
            OrderImageAdapter imageAdapter = new OrderImageAdapter(imageItemsList, context);
            holder.vpOrderImages.setAdapter(imageAdapter);

            if (imageItemsList.size() > 1) {
                holder.dotsIndicator.setVisibility(View.VISIBLE);
                holder.dotsIndicator.attachTo(holder.vpOrderImages);
            } else {
                holder.dotsIndicator.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvCustomerName, tvOrderDate, tvOrderStatus, tvOrderTotal, tvItemCount;
        ViewPager2 vpOrderImages;
        DotsIndicator dotsIndicator;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvOrderTotal = itemView.findViewById(R.id.tvOrderTotal);
            tvItemCount = itemView.findViewById(R.id.tvItemCount);
            vpOrderImages = itemView.findViewById(R.id.vpOrderImages);
            dotsIndicator = itemView.findViewById(R.id.dotsIndicator);
        }
    }
}