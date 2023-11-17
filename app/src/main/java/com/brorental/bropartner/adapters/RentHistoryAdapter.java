package com.brorental.bropartner.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.brorental.bropartner.R;
import com.brorental.bropartner.databinding.PaymentHistoryListItemBinding;
import com.brorental.bropartner.databinding.RentHistoryListItemBinding;
import com.brorental.bropartner.models.HistoryModel;
import com.brorental.bropartner.models.PaymentHistoryModel;
import com.bumptech.glide.Glide;

public class RentHistoryAdapter extends ListAdapter<HistoryModel, RentHistoryAdapter.ViewHolder> {
    private String TAG = "PaymentAdapter.java";
    private Context context;

    public RentHistoryAdapter(Context ctx) {
        super(new DiffUtil.ItemCallback<HistoryModel>() {
            @Override
            public boolean areItemsTheSame(@NonNull HistoryModel oldItem, @NonNull HistoryModel newItem) {
                return oldItem.id.matches(newItem.id);
            }

            @Override
            public boolean areContentsTheSame(@NonNull HistoryModel oldItem, @NonNull HistoryModel newItem) {
                return oldItem.equals(newItem);
            }
        });
        this.context = ctx;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.rent_history_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoryModel data = getItem(position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private RentHistoryListItemBinding binding;
        public ViewHolder(RentHistoryListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
