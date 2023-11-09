package com.brorental.bropartner.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.brorental.bropartner.MainActivity;
import com.brorental.bropartner.R;
import com.brorental.bropartner.activities.RentDetailsActivity;
import com.brorental.bropartner.databinding.RentListItemBinding;
import com.brorental.bropartner.models.RentItemModel;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.util.ArrayList;

public class RentListAdapter extends ListAdapter<RentItemModel, RentListAdapter.ViewHolder> {
    private Context ctx;
    private MainActivity activity;
    private String TAG = "RentListAdapter.java";
    public RentListAdapter(Context ctx) {
        super(new DiffUtil.ItemCallback<RentItemModel>() {
            @Override
            public boolean areItemsTheSame(@NonNull RentItemModel oldItem, @NonNull RentItemModel newItem) {
                Log.d("RentListAdapter.java", "areItemsTheSame: " + oldItem.advertisementId.equals(newItem.advertisementId));
                return oldItem.advertisementId.equals(newItem.advertisementId);
            }

            @Override
            public boolean areContentsTheSame(@NonNull RentItemModel oldItem, @NonNull RentItemModel newItem) {
                Log.d("RentListAdapter.java", "areContentsTheSame: " + oldItem.equals(newItem));
                return oldItem.equals(newItem);
            }
        });
        this.ctx = ctx;
        this.activity = (MainActivity) ctx;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int whichType) {
        RentListItemBinding binding = RentListItemBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int pos) {
        RentItemModel data = getItem(pos);
        holder.binding.cateTV.setText(data.getCategory());
        holder.binding.addTV.setText(data.getAddress());
        String spanned = data.getPerHourCharge();
        holder.binding.perHourTV.setText("\u20B9 " + Html.fromHtml(spanned));
        holder.binding.timingsTV.setText(data.getTimings());

        Glide.with(ctx).load(data.getAdsImageUrl()).placeholder(R.drawable.american_flag)
                .into(holder.binding.pdImgView);

        holder.binding.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ctx, RentDetailsActivity.class);
                Gson gson = new Gson();
                String json = gson.toJson(data);
                i.putExtra("data", json);
                activity.startActivityForRes(i);
            }
        });
    }
    class ViewHolder extends RecyclerView.ViewHolder {
        public RentListItemBinding binding;
        public ViewHolder(RentListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
