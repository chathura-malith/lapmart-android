package com.metkring.lapmart.adapter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.metkring.lapmart.R;
import com.metkring.lapmart.fragment.ProductDetailFragment;
import com.metkring.lapmart.model.Product;

import java.util.List;

public class SimilarProductAdapter extends RecyclerView.Adapter<SimilarProductAdapter.ViewHolder> {

    private List<Product> productList;

    public SimilarProductAdapter(List<Product> productList) {
        this.productList = productList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_similar_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);

        String fullName = product.getBrand() + " " + product.getModel() + "–" + product.getProcessor();
        holder.name.setText(fullName);
        holder.price.setText("Rs." + String.format("%,.2f", product.getPrice()));

        // Image එක load කිරීම
        if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(product.getImageUrls().get(0))
                    .placeholder(R.drawable.msi_laptop)
                    .into(holder.image);
        }

        holder.itemView.setOnClickListener(v -> {
            ProductDetailFragment fragment = new ProductDetailFragment();
            Bundle bundle = new Bundle();
            bundle.putString("product_id", product.getId());
            fragment.setArguments(bundle);

            AppCompatActivity activity = (AppCompatActivity) v.getContext();
            activity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, price;
        ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.similarProductName);
            price = itemView.findViewById(R.id.similarProductPrice);
            image = itemView.findViewById(R.id.similarProductImage);
        }
    }
}