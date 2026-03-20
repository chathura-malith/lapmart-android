package com.metkring.lapmart.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.metkring.lapmart.R;
import com.metkring.lapmart.fragment.ProductDetailFragment;
import com.metkring.lapmart.helper.CartManager;
import com.metkring.lapmart.model.CartItem;
import com.metkring.lapmart.model.Product;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
    private List<Product> productList;

    public ProductAdapter(List<Product> productList) {
        this.productList = productList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.name.setText(product.getModel());
        holder.price.setText("Rs." + String.format("%.2f", product.getPrice()));
        if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(product.getImageUrls().get(0))
                    .placeholder(R.drawable.msi_laptop)
                    .error(R.drawable.msi_laptop)
                    .into(holder.image);
        }

        holder.image.setOnClickListener(v -> {
            ProductDetailFragment fragment = new ProductDetailFragment();

            android.os.Bundle bundle = new android.os.Bundle();
            bundle.putString("product_id", product.getId());
            fragment.setArguments(bundle);

            androidx.appcompat.app.AppCompatActivity activity =
                    (androidx.appcompat.app.AppCompatActivity) v.getContext();
            activity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        holder.addToCart.setOnClickListener(v -> {
            CartItem cartItem = new CartItem(
                    product.getId(),
                    product.getModel(),
                    product.getImageUrls().get(0),
                    product.getPrice(),
                    1
            );
            new CartManager(v.getContext()).addItem(cartItem, v.getContext());
        });
    }

    @Override
    public int getItemCount() { return productList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, price;
        ImageView image,addToCart;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.productName);
            price = itemView.findViewById(R.id.productPrice);
            image = itemView.findViewById(R.id.productImage);
            addToCart = itemView.findViewById(R.id.addToCartBtn);
        }
    }
}
