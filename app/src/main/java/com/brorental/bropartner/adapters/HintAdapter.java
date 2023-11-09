package com.brorental.bropartner.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.brorental.bropartner.MainActivity;
import com.brorental.bropartner.R;
import com.brorental.bropartner.fragments.SearchFragment;
import com.brorental.bropartner.interfaces.UtilsInterface;

import java.util.ArrayList;

public class HintAdapter extends RecyclerView.Adapter<HintAdapter.ViewHolder> {
    private Context ctx;
    private String TAG = "HintAdapter.java";
    private ArrayList<String> list;
    private MainActivity hostAct;
    public UtilsInterface.RefreshInterface refreshInterface;

    public HintAdapter(Context ctx, ArrayList<String> list) {
        hostAct = (MainActivity) ctx;
        this.ctx = ctx;
        this.list = list;
    }
    @NonNull
    @Override
    public HintAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.hint_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HintAdapter.ViewHolder holder, int position) {
        String str = list.get(position);
        holder.hintTV.setText(str);
        holder.hintTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshInterface.refresh(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public TextView hintTV;
        public ViewHolder(View view) {
            super(view);
            hintTV = view.findViewById(R.id.hintTV);
        }
    }

    public void addRefreshListener(UtilsInterface.RefreshInterface refreshInterface) {
        this.refreshInterface = refreshInterface;
    }
}
