package com.brorental.bropartner.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.brorental.bropartner.R;
import com.brorental.bropartner.databinding.PointsListItemBinding;
import com.brorental.bropartner.interfaces.UtilsInterface;
import com.brorental.bropartner.models.PointsHistoryModel;

public class PointsAdapter extends ListAdapter<PointsHistoryModel, PointsAdapter.ViewHolder> {
    private Context ctx;
    private UtilsInterface.RentRefreshListener refreshInterface;
    public PointsAdapter(Context ctx) {
        super(new DiffUtil.ItemCallback<PointsHistoryModel>() {
            @Override
            public boolean areItemsTheSame(@NonNull PointsHistoryModel oldItem, @NonNull PointsHistoryModel newItem) {
                return oldItem.getTimestamp() == newItem.getTimestamp();
            }

            @Override
            public boolean areContentsTheSame(@NonNull PointsHistoryModel oldItem, @NonNull PointsHistoryModel newItem) {
                return oldItem.equals(newItem);
            }
        });
        this.ctx = ctx;
    }

    @Override
    public PointsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int whichType) {
        PointsListItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.points_list_item, parent, false);
        return new PointsAdapter.ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int pos) {
        PointsHistoryModel data = getItem(pos);
        PointsListItemBinding binding = holder.binding;
        binding.fromET.setText(data.getFrom());
        binding.toET.setText(data.getTo());
        binding.tvTotalAmt.setText("\u20b9 " + data.getAmount());
        binding.tvDistance.setText(data.getDistance() + " /km");
        if(data.isStatus()) {
            binding.statusSwitch.setChecked(data.isStatus());
        } else
            binding.statusSwitch.setChecked(data.isStatus());

        binding.statusSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    refreshInterface.updateLiveStatus(true, data.getDocId(), pos);
                } else {
                    refreshInterface.updateLiveStatus(false, data.getDocId(), pos);
                }
            }
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        PointsListItemBinding binding;
        public ViewHolder(PointsListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
    public void addRefreshListener(UtilsInterface.RentRefreshListener refreshInterface) {
        this.refreshInterface = refreshInterface;
    }
}
