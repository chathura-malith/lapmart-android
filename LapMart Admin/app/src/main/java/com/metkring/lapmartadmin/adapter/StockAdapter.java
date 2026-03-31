package com.metkring.lapmartadmin.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.metkring.lapmartadmin.R;
import com.metkring.lapmartadmin.model.Product; // ඔයාගේ Product model එකේ path එක හරියටම දෙන්න

import java.util.List;

public class StockAdapter extends RecyclerView.Adapter<StockAdapter.StockViewHolder> {

    private Context context;
    private List<Product> productList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onEditClick(Product product);
    }

    public StockAdapter(Context context, List<Product> productList, OnItemClickListener listener) {
        this.context = context;
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public StockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent,
                false);
        return new StockViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StockViewHolder holder, int position) {
        Product product = productList.get(position);

        String fullName = product.getBrand() + " " + product.getModel() + "–" +
                product.getProcessor();
        holder.tvProductName.setText(fullName);

        holder.tvProductPrice.setText("Rs " + String.format("%.2f", product.getPrice()));

        if (product.getQty() > 0) {
            holder.tvProductStock.setText("Stock: " + product.getQty());
        } else {
            holder.tvProductStock.setText("Stock: " + product.getQty());
            holder.tvProductStock.setTextColor(context.getResources()
                    .getColor(android.R.color.holo_red_dark));
        }

        if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
            Glide.with(context)
                    .load(product.getImageUrls().get(0))
                    .centerCrop()
                    .into(holder.ivProductImage);
        }

        holder.btnEdit.setOnClickListener(v -> listener.onEditClick(product));
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class StockViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage, btnEdit;
        TextView tvProductName, tvProductPrice, tvProductStock;

        public StockViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvProductStock = itemView.findViewById(R.id.tvProductStock);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }
    }
}