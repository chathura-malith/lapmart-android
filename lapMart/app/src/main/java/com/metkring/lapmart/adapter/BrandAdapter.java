package com.metkring.lapmart.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.metkring.lapmart.R;
import com.metkring.lapmart.model.Brand;

import java.util.List;

public class BrandAdapter extends RecyclerView.Adapter<BrandAdapter.ViewHolder>{

    private List<Brand> brands;
    private int selectedPosition = 0;

    private OnBrandClickListener listener;

    public BrandAdapter(List<Brand> brandList, OnBrandClickListener listener) {
        this.brands = brandList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_brand, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Brand brand = brands.get(position);
        holder.brandName.setText(brand.getName());


        if (selectedPosition == position) {
            holder.brandIndicator.setVisibility(View.VISIBLE);
            holder.brandName.setSelected(true);
        } else {
            holder.brandIndicator.setVisibility(View.INVISIBLE);
            holder.brandName.setSelected(false);
        }

        holder.itemView.setOnClickListener(v -> {
            int previous = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(previous);
            notifyItemChanged(selectedPosition);

            listener.onBrandClick(brands.get(selectedPosition).getName());
        });
    }

    @Override
    public int getItemCount() {
        return brands.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView brandName;
        View brandIndicator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            brandName = itemView.findViewById(R.id.brandName);
            brandIndicator = itemView.findViewById(R.id.brandIndicator);
        }
    }

    public interface OnBrandClickListener {
        void onBrandClick(String brandName);
    }
}
