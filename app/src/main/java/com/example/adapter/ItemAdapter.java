package com.example.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.model.ItemModel;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(ItemModel item);
    }

    private final Context context;
    private final List<ItemModel> items = new ArrayList<>();
    private final OnItemClickListener listener;

    public ItemAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void updateData(List<ItemModel> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_card_fluent, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        ItemModel item = items.get(position);

        holder.tvItemTitle.setText(item.getTitle());
        holder.tvItemCategoryPill.setText(item.getCategory());
        holder.tvItemDate.setText(item.getDate());
        holder.tvItemLocation.setText(item.getLocation());
        holder.tvItemHolding.setText(item.getHoldingLocation() != null ? item.getHoldingLocation() : "Secretaria");

        // Thumbnail image
        if (item.getPhotoPath() != null && !item.getPhotoPath().isEmpty() && new java.io.File(item.getPhotoPath()).exists()) {
            android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeFile(item.getPhotoPath());
            if (bitmap != null) {
                holder.ivItemThumb.setImageBitmap(bitmap);
                holder.ivItemThumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
                holder.ivItemThumb.setPadding(0, 0, 0, 0);
            } else {
                holder.ivItemThumb.setImageResource(getCategoryFallbackIcon(item.getCategory()));
            }
        } else if (item.getImageRes() != 0) {
            holder.ivItemThumb.setImageResource(item.getImageRes());
            holder.ivItemThumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
            holder.ivItemThumb.setPadding(0, 0, 0, 0);
        } else {
            holder.ivItemThumb.setImageResource(getCategoryFallbackIcon(item.getCategory()));
            int pad = (int) (14 * context.getResources().getDisplayMetrics().density);
            holder.ivItemThumb.setPadding(pad, pad, pad, pad);
        }

        // Status presentation
        setupStatus(holder, item.getStatus(), item.getType());

        holder.cardItem.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    private void setupStatus(ItemViewHolder holder, String status, String type) {
        if ("DELIVERED".equalsIgnoreCase(status) || "RESOLVED".equalsIgnoreCase(status)) {
            holder.tvItemStatus.setText("Devolvido");
            holder.tvItemStatus.setTextColor(ContextCompat.getColor(context, R.color.fluent_status_delivered_text));
            holder.ivItemStatusIcon.setImageResource(R.drawable.ic_check);
            holder.ivItemStatusIcon.setColorFilter(ContextCompat.getColor(context, R.color.fluent_status_delivered_text));
        } else if ("WAITING_PICKUP".equalsIgnoreCase(status)) {
            holder.tvItemStatus.setText("Aguardando o dono");
            holder.tvItemStatus.setTextColor(ContextCompat.getColor(context, R.color.fluent_blue_primary));
            holder.ivItemStatusIcon.setImageResource(R.drawable.ic_clock);
            holder.ivItemStatusIcon.setColorFilter(ContextCompat.getColor(context, R.color.fluent_blue_primary));
        } else if ("LOST".equalsIgnoreCase(type) || "SEARCHING".equalsIgnoreCase(status)) {
            holder.tvItemStatus.setText("Procura-se");
            holder.tvItemStatus.setTextColor(ContextCompat.getColor(context, R.color.fluent_status_lost_text));
            holder.ivItemStatusIcon.setImageResource(R.drawable.ic_clock);
            holder.ivItemStatusIcon.setColorFilter(ContextCompat.getColor(context, R.color.fluent_status_lost_text));
        } else {
            holder.tvItemStatus.setText("Aguardando o dono");
            holder.tvItemStatus.setTextColor(ContextCompat.getColor(context, R.color.fluent_blue_primary));
            holder.ivItemStatusIcon.setImageResource(R.drawable.ic_clock);
            holder.ivItemStatusIcon.setColorFilter(ContextCompat.getColor(context, R.color.fluent_blue_primary));
        }
    }

    private int getCategoryFallbackIcon(String category) {
        if (category == null) return R.drawable.ic_category_other;
        if (category.contains("Documento")) return R.drawable.ic_category_document;
        if (category.contains("Eletrônic")) return R.drawable.ic_category_electronic;
        if (category.contains("Chave")) return R.drawable.ic_category_key;
        if (category.contains("Material") || category.contains("Livro")) return R.drawable.ic_category_school;
        if (category.contains("Garrafa") || category.contains("Copo")) return R.drawable.ic_category_bottle;
        if (category.contains("Roupa") || category.contains("Agasalho")) return R.drawable.ic_category_clothing;
        return R.drawable.ic_category_other;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardItem;
        ImageView ivItemThumb;
        TextView tvItemTitle;
        TextView tvItemCategoryPill;
        TextView tvItemDate;
        TextView tvItemLocation;
        TextView tvItemHolding;
        ImageView ivItemStatusIcon;
        TextView tvItemStatus;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            cardItem = itemView.findViewById(R.id.cardItem);
            ivItemThumb = itemView.findViewById(R.id.ivItemThumb);
            tvItemTitle = itemView.findViewById(R.id.tvItemTitle);
            tvItemCategoryPill = itemView.findViewById(R.id.tvItemCategoryPill);
            tvItemDate = itemView.findViewById(R.id.tvItemDate);
            tvItemLocation = itemView.findViewById(R.id.tvItemLocation);
            tvItemHolding = itemView.findViewById(R.id.tvItemHolding);
            ivItemStatusIcon = itemView.findViewById(R.id.ivItemStatusIcon);
            tvItemStatus = itemView.findViewById(R.id.tvItemStatus);
        }
    }
}
