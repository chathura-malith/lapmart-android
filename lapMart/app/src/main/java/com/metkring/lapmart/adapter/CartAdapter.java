package com.metkring.lapmart.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.metkring.lapmart.R;
import com.metkring.lapmart.databinding.ItemCartBinding;
import com.metkring.lapmart.model.CartItem;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItems;
    private OnCartActionListener listener;

    public interface OnCartActionListener {
        void onQuantityChanged(int position, int newQuantity);
        void onRemoveItem(int position);
    }

    public CartAdapter(List<CartItem> cartItems, OnCartActionListener listener) {
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCartBinding binding = ItemCartBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CartViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.bind(item, position);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    class CartViewHolder extends RecyclerView.ViewHolder {
        private ItemCartBinding binding;

        public CartViewHolder(ItemCartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(CartItem item, int position) {;
            binding.tvProductName.setText(item.getProductName());
            binding.tvProductPrice.setText("Rs. " + String.format("%,.2f", item.getPrice()));
            binding.tvQuantity.setText(String.valueOf(item.getQuantity()));

            // Image Loading using Glide
            Glide.with(itemView.getContext())
                    .load(item.getProductImage())
                    .placeholder(R.drawable.msi_laptop)
                    .into(binding.ivProductImage);

            // Increase Quantity Button
            binding.btnIncreaseQuantity.setOnClickListener(v -> {
                int newQty = item.getQuantity() + 1;
                listener.onQuantityChanged(position, newQty);
            });

            // Decrease Quantity Button
            binding.btnDecreaseQuantity.setOnClickListener(v -> {
                if (item.getQuantity() > 1) {
                    int newQty = item.getQuantity() - 1;
                    listener.onQuantityChanged(position, newQty);
                }
            });

            // Remove Button
            binding.btnRemove.setOnClickListener(v -> {
                listener.onRemoveItem(position);
            });
        }
    }
}