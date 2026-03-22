package com.metkring.lapmart.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.metkring.lapmart.R;
import com.metkring.lapmart.databinding.ItemOrderBinding;
import com.metkring.lapmart.model.Order;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<Order> orderList;
    private Context context;

    public OrderAdapter(List<Order> orderList, Context context) {
        this.orderList = orderList;
        this.context = context;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOrderBinding binding = ItemOrderBinding.inflate(LayoutInflater.from(context), parent, false);
        return new OrderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        private ItemOrderBinding binding;

        public OrderViewHolder(ItemOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Order order) {
            binding.tvOrderId.setText("#" + order.getOrderId());
            binding.tvOrderTotal.setText("Rs. " + String.format("%,.2f", order.getTotalAmount()));
            binding.tvOrderStatus.setText(order.getStatus());

            // 📅 Date Format කිරීම
            if (order.getTimestamp() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy - hh:mm b", Locale.getDefault());
                binding.tvOrderDate.setText(sdf.format(order.getTimestamp().toDate()));
            }

            // 📦 Item Count එක
            int count = order.getItems() != null ? order.getItems().size() : 0;
            binding.tvItemCount.setText(count + (count > 1 ? " Items Ordered" : " Item Ordered"));

            // 🎨 Status එක අනුව පාට වෙනස් කිරීම
            if (order.getStatus().equalsIgnoreCase("Pending")) {
                binding.tvOrderStatus.setBackgroundResource(R.drawable.status_bg_pending);
                binding.tvOrderStatus.setTextColor(Color.parseColor("#E65100"));
            } else {
                binding.tvOrderStatus.setBackgroundResource(R.drawable.status_bg_success);
                binding.tvOrderStatus.setTextColor(Color.parseColor("#2E7D32"));
            }

            // 🖼️ පළවෙනි අයිතමයේ Image එක පෙන්වීම
//            if (order.getItems() != null && !order.getItems().isEmpty()) {
//                Map<String, Object> firstItem = order.getItems().get(0);
//                String imageUrl = (String) firstItem.get("productImage"); // 👈 Firestore එකේ සේව් කරපු key එක බලන්න (image/imageUrl)
//
//                Glide.with(context)
//                        .load(imageUrl)// Load වෙනකන් පේන image එක
//                        .into(binding.ivOrderProduct);
//            }

            // 🖼️ පළවෙනි අයිතමයේ Image එක පෙන්වීම (පරණ කේතය මකලා මේක දාන්න)
            if (order.getItems() != null && !order.getItems().isEmpty()) {
                // අපේ අලුත් Adapter එක ViewPager එකට සෙට් කරනවා
                OrderImageAdapter imageAdapter = new OrderImageAdapter(order.getItems(), context);
                binding.vpOrderImages.setAdapter(imageAdapter);

                // Dots ටික ViewPager එකත් එක්ක ලින්ක් කරනවා
                binding.dotsIndicator.setViewPager2(binding.vpOrderImages);

                // පින්තූර 1ක් විතරක් තියෙනවා නම් Dots පෙන්නන්න ඕනේ නෑනේ
                if (order.getItems().size() <= 1) {
                    binding.dotsIndicator.setVisibility(android.view.View.GONE);
                } else {
                    binding.dotsIndicator.setVisibility(android.view.View.VISIBLE);
                }
            }
        }
    }
}