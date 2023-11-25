package com.brorental.bropartner.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
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
import com.brorental.bropartner.databinding.AuthPinDialogBinding;
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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

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
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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
                                    Calendar cal = Calendar.getInstance();
                                    Date date = cal.getTime();
                                    SimpleDateFormat spf = new SimpleDateFormat("dd-MM-yyyy, hh:mm:ss a", Locale.getDefault());
                                    String dateAndTime = spf.format(date);
                                    if (status.equalsIgnoreCase("reject")) {
                                        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireActivity());
                                        builder.setMessage("Are you sure to reject?");
                                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                pDialog.show();
                                                HashMap<String, Object> map = new HashMap<>();
                                                map.put("status", status);
                                                appClass.firestore.collection("rentHistory")
                                                        .document(data.id)
                                                        .update(map)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                appClass.firestore.collection("users")
                                                                        .document(data.broRentalId)
                                                                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                                if (task.isSuccessful()) {
                                                                                    String currentWalAmt = task.getResult().getString("wallet");
                                                                                    String newWalAmt = String.valueOf(Long.parseLong(currentWalAmt) + Long.parseLong(data.totalRentCost));
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
                                                                                                        map.put("broRentalId", data.broRentalId);
                                                                                                        map.put("broPartnerId", data.broPartnerId);
                                                                                                        appClass.firestore.collection("transactions").add(map)
                                                                                                                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                                                                                    @Override
                                                                                                                    public void onComplete(@NonNull Task<DocumentReference> task) {
                                                                                                                        if (task.isSuccessful()) {
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
                                                            }
                                                        });
                                            }
                                        });
                                        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {

                                            }
                                        });
                                        builder.create().show();
                                    } else {
                                        String productPin = data.id.substring(0, 4);
                                        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireActivity());
                                        AuthPinDialogBinding dialogBinding = AuthPinDialogBinding.inflate(getLayoutInflater());
                                        builder.setView(dialogBinding.getRoot());
                                        androidx.appcompat.app.AlertDialog alertDialog = builder.create();
                                        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                        dialogBinding.cancelBtn.setOnClickListener(view -> {
                                            alertDialog.dismiss();
                                        });

                                        dialogBinding.submitBtn.setOnClickListener(view -> {
                                            String pin = dialogBinding.etPin.getText().toString();
                                            if (pin.isEmpty() || pin.length() < 4) {
                                                dialogBinding.etPin.setError("Invalid");
                                                dialogBinding.etPin.requestFocus();
                                            } else if (productPin.matches(pin)) {
                                                alertDialog.dismiss();
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
                                                                    if (status.equalsIgnoreCase("completed")) {
                                                                        appClass.firestore.collection("partners")
                                                                                .document(appClass.sharedPref.getUser().getPin())
                                                                                .get()
                                                                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                                        if (task.isSuccessful()) {
                                                                                            String currWalAmt = task.getResult().getString("wallet");
                                                                                            String newWalAmt = String.valueOf(Long.parseLong(currWalAmt) + ((Long.parseLong(data.totalRentCost) * 5) / 100));
                                                                                            HashMap<String, Object> map = new HashMap<>();
                                                                                            map.put("wallet", newWalAmt);
                                                                                            appClass.firestore.collection("partners")
                                                                                                    .document(appClass.sharedPref.getUser().getPin())
                                                                                                    .update(map)
                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                            if (task.isSuccessful()) {
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
                                                                                                                map.put("broRentalId", data.broRentalId);
                                                                                                                appClass.firestore.collection("transactions").add(map)
                                                                                                                        .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                                                                                            @Override
                                                                                                                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                                                                                                                if (task.isSuccessful()) {
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
                                                                    } else if (status.equalsIgnoreCase("ongoing")) {
                                                                        pDialog.dismiss();
                                                                        getRentItems();
                                                                    }
                                                                } else {
                                                                    pDialog.dismiss();
                                                                    Toast.makeText(requireContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                            } else {
                                                dialogBinding.etPin.setError("Invalid");
                                            }
                                        });
                                        alertDialog.show();
                                    }
                                }

                                @Override
                                public void contactListener(String type) {
                                    if (type.equalsIgnoreCase("dial")) {
                                        Intent i = new Intent(Intent.ACTION_DIAL);
                                        i.setData(Uri.parse("tel:" + "+919773602742"));
                                        requireActivity().startActivity(i);
                                    }
                                }
                            });
                        } else {
                            Log.d(TAG, "onComplete: " + task.getException());
                        }
                    }
                });
    }
}