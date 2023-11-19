package com.brorental.bropartner.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.brorental.bropartner.R;
import com.brorental.bropartner.adapters.RentHistoryAdapter;
import com.brorental.bropartner.databinding.FragmentRentHistoryBinding;
import com.brorental.bropartner.interfaces.UtilsInterface;
import com.brorental.bropartner.models.HistoryModel;
import com.brorental.bropartner.utilities.AppClass;
import com.brorental.bropartner.utilities.DialogCustoms;
import com.brorental.bropartner.utilities.ProgressDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class RentHistoryFragment extends Fragment {
    private FragmentRentHistoryBinding binding;
    private AppClass appClass;
    private String TAG = "RentHistoryFrag.java";
    private ArrayList<HistoryModel> list = new ArrayList<>();
    private RentHistoryAdapter adapter;
    private AlertDialog pDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_rent_history, container, false);
        appClass = (AppClass) requireActivity().getApplication();
        adapter = new RentHistoryAdapter(requireActivity());
        binding.recyclerView.setAdapter(adapter);
        pDialog = ProgressDialog.createAlertDialog(requireContext());
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
        appClass.firestore.collection("rentHistory")
                .whereEqualTo("broPartnerId", appClass.sharedPref.getUser().getPin())
                .get(Source.SERVER).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        binding.swipeRef.setRefreshing(false);
                        binding.shimmer.setVisibility(View.GONE);
                        binding.recyclerView.setVisibility(View.VISIBLE);
                        if (task.isSuccessful()) {
                            list.clear();
                            for (DocumentSnapshot d : task.getResult().getDocuments()) {
                                HistoryModel model = d.toObject(HistoryModel.class);
                                list.add(model);
                            }
                            Log.d(TAG, "onComplete: " + list);
                            adapter.submitList(list);
                            adapter.setRentStatusListener(new UtilsInterface.RentStatusListener() {
                                @Override
                                public void updateStatus(String status, HistoryModel data) {
                                    pDialog.show();
                                    HashMap<String, Object> map = new HashMap<>();
                                    map.put("status", status);
                                    appClass.firestore.collection("rentHistory")
                                            .document(data.id)
                                            .update(map)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Calendar cal = Calendar.getInstance();
                                                        Date date = cal.getTime();
                                                        SimpleDateFormat spf = new SimpleDateFormat("dd-MM-yyyy, hh:mm:ss a", Locale.getDefault());
                                                        String dateAndTime = spf.format(date);
                                                        if (status.equalsIgnoreCase("reject")) {
                                                            appClass.firestore.collection("users")
                                                                    .document(data.broRentalId)
                                                                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                            if (task.isSuccessful()) {
                                                                                String currentWalAmt = task.getResult().getString("wallet");
                                                                                String newWalAmt = currentWalAmt + data.totalRentCost;
                                                                                HashMap<String, Object> updateMap = new HashMap<>();
                                                                                updateMap.put("wallet", newWalAmt);
                                                                                appClass.firestore.collection("users")
                                                                                        .document(data.broRentalId)
                                                                                        .update(updateMap)
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if (task.isSuccessful()) {
                                                                                                    HashMap<String, Object> map = new HashMap<>();
                                                                                                    map.put("amount", newWalAmt);
                                                                                                    map.put("date", dateAndTime);
                                                                                                    map.put("info", null);
                                                                                                    map.put("name", appClass.sharedPref.getUser().getName());
                                                                                                    map.put("status", "completed");
                                                                                                    map.put("type", "rentRefund");
                                                                                                    map.put("advertisementId", data.advertisementId);
                                                                                                    map.put("timestamp", System.currentTimeMillis());
                                                                                                    map.put("isBroRental", false);
                                                                                                    appClass.firestore.collection("transactions").add(map)
                                                                                                            .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                                                                                @Override
                                                                                                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                                                                                                    if(task.isSuccessful()) {
                                                                                                                        pDialog.dismiss();
                                                                                                                        getRentItems();
                                                                                                                        Log.d(TAG, "onComplete: success wallet added");
                                                                                                                    } else {
                                                                                                                        pDialog.dismiss();
                                                                                                                        DialogCustoms.showSnackBar(requireContext(), task.getException().getMessage(), binding.getRoot());
                                                                                                                    }
                                                                                                                }
                                                                                                            });
                                                                                                    Log.d(TAG, "onComplete: update wallet success");
                                                                                                } else {
                                                                                                    pDialog.dismiss();
                                                                                                    DialogCustoms.showSnackBar(requireContext(), task.getException().getMessage(), binding.getRoot());
                                                                                                }
                                                                                            }
                                                                                        });
                                                                            } else {
                                                                                pDialog.dismiss();
                                                                                DialogCustoms.showSnackBar(requireContext(), task.getException().getMessage(), binding.getRoot());
                                                                            }
                                                                        }
                                                                    });
                                                        } else if (status.equalsIgnoreCase("completed")) {
                                                            appClass.firestore.collection("partners")
                                                                    .document(appClass.sharedPref.getUser().getPin())
                                                                    .get()
                                                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                            if(task.isSuccessful()) {
                                                                                String currWalAmt = task.getResult().getString("wallet");
                                                                                String newWalAmt = currWalAmt + ((Long.parseLong(data.totalRentCost) * 5) / 100);
                                                                                HashMap<String, Object> map = new HashMap<>();
                                                                                map.put("wallet", newWalAmt);
                                                                                appClass.firestore.collection("partners")
                                                                                        .document(appClass.sharedPref.getUser().getPin())
                                                                                        .update(map)
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if(task.isSuccessful()) {
                                                                                                    appClass.sharedPref.setWallet(newWalAmt);
                                                                                                    HashMap<String, Object> map = new HashMap<>();
                                                                                                    map.put("amount", newWalAmt);
                                                                                                    map.put("date", dateAndTime);
                                                                                                    map.put("info", null);
                                                                                                    map.put("name", appClass.sharedPref.getUser().getName());
                                                                                                    map.put("status", "completed");
                                                                                                    map.put("type", "rentRefund");
                                                                                                    map.put("advertisementId", data.advertisementId);
                                                                                                    map.put("timestamp", System.currentTimeMillis());
                                                                                                    map.put("isBroRental", false);
                                                                                                    appClass.firestore.collection("transactions").add(map)
                                                                                                                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                                                                                        @Override
                                                                                                                        public void onComplete(@NonNull Task<DocumentReference> task) {
                                                                                                                            if(task.isSuccessful()) {
                                                                                                                                pDialog.dismiss();
                                                                                                                                getRentItems();
                                                                                                                                DialogCustoms.showSnackBar(requireContext(), "Payment Credited Successfully", binding.getRoot());
                                                                                                                                Log.d(TAG, "onComplete: success wallet added");
                                                                                                                            } else {
                                                                                                                                pDialog.dismiss();
                                                                                                                                DialogCustoms.showSnackBar(requireContext(), task.getException().getMessage(), binding.getRoot());
                                                                                                                            }
                                                                                                                        }
                                                                                                                    });
                                                                                                } else {
                                                                                                    pDialog.dismiss();
                                                                                                    DialogCustoms.showSnackBar(requireContext(), task.getException().getMessage(), binding.getRoot());
                                                                                                }
                                                                                            }
                                                                                        });
                                                                            } else {
                                                                                pDialog.dismiss();
                                                                                DialogCustoms.showSnackBar(requireContext(), task.getException().getMessage(), binding.getRoot());
                                                                            }
                                                                        }
                                                                    });
                                                        } else
                                                            getRentItems();
                                                    } else {
                                                        pDialog.dismiss();
                                                        Toast.makeText(requireContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                }
                            });
                        } else {
                            Log.d(TAG, "onComplete: " + task.getException());
                        }
                    }
                });
    }
}