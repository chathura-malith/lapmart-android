package com.metkring.lapmart.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.metkring.lapmart.R;

import java.util.List;

public class BrandAdapter extends RecyclerView.Adapter<BrandAdapter.ViewHolder>{

    private List<String> brands;
    private int selectedPosition = 0;

    public BrandAdapter(List<String> brandList) {
        this.brands = brandList;
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
        String brand = brands.get(position);
        holder.brandName.setText(brand);

        // සිලෙක්ට් වී ඇත්නම් Indicator එක පෙන්වන්න, නැත්නම් හංගන්න
        if (selectedPosition == position) {
            holder.brandIndicator.setVisibility(View.VISIBLE);
            holder.brandName.setSelected(true); // අකුරු වල පාට වෙනස් වීමට
        } else {
            holder.brandIndicator.setVisibility(View.INVISIBLE);
            holder.brandName.setSelected(false);
        }

        holder.itemView.setOnClickListener(v -> {
            int previous = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(previous);
            notifyItemChanged(selectedPosition);
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
}
