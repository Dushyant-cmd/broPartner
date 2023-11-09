package com.brorental.bropartner.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.brorental.bropartner.R;
import com.brorental.bropartner.activities.HistoryActivity;
import com.brorental.bropartner.adapters.RentHistoryAdapter;
import com.brorental.bropartner.databinding.FragmentRentHistoryBinding;
import com.brorental.bropartner.models.HistoryModel;
import com.brorental.bropartner.utilities.AppClass;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class RentHistoryFragment extends Fragment {
    private FragmentRentHistoryBinding binding;
    private AppClass appClass;
    private String TAG = "RentHistoryFrag.java";
    private ArrayList<HistoryModel> list = new ArrayList<>();
    private RentHistoryAdapter adapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_rent_history, container, false);
        appClass = (AppClass) requireActivity().getApplication();
        adapter = new RentHistoryAdapter(requireActivity());
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));

        binding.swipeRef.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getRentItems();
            }
        });
        getRentItems();
        return binding.getRoot();
    }

    private void getRentItems() {
        binding.shimmer.setVisibility(View.VISIBLE);
        binding.recyclerView.setVisibility(View.GONE);
        appClass.firestore.collection("rentHistory").whereEqualTo("broRentalId", appClass.sharedPref.getUser().getPin())
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        binding.swipeRef.setRefreshing(false);
                        binding.shimmer.setVisibility(View.GONE);
                        binding.recyclerView.setVisibility(View.VISIBLE);
                        if(task.isSuccessful()) {
                            list.clear();
                            for(DocumentSnapshot d: task.getResult().getDocuments()) {
                                HistoryModel model = d.toObject(HistoryModel.class);
                                list.add(model);
                            }
                            Log.d(TAG, "onComplete: " + list);
                            adapter.submitList(list);
                        } else {
                            Log.d(TAG, "onComplete: " + task.getException());
                        }
                    }
                });
    }
}