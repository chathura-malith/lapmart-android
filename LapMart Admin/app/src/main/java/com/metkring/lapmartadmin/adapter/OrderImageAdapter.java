package com.metkring.lapmartadmin.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.metkring.lapmartadmin.R;

import java.util.List;
import java.util.Map;

public class OrderImageAdapter extends RecyclerView.Adapter<OrderImageAdapter.ImageViewHolder> {

    private List<Map<String, Object>> itemsList;
    private Context context;

    public OrderImageAdapter(List<Map<String, Object>> itemsList, Context context) {
        this.itemsList = itemsList;
        this.context = context;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_image, parent,
                false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Map<String, Object> item = itemsList.get(position);

        String imageUrl = (String) item.get("productImage");

        Glide.with(context)
                .load(imageUrl)
                .into(holder.ivProduct);
    }

    @Override
    public int getItemCount() {
        return itemsList.size();
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.ivProduct);
        }
    }
}