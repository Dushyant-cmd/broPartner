package com.brorental.bropartner.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.brorental.bropartner.R;
import com.brorental.bropartner.databinding.RentHistoryListItemBinding;
import com.brorental.bropartner.interfaces.UtilsInterface;
import com.brorental.bropartner.models.HistoryModel;
import com.brorental.bropartner.utilities.Utility;

public class RentHistoryAdapter extends ListAdapter<HistoryModel, RentHistoryAdapter.ViewHolder> {
    private String TAG = "PaymentAdapter.java";
    private Context context;
    private UtilsInterface.RentStatusListener rentStatusListener;

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
        String[] imageArr = data.rentImages.split(",");
//        List<SlideModel> list = new ArrayList<>();
//        for(String url: imageArr) {
//            list.add(new SlideModel(url, ScaleTypes.FIT));
//        }
//        Glide.with(context).load(imageArr[0]).placeholder(com.denzcoskun.imageslider.R.drawable.placeholder).into(holder.binding.imageSlider);

        if(data.status.equalsIgnoreCase("pending")) {
            holder.binding.completeBtn.setVisibility(View.GONE);
            holder.binding.pendingLy.setVisibility(View.VISIBLE);
            holder.binding.callBtn.setVisibility(View.GONE);
        } else if(data.status.equalsIgnoreCase("ongoing")) {
            holder.binding.completeBtn.setVisibility(View.VISIBLE);
            holder.binding.pendingLy.setVisibility(View.GONE);
            holder.binding.callBtn.setVisibility(View.GONE);
        } else if(data.status.equalsIgnoreCase("completed")) {
            holder.binding.completeBtn.setVisibility(View.GONE);
            holder.binding.pendingLy.setVisibility(View.GONE);
            holder.binding.callBtn.setVisibility(View.GONE);
        } else {
            holder.binding.completeBtn.setVisibility(View.GONE);
            holder.binding.pendingLy.setVisibility(View.GONE);
            holder.binding.callBtn.setVisibility(View.VISIBLE);
        }
        
        holder.binding.nameTv.setText(data.name);
        holder.binding.advertIdTv.setText(data.advertisementId);
        holder.binding.ttlChgTv.setText("Total cost: " + Utility.rupeeIcon + data.totalRentCost + "(as per " + Utility.rupeeIcon + data.perHourCharge + " /hour)");
        holder.binding.extChgTv.setText("Extra charge: " + Utility.rupeeIcon + data.extraCharge);
        holder.binding.payStaModeTv.setText("Payment completed " + data.paymentMode);
        holder.binding.dateTimeTv.setText("From " + data.rentStartTime + " To " + data.rentEndTime);
        holder.binding.totalHourTv.setText("Total hour: " + data.totalHours);

        holder.binding.rejectBtn.setOnClickListener(view -> {
            rentStatusListener.updateStatus("reject", data);
        });

        holder.binding.acceptBtn.setOnClickListener(view -> {
            rentStatusListener.updateStatus("ongoing", data);
        });

        holder.binding.completeBtn.setOnClickListener(view -> {
            rentStatusListener.updateStatus("completed", data);
        });

        holder.binding.callBtn.setOnClickListener(view -> {
            rentStatusListener.contactListener("dial");
        });
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private RentHistoryListItemBinding binding;
        public ViewHolder(RentHistoryListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public void setRentStatusListener(UtilsInterface.RentStatusListener statusListener) {
        this.rentStatusListener = statusListener;
    }
}
