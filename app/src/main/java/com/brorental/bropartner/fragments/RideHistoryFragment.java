package com.brorental.bropartner.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.brorental.bropartner.R;
import com.brorental.bropartner.adapters.RideHistoryAdapter;
import com.brorental.bropartner.databinding.AuthPinDialogBinding;
import com.brorental.bropartner.databinding.FragmentRideHistoryBinding;
import com.brorental.bropartner.databinding.RidePayDialogBinding;
import com.brorental.bropartner.interfaces.UtilsInterface;
import com.brorental.bropartner.models.RideHistoryModel;
import com.brorental.bropartner.utilities.AppClass;
import com.brorental.bropartner.utilities.DialogCustoms;
import com.brorental.bropartner.utilities.ProgressDialog;
import com.brorental.bropartner.utilities.Utility;
import com.bumptech.glide.Glide;
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
import java.util.List;
import java.util.Locale;

public class RideHistoryFragment extends Fragment {
    private String TAG = "RideHistoryFragment.java";
    private FragmentRideHistoryBinding binding;
    private Context ctx;
    private Activity activity;
    private AlertDialog pDialog;
    private AppClass appclass;
    private List<RideHistoryModel> list = new ArrayList<>();
    private RideHistoryAdapter adapter;
    private DocumentSnapshot lastDoc = null;
    private long page = 0;
    private String qrImg = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_ride_history, container, false);
        ctx = requireContext();
        activity = requireActivity();
        pDialog = ProgressDialog.createAlertDialog(ctx);
        appclass = (AppClass) activity.getApplication();
        adapter = new RideHistoryAdapter(ctx, appclass);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
        binding.recyclerView.setAdapter(adapter);
        getData();
        setListeners();
        return binding.getRoot();
    }

    private void setListeners() {
        binding.swipeRef.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getData();
            }
        });
    }

    private void getData() {
        if (Utility.isNetworkAvailable(ctx)) {
            queries();
        } else {
            Utility.noNetworkDialog(ctx, new UtilsInterface.RefreshInterface() {
                @Override
                public void refresh(int catePosition) {
                    queries();
                }
            });
        }
    }

    private void queries() {
        binding.shimmer.setVisibility(View.VISIBLE);
        binding.recyclerView.setVisibility(View.GONE);
        appclass.firestore.collection("rideHistory")
                .whereEqualTo("broPartnerId", appclass.sharedPref.getUser().getPin())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(10)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        binding.swipeRef.setRefreshing(false);
                        binding.shimmer.setVisibility(View.GONE);
                        binding.recyclerView.setVisibility(View.VISIBLE);
                        list.clear();
                        if (task.isSuccessful()) {
                            page = 0;
                            List<DocumentSnapshot> dList = task.getResult().getDocuments();
                            for (DocumentSnapshot d : dList) {
                                RideHistoryModel model = d.toObject(RideHistoryModel.class);
                                if (!model.getStatus().equalsIgnoreCase("pending"))
                                    list.add(model);
                            }

                            adapter.submitList(list);
                            adapter.addRefreshListeners(new UtilsInterface.RideHistoryListener() {
                                @Override
                                public void updateStatus(String status, String docId, int pos, RideHistoryModel data) {
                                    String productPin = data.getPin().substring(0, 4);
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
                                            if(status.equalsIgnoreCase("completed")) {
                                                completePayment(map, docId, data);
                                            } else if(status.equalsIgnoreCase("ongoing")) {
                                                map.put("startTimestamp", System.currentTimeMillis());
                                                appclass.firestore.collection("rideHistory")
                                                        .document(docId)
                                                        .update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                pDialog.dismiss();
                                                                if(task.isSuccessful()) {
                                                                    getData();
                                                                    Toast.makeText(ctx, "Ride started.", Toast.LENGTH_SHORT).show();
                                                                } else {
                                                                    Toast.makeText(ctx, "Please try again", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                            }
                                        } else {
                                            dialogBinding.etPin.setError("Invalid");
                                        }
                                    });
                                    alertDialog.show();
                                }

                            @Override
                            public void contactListener (String phoneNum){
                                Intent i = new Intent(Intent.ACTION_DIAL);
                                i.setData(Uri.parse("tel:" + phoneNum));
                                activity.startActivity(i);
                            }
                        });

                        if (!dList.isEmpty())
                            lastDoc = dList.get(dList.size() - 1);

                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M)
                            binding.nestedSv.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
                                @Override
                                public void onScrollChange(@NonNull NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                                    //Check if user scrolled till bottom
                                    if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
                                        Log.v(TAG, "list scroll till bottom");
                                        if (Utility.isNetworkAvailable(ctx) && page == 0) {
                                            page++;
                                            loadMoreGameResult();
                                        } else if (!Utility.isNetworkAvailable(ctx)) {
                                            Toast.makeText(getActivity(), "Check internet connection", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            });
                    } else

                    {
                        DialogCustoms.showSnackBar(ctx, "Please try again", binding.getRoot());
                    }
                }
    });


        appclass.firestore.collection("appData").document("lowerSettings")
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()) {
                            qrImg = task.getResult().getString("upiQrImage");
                        } else {
                            Log.d(TAG, "onComplete: " + task.getException());
                        }
                    }
                });
}

    private void completePayment(HashMap<String, Object> map, String docId, RideHistoryModel data) {
        androidx.appcompat.app.AlertDialog.Builder builder1 = new androidx.appcompat.app.AlertDialog.Builder(ctx);
        RidePayDialogBinding binding2 = DataBindingUtil.inflate(activity.getLayoutInflater(), R.layout.ride_pay_dialog, binding.nestedSv, false);
        builder1.setView(binding2.getRoot());
        androidx.appcompat.app.AlertDialog payDialog = builder1.create();
        ArrayList<String> payList = new ArrayList<>();
        payList.add("online");
        payList.add("cod");
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(ctx, android.R.layout.simple_spinner_dropdown_item, payList);
        binding2.spinnerPay.setAdapter(arrayAdapter);
        binding2.ivPay.setVisibility(View.VISIBLE);
        Glide.with(ctx).load(qrImg).placeholder(R.drawable.no_pictures).into(binding2.ivPay);
        binding2.spinnerPay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(payList.get(i).equalsIgnoreCase("cod")) {
                    binding2.ivPay.setVisibility(View.GONE);
                } else {
                    binding2.ivPay.setVisibility(View.VISIBLE);
                    Glide.with(ctx).load(qrImg).placeholder(R.drawable.no_pictures).into(binding2.ivPay);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        binding2.btnCancel.setOnClickListener(v -> {
            payDialog.dismiss();
            pDialog.dismiss();
        });

        Calendar cal = Calendar.getInstance();
        Date date = cal.getTime();
        SimpleDateFormat spf = new SimpleDateFormat("dd-MM-yyyy, hh:mm:ss a", Locale.getDefault());
        String dateAndTime = spf.format(date);
        binding2.btnSubmit.setOnClickListener(v -> {
            binding2.btnSubmit.setEnabled(false);
            builder1.setCancelable(false);
            String selectedPayMode = binding2.spinnerPay.getSelectedItem().toString();
            map.put("endTimestamp", System.currentTimeMillis());

            if(selectedPayMode.equalsIgnoreCase("cod")) {
                map.put("paymentMode", "cod");
                appclass.firestore.collection("rideHistory")
                        .document(docId)
                        .update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                HashMap<String, Object> map = new HashMap<>();
                                map.put("amount", data.getAmount());
                                map.put("date", dateAndTime);
                                map.put("info", null);
                                map.put("name", appclass.sharedPref.getUser().getName());
                                map.put("status", "pending");
                                map.put("type", "ride");
                                map.put("advertisementId", "");
                                map.put("timestamp", System.currentTimeMillis());
                                map.put("isBroRental", false);
                                map.put("broRentalId", "");
                                map.put("broPartnerId", appclass.sharedPref.getUser().getPin());
                                appclass.firestore.collection("transactions").add(map)
                                        .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                                if(task.isSuccessful()) {
                                                    pDialog.dismiss();
                                                    payDialog.dismiss();
                                                    getData();
                                                    if(task.isSuccessful()) {
                                                        updateTotalRides();
                                                        DialogCustoms.showSnackBar(ctx, "Ride Completed", binding.getRoot());
                                                    } else {
                                                        Toast.makeText(ctx, "Please try again", Toast.LENGTH_SHORT).show();
                                                    }
                                                } else {
                                                    Log.d(TAG, "onComplete: " + task.getException());
                                                }
                                            }
                                        });
                            }
                        });

            } else if(selectedPayMode.equalsIgnoreCase("online")) {
                map.put("paymentMode", "online");
                appclass.firestore.collection("rideHistory")
                        .document(docId)
                        .update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                HashMap<String, Object> map = new HashMap<>();
                                map.put("amount", data.getAmount());
                                map.put("date", dateAndTime);
                                map.put("info", null);
                                map.put("name", appclass.sharedPref.getUser().getName());
                                map.put("status", "pending");
                                map.put("type", "ride");
                                map.put("advertisementId", "");
                                map.put("timestamp", System.currentTimeMillis());
                                map.put("isBroRental", false);
                                map.put("broRentalId", "");
                                map.put("broPartnerId", appclass.sharedPref.getUser().getPin());
                                appclass.firestore.collection("transactions").add(map)
                                                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentReference> task) {
                                                        if(task.isSuccessful()) {
                                                            pDialog.dismiss();
                                                            payDialog.dismiss();
                                                            getData();
                                                            if(task.isSuccessful()) {
                                                                DialogCustoms.showSnackBar(ctx, "Ride Completed", binding.getRoot());
                                                            } else {
                                                                Toast.makeText(ctx, "Please try again", Toast.LENGTH_SHORT).show();
                                                            }
                                                        } else {
                                                            Log.d(TAG, "onComplete: " + task.getException());
                                                        }
                                                    }
                                                });
                            }
                        });
            }

            HashMap<String, Object> updateMap = new HashMap<>();
            updateMap.put("readyForRide", true);
            updateMap.put("wallet", (Long.parseLong(appclass.sharedPref.getUser().getWallet()) + data.getAmount()));
            appclass.firestore.collection("partners")
                    .document(appclass.sharedPref.getUser().getPin())
                    .update(updateMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                Log.d(TAG, "onComplete: success");
                            } else {
                                Toast.makeText(ctx, "Try again", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });
        payDialog.show();
    }

    private void loadMoreGameResult() {
        try {
            pDialog.show();
            appclass.firestore.collection("rideHistory")
                    .whereEqualTo("broPartnerId", appclass.sharedPref.getUser().getPin())
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .startAfter(lastDoc)
                    .limit(10)
                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            page = 0;
                            pDialog.dismiss();
                            if (task.isSuccessful()) {
                                List<DocumentSnapshot> dList = task.getResult().getDocuments();
                                if (!dList.isEmpty()) {
                                    for (DocumentSnapshot d : dList) {
                                        RideHistoryModel model = d.toObject(RideHistoryModel.class);
                                        if (!model.getStatus().equalsIgnoreCase("pending"))
                                            list.add(model);
                                    }

                                    if (!dList.isEmpty())
                                        lastDoc = dList.get(dList.size() - 1);

                                    adapter.submitList(list);
                                    adapter.notifyDataSetChanged();
                                } else {
                                    Toast.makeText(ctx, "No data found", Toast.LENGTH_SHORT).show();
                                    page++;
                                }
                            } else {
                                DialogCustoms.showSnackBar(ctx, "Please try again", binding.getRoot());
                            }
                        }
                    });
        } catch (Exception e) {
            Log.d(TAG, "loadMoreGameResult: " + e);
        }
    }

    private void updateTotalRides() {
        appclass.firestore.collection("users").document(appclass.sharedPref.getUser().getPin())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()) {
                            long totalRides = Long.parseLong(task.getResult().getString("totalRides"));
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("totalRides", --totalRides);
                            appclass.firestore.collection("users").document(appclass.sharedPref.getUser().getPin())
                                    .update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "onComplete: success");
                                            } else {
                                                Log.d(TAG, "onComplete: " + task.getException());
                                            }
                                        }
                                    });
                        } else {
                            Log.d(TAG, "onComplete:  " + task.getException());
                        }
                    }
                });
    }
}