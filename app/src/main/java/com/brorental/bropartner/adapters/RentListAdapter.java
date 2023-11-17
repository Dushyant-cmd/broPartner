package com.brorental.bropartner.adapters;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.brorental.bropartner.R;
import com.brorental.bropartner.activities.RentActivity;
import com.brorental.bropartner.databinding.RentListItemBinding;
import com.brorental.bropartner.interfaces.UtilsInterface;
import com.brorental.bropartner.models.RentItemModel;
import com.bumptech.glide.Glide;

public class RentListAdapter extends ListAdapter<RentItemModel, RentListAdapter.ViewHolder> {
    private Context ctx;
    private RentActivity activity;
    private String TAG = "RentListAdapter.java";
    private UtilsInterface.RentRefreshListener refreshInterface;
    public void addRentRefreshListener(UtilsInterface.RentRefreshListener refreshInterface) {
        this.refreshInterface = refreshInterface;
    }
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
        this.activity = (RentActivity) ctx;
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
        holder.binding.pdStatusTv.setText(data.getStatus());

        Log.d(TAG, "onBindViewHolder: " + data.getAdsImageUrl());
        String thumbnail = data.getAdsImageUrl().split(",")[0];
        Glide.with(ctx).load(thumbnail).placeholder(com.denzcoskun.imageslider.R.drawable.placeholder)
                .into(holder.binding.pdImgView);

        if(data.getLiveStatus()) {
            holder.binding.liveSc.setChecked(true);
        } else
            holder.binding.liveSc.setChecked(false);

        holder.binding.liveSc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                refreshInterface.updateLiveStatus(b, data.getDocId(), pos);
                Log.d(TAG, "onCheckedChanged: " + b);
            }
        });
//        holder.binding.root.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent i = new Intent(ctx, RentDetailsActivity.class);
//                Gson gson = new Gson();
//                String json = gson.toJson(data);
//                i.putExtra("data", json);
//                activity.startActivityForRes(i);
//            }
//        });
    }
    class ViewHolder extends RecyclerView.ViewHolder {
        public RentListItemBinding binding;
        public ViewHolder(RentListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
