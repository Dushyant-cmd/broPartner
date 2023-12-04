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
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.brorental.bropartner.R;
import com.brorental.bropartner.activities.HistoryActivity;
import com.brorental.bropartner.activities.RideActivity;
import com.brorental.bropartner.adapters.RideHistoryAdapter;
import com.brorental.bropartner.databinding.FragmentRidesBinding;
import com.brorental.bropartner.databinding.RidesFilterDialogBinding;
import com.brorental.bropartner.interfaces.UtilsInterface;
import com.brorental.bropartner.models.RideHistoryModel;
import com.brorental.bropartner.utilities.AppClass;
import com.brorental.bropartner.utilities.DialogCustoms;
import com.brorental.bropartner.utilities.ProgressDialog;
import com.brorental.bropartner.utilities.Utility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RidesFragment extends Fragment {
    private String TAG = "RidesFragment.java";
    private FragmentRidesBinding binding;
    private long page = 0;
    private DocumentSnapshot lastDoc;
    private List<RideHistoryModel> list = new ArrayList<>();
    private Context context;
    private Activity activity;
    private AppClass appClass;
    private AlertDialog pDialog;
    private RideHistoryAdapter adapter;
    private ArrayList<String> fromList = new ArrayList<>(), toList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_rides, container, false);
        context = requireContext();
        activity = requireActivity();
        appClass = (AppClass) activity.getApplication();
        pDialog = ProgressDialog.createAlertDialog(context);
        adapter = new RideHistoryAdapter(context, appClass);
        binding.recyclerViewRide.setLayoutManager(new LinearLayoutManager(context));
        binding.recyclerViewRide.setAdapter(adapter);
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

        binding.filterFM.setOnClickListener(view -> {
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
            RidesFilterDialogBinding binding2 = DataBindingUtil.inflate(activity.getLayoutInflater(), R.layout.rides_filter_dialog, binding.swipeRef, false);
            builder.setView(binding2.getRoot());
            ArrayAdapter<String> fromAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, fromList);
            ArrayAdapter<String> toAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, toList);
            binding2.spinnerFrom.setAdapter(fromAdapter);
            binding2.spinnerTo.setAdapter(toAdapter);
            androidx.appcompat.app.AlertDialog dialog = builder.create();
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            binding2.submit.setOnClickListener(v -> {
                getFilterData(binding2.spinnerFrom.getSelectedItem().toString(), binding2.spinnerTo.getSelectedItem().toString(), dialog);
            });

            if (!fromList.isEmpty() && !toList.isEmpty()) {
                dialog.show();
            } else {
                Snackbar bar = Snackbar.make(binding.getRoot(), "No points added", Snackbar.LENGTH_INDEFINITE);
                bar.setAction("Add point", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(context, RideActivity.class);
                        startActivity(i);
                    }
                });

                bar.show();
            }
        });

        binding.tvRideViewAll.setOnClickListener(view -> {
            Intent i = new Intent(context, HistoryActivity.class);
            startActivity(i);
        });
    }

    private void getFilterData(String from, String to, androidx.appcompat.app.AlertDialog alertDialog) {
        pDialog.show();
        appClass.firestore.collection("rideHistory")
                .whereEqualTo("status", "pending")
                .whereEqualTo("from", from)
                .whereEqualTo("to", to)
                .limit(10)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<DocumentSnapshot> list2 = task.getResult().getDocuments();
                        if (list2.isEmpty()) {
                            return;
                        }
                        list.clear();
                        for (DocumentSnapshot d : list2) {
                            RideHistoryModel model = d.toObject(RideHistoryModel.class);
                            list.add(model);
                        }

                        alertDialog.dismiss();
                        pDialog.dismiss();
                        adapter.submitList(list);
                        adapter.notifyDataSetChanged();
                    } else {
                        DialogCustoms.showSnackBar(context, "No data found", binding.getRoot());
                    }
                });
    }

    private void getData() {
        if (Utility.isNetworkAvailable(context)) {
            queries();
        } else {
            Utility.noNetworkDialog(context, new UtilsInterface.RefreshInterface() {
                @Override
                public void refresh(int catePosition) {
                    queries();
                }
            });
        }
    }

    private void queries() {
        //ride history.
        pDialog.show();
        appClass.firestore.collection("rideHistory")
                .whereEqualTo("status", "pending")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(10)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        binding.swipeRef.setRefreshing(false);
                        binding.recyclerViewRide.setVisibility(View.VISIBLE);
                        pDialog.dismiss();
                        page = 0;
                        if (task.isSuccessful()) {
                            list.clear();
                            List<DocumentSnapshot> dList = task.getResult().getDocuments();
                            for (DocumentSnapshot d : dList) {
                                list.add(d.toObject(RideHistoryModel.class));
                            }

                            if (list.isEmpty())
                                binding.errorRide.setVisibility(View.VISIBLE);
                            else
                                binding.errorRide.setVisibility(View.GONE);

                            if (!dList.isEmpty())
                                lastDoc = dList.get(dList.size() - 1);

                            adapter.submitList(list);
                            adapter.addRefreshListeners(new UtilsInterface.RideHistoryListener() {
                                @Override
                                public void updateStatus(String status, String docId, int pos, RideHistoryModel data) {
                                    pDialog.show();
                                    appClass.firestore.collection("partners")
                                            .document(appClass.sharedPref.getUser().getPin())
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        String bikeNum = task.getResult().getString("rideBikeNum");
                                                        if (bikeNum.isEmpty()) {
                                                            pDialog.dismiss();
                                                            DialogCustoms.showSnackBar(context, "Add bike number", binding.getRoot());
                                                        } else {
                                                            appClass.firestore.collection("rideHistory").document(docId)
                                                                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                            if (task.isSuccessful()) {
                                                                                if (task.getResult().getString("status").equalsIgnoreCase("pending")) {
                                                                                    appClass.firestore.collection("partners")
                                                                                            .document(appClass.sharedPref.getUser().getPin())
                                                                                            .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                                                    if (task.isSuccessful()) {
                                                                                                        boolean readyForRide = task.getResult().getBoolean("readyForRide");
                                                                                                        if (readyForRide) {
                                                                                                            HashMap<String, Object> map2 = new HashMap<>();
                                                                                                            map2.put("readyForRide", false);
                                                                                                            appClass.firestore.collection("partners")
                                                                                                                    .document(appClass.sharedPref.getUser().getPin())
                                                                                                                    .update(map2).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                        @Override
                                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                                            if (task.isSuccessful()) {
                                                                                                                                HashMap<String, Object> map = new HashMap<>();
                                                                                                                                map.put("broPartnerId", appClass.sharedPref.getUser().getPin());
                                                                                                                                map.put("broPartnerNumber", appClass.sharedPref.getUser().getMobile());
                                                                                                                                map.put("status", status);
                                                                                                                                appClass.firestore.collection("rideHistory")
                                                                                                                                        .document(docId)
                                                                                                                                        .update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                                            @Override
                                                                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                                                                pDialog.dismiss();
                                                                                                                                                if (task.isSuccessful()) {
                                                                                                                                                    list.remove(pos);
                                                                                                                                                    adapter.submitList(list);
                                                                                                                                                    adapter.notifyDataSetChanged();
                                                                                                                                                } else {
                                                                                                                                                    DialogCustoms.showSnackBar(context, "Please try again", binding.getRoot());
                                                                                                                                                }
                                                                                                                                            }
                                                                                                                                        });
                                                                                                                            } else {
                                                                                                                                pDialog.dismiss();
                                                                                                                                DialogCustoms.showSnackBar(context, "Please try again", binding.getRoot());
                                                                                                                            }
                                                                                                                        }
                                                                                                                    });
                                                                                                        } else {
                                                                                                            pDialog.dismiss();
                                                                                                            Snackbar snackbar = Snackbar.make(binding.getRoot(), "Please complete previous ride", Snackbar.LENGTH_SHORT);
                                                                                                            snackbar.setAction("Okay", new View.OnClickListener() {
                                                                                                                @Override
                                                                                                                public void onClick(View view) {
                                                                                                                }
                                                                                                            });

                                                                                                            snackbar.show();
                                                                                                        }
                                                                                                    } else {
                                                                                                        pDialog.dismiss();
                                                                                                        DialogCustoms.showSnackBar(context, "Please try again", binding.getRoot());
                                                                                                    }
                                                                                                }
                                                                                            });
                                                                                } else {
                                                                                    pDialog.dismiss();
                                                                                    if (list.isEmpty()) {
                                                                                        getData();
                                                                                    } else {
                                                                                        list.remove(pos);
                                                                                        adapter.submitList(list);
                                                                                        adapter.notifyDataSetChanged();
                                                                                    }

                                                                                    DialogCustoms.showSnackBar(context, "Ride already accepted", binding.getRoot());
                                                                                }
                                                                            } else {
                                                                                Log.d(TAG, "onComplete: " + task.getException());
                                                                                pDialog.dismiss();
                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    }
                                                }
                                            });

                                }

                                @Override
                                public void contactListener(String phoneNum) {
                                    Intent i = new Intent(Intent.ACTION_DIAL);
                                    i.setData(Uri.parse("tel:" + phoneNum));
                                    startActivity(i);
                                }
                            });

                            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M)
                                binding.nestedSv.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
                                    @Override
                                    public void onScrollChange(@NonNull NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                                        //Check if user scrolled till bottom
                                        if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
                                            Log.v(TAG, "list scroll till bottom");
                                            if (Utility.isNetworkAvailable(context) && page == 0) {
                                                page++;
                                                loadMoreRideResult();
                                            } else if (!Utility.isNetworkAvailable(context)) {
                                                Toast.makeText(context, "Check internet connection", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                });
                        } else {
                            DialogCustoms.showSnackBar(context, "Please try again", binding.getRoot());
                        }
                    }
                });

        //get all the added points
        appClass.firestore.collection("pointsHistory")
                .whereEqualTo("status", true)
                .whereEqualTo("broPartnerId", appClass.sharedPref.getUser().getPin())
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<DocumentSnapshot> list2 = task.getResult().getDocuments();
                            for (DocumentSnapshot d : list2) {
                                fromList.add(d.getString("from"));
                                toList.add(d.getString("to"));
                            }
                        } else {
                            DialogCustoms.showSnackBar(context, "Please add points for filter", binding.getRoot());
                        }
                    }
                });
    }

    private void loadMoreRideResult() {
        pDialog.show();
        appClass.firestore.collection("rideHistory")
                .whereEqualTo("status", "pending")
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
                                    list.add(model);
                                }

                                if (!dList.isEmpty())
                                    lastDoc = dList.get(dList.size() - 1);

                                adapter.submitList(list);
                                adapter.notifyDataSetChanged();
                            } else {
                                Toast.makeText(context, "No data found", Toast.LENGTH_SHORT).show();
                                page++;
                            }
                        } else {
                            DialogCustoms.showSnackBar(context, "Please try again", binding.getRoot());
                        }
                    }
                });
    }
}