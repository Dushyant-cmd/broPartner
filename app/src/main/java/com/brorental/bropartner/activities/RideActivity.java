package com.brorental.bropartner.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.brorental.bropartner.R;
import com.brorental.bropartner.adapters.PointsAdapter;
import com.brorental.bropartner.databinding.ActivityRideBinding;
import com.brorental.bropartner.databinding.BikeDrivingSheetBinding;
import com.brorental.bropartner.databinding.PointsBottomSheetBinding;
import com.brorental.bropartner.interfaces.UtilsInterface;
import com.brorental.bropartner.models.PointsHistoryModel;
import com.brorental.bropartner.utilities.AppClass;
import com.brorental.bropartner.utilities.DialogCustoms;
import com.brorental.bropartner.utilities.ProgressDialog;
import com.brorental.bropartner.utilities.Utility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class RideActivity extends AppCompatActivity {
    private AppClass appClass;
    private ActivityRideBinding binding;
    private List<PointsHistoryModel> list = new ArrayList<>();
    private PointsAdapter adapter;
    private String[] fromArr, toArr;
    private boolean isAllFabVisible = false;
    private AlertDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_ride);
        appClass = (AppClass) getApplication();
        //REGISTER BROADCAST RECEIVER FOR INTERNET
        Utility.registerConnectivityBR(RideActivity.this, appClass);
        //Check if bikeRideNum is empty or not in broPartner document, if true then display a button
        //to add riding bike number, else display points if added else display a gif for no points found
        //with a button to add a point which opens a sheet.
        adapter = new PointsAdapter(RideActivity.this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(RideActivity.this));
        binding.recyclerView.setAdapter(adapter);
        binding.addFab.shrink();
        binding.exFabAddBikeNum.hide();
        binding.exFabAddPoints.hide();
        pDialog = ProgressDialog.createAlertDialog(RideActivity.this);
        getData();
        setListeners();
    }

    private void getData() {
        if (Utility.isNetworkAvailable(RideActivity.this)) {
            queries();
        } else {
            Utility.noNetworkDialog(RideActivity.this, new UtilsInterface.RefreshInterface() {
                @Override
                public void refresh(int catePosition) {
                    queries();
                }
            });
        }
    }

    private void queries() {
        pDialog.show();
        appClass.firestore.collection("partners")
                .document(appClass.sharedPref.getUser().getPin())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        binding.swipeRef.setRefreshing(false);
                        if (task.isSuccessful()) {
                            String bikeNum = task.getResult().getString("rideBikeNum");
                            if (bikeNum.isEmpty()) {
                                pDialog.dismiss();
                                binding.addBikeNumBtn.setVisibility(View.VISIBLE);
                                binding.noDataLy.setVisibility(View.GONE);
                                binding.extLy.setVisibility(View.GONE);
                            } else {
                                appClass.firestore.collection("pointsHistory")
                                        .whereEqualTo("broPartnerId", appClass.sharedPref.getUser().getPin())
                                        .orderBy("timestamp", Query.Direction.DESCENDING)
                                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    pDialog.dismiss();
                                                    binding.noDataLy.setVisibility(View.GONE);
                                                    binding.addBikeNumBtn.setVisibility(View.GONE);
                                                    binding.extLy.setVisibility(View.VISIBLE);
                                                    binding.recyclerView.setVisibility(View.VISIBLE);
                                                    List<DocumentSnapshot> docList = task.getResult().getDocuments();
                                                    for (DocumentSnapshot d : docList) {
                                                        PointsHistoryModel model = d.toObject(PointsHistoryModel.class);
                                                        list.add(model);
                                                    }
                                                    adapter.submitList(list);
                                                    adapter.addRefreshListener(new UtilsInterface.RentRefreshListener() {
                                                        @Override
                                                        public void updateLiveStatus(boolean status, String docId, int pos) {
                                                            HashMap<String, Object> map = new HashMap<>();
                                                            map.put("status", status);
                                                            appClass.firestore.collection("pointsHistory")
                                                                    .document(docId)
                                                                    .update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                getData();
                                                                                Toast.makeText(RideActivity.this, "Success", Toast.LENGTH_SHORT).show();
                                                                            } else {
                                                                                DialogCustoms.showSnackBar(RideActivity.this, task.getException().getMessage(), binding.getRoot());
                                                                            }
                                                                        }
                                                                    });
                                                        }

                                                        @Override
                                                        public void refresh() {
                                                            //refresh
                                                        }
                                                    });
                                                } else {
                                                    pDialog.dismiss();
                                                    DialogCustoms.showSnackBar(RideActivity.this, task.getException().getMessage(), binding.getRoot());
                                                }
                                            }
                                        });
                            }
                        } else {
                            pDialog.dismiss();
                            DialogCustoms.showSnackBar(RideActivity.this, task.getException().getMessage(), binding.getRoot());
                        }
                    }
                });

        appClass.firestore.collection("appData")
                .document("constants")
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        fromArr = task.getResult().getString("from").split(",");
                        toArr = task.getResult().getString("to").split(",");
                    }
                });
    }

    private void setListeners() {
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.addBikeNumBtn.setOnClickListener(view -> {
            BikeDetailsSheet sheet = new BikeDetailsSheet(appClass);
            sheet.show(getSupportFragmentManager(), "Bike Details");
        });

        binding.swipeRef.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getData();
            }
        });

        binding.addFab.setOnClickListener(view -> {
            if (!isAllFabVisible) {
                binding.exFabAddPoints.show();
                binding.exFabAddPoints.extend();
                binding.exFabAddBikeNum.show();
                binding.exFabAddBikeNum.extend();
                binding.addFab.extend();
                isAllFabVisible = true;
                binding.addFab.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.close_ic, getTheme()));
            } else {
                binding.exFabAddPoints.hide();
                binding.exFabAddPoints.shrink();
                binding.exFabAddBikeNum.hide();
                binding.exFabAddBikeNum.shrink();
                isAllFabVisible = false;
                binding.addFab.shrink();

                binding.addFab.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.baseline_add_24, getTheme()));
            }
        });

        binding.exFabAddBikeNum.setOnClickListener(view -> {
            BikeDetailsSheet sheet = new BikeDetailsSheet(appClass);
            sheet.show(getSupportFragmentManager(), "bike_details");
        });

        binding.exFabAddPoints.setOnClickListener(view -> {
            PointAddSheet sheet = new PointAddSheet(appClass, fromArr, toArr, new UtilsInterface.PointRefreshListener() {
                @Override
                public void refresh(HashMap<String, Object> map) {
                    String from = map.get("from").toString();
                    String to = map.get("to").toString();
                    String docId = map.get("docId").toString();
                    String broPartnerId = map.get("broPartnerId").toString();
                    long amt = Long.parseLong(map.get("amount").toString());
                    long dis = Long.parseLong(map.get("distance").toString());
                    long totalRides = Long.parseLong(map.get("totalRides").toString());
                    long timestamp = Long.parseLong(map.get("timestamp").toString());
                    boolean status = Boolean.parseBoolean(map.get("status").toString());
                    PointsHistoryModel newModel = new PointsHistoryModel(from, to, broPartnerId, docId, status, amt, dis, timestamp, totalRides);
                    list.add(0, newModel);
                    adapter.submitList(list);
                    adapter.notifyDataSetChanged();
                }
            });

            sheet.show(getSupportFragmentManager(), "point_sheet");
        });
    }


    public static class PointAddSheet extends BottomSheetDialogFragment {
        private String TAG = "PointAddSheet.java";
        private Context ctx;
        private Activity activity;
        private PointsBottomSheetBinding binding;
        private AppClass appClass;
        private String[] fromArr, toArr;
        private String from, to;
        private long amt, dis;
        private UtilsInterface.PointRefreshListener refreshListener;
        private AlertDialog pDialog;

        public PointAddSheet(AppClass appClass, String[] fromArr, String[] toArr, UtilsInterface.PointRefreshListener refreshListener) {
            this.appClass = appClass;
            this.fromArr = fromArr;
            this.toArr = toArr;
            this.refreshListener = refreshListener;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            binding = DataBindingUtil.inflate(inflater, R.layout.points_bottom_sheet, container, false);
            ctx = requireContext();
            activity = requireActivity();
            List<String> fromList = new ArrayList<>();
            List<String> toList = new ArrayList<>();
            fromList.add("Select From");
            toList.add("Select To");
            Collections.addAll(fromList, fromArr);
            Collections.addAll(toList, toArr);
            ArrayAdapter<String> fromAdapter = new ArrayAdapter<>(ctx, android.R.layout.simple_spinner_dropdown_item, fromList);
            ArrayAdapter<String> toAdapter = new ArrayAdapter<>(ctx, android.R.layout.simple_spinner_dropdown_item, toList);
            binding.spinnerFrom.setAdapter(fromAdapter);
            binding.spinnerTo.setAdapter(toAdapter);
            pDialog = ProgressDialog.createAlertDialog(ctx);

            setPointListeners();
            return binding.getRoot();
        }

        private void setPointListeners() {
            binding.spinnerFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    from = binding.spinnerFrom.getSelectedItem().toString();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    //no selected
                }
            });

            binding.spinnerTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    to = binding.spinnerTo.getSelectedItem().toString();
                    if (!to.toLowerCase().contains("select") && !from.toLowerCase().contains("select")) {
                        getPointsDetails(from, to);
                    } else
                        binding.textLL.setVisibility(View.GONE);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    //no selected
                }
            });

            binding.btnAdd.setOnClickListener(view -> {
                if (!from.toLowerCase().contains("select") && !to.toLowerCase().contains("select")) {
                    checkAndAddPoint(amt, dis);
                } else {
                    DialogCustoms.showSnackBar(ctx, "Select points", binding.getRoot());
                }
            });
        }

        private void checkAndAddPoint(long amt, long dis) {
            pDialog.show();
            appClass.firestore.collection("points")
                    .whereEqualTo("from", from)
                    .whereEqualTo("to", to)
                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                if (!task.getResult().getDocuments().isEmpty()) {
                                    appClass.firestore.collection("pointsHistory")
                                            .whereEqualTo("from", from)
                                            .whereEqualTo("to", to)
                                            .whereEqualTo("broPartnerId", appClass.sharedPref.getUser().getPin())
                                            .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if (task.getResult().getDocuments().isEmpty()) {
                                                        String docId = UUID.randomUUID().toString();
                                                        HashMap<String, Object> map = new HashMap<>();
                                                        map.put("from", from);
                                                        map.put("to", to);
                                                        map.put("amount", amt);
                                                        map.put("distance", dis);
                                                        map.put("broPartnerId", appClass.sharedPref.getUser().getPin());
                                                        map.put("totalRides", 0);
                                                        map.put("docId", docId);
                                                        map.put("status", true);
                                                        map.put("timestamp", System.currentTimeMillis());
                                                        appClass.firestore.collection("pointsHistory")
                                                                .document(docId)
                                                                .set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()) {
                                                                            pDialog.dismiss();
                                                                            dismiss();
                                                                            refreshListener.refresh(map);
                                                                            Toast.makeText(ctx, "Point Added Successfully", Toast.LENGTH_SHORT).show();
                                                                        } else {
                                                                            pDialog.dismiss();
                                                                            DialogCustoms.showSnackBar(ctx, task.getException().getMessage(), binding.getRoot());
                                                                        }
                                                                    }
                                                                });
                                                    } else {
                                                        pDialog.dismiss();
                                                        Toast.makeText(ctx, "Point Already Added", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                } else {
                                    pDialog.dismiss();
                                    DialogCustoms.showSnackBar(ctx, "Add another points", binding.getRoot());
                                }
                            } else {
                                pDialog.dismiss();
                                Log.d(TAG, "onComplete: " + task.getException().getMessage());
                            }
                        }
                    });
        }

        private void getPointsDetails(String from, String to) {
            pDialog.show();
            appClass.firestore.collection("points")
                    .whereEqualTo("from", from)
                    .whereEqualTo("to", to)
                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                pDialog.dismiss();
                                List<DocumentSnapshot> documents = task.getResult().getDocuments();
                                if (!documents.isEmpty()) {
                                    binding.textLL.setVisibility(View.VISIBLE);
                                    DocumentSnapshot d = documents.get(0);
                                    amt = d.getLong("amount");
                                    dis = d.getLong("distance");

                                    binding.tvRideAmt.setText("\u20b9 " + amt);
                                    binding.tvDis.setText(dis + " /km");
                                } else
                                    binding.textLL.setVisibility(View.GONE);
                            } else {
                                pDialog.dismiss();
                                DialogCustoms.showSnackBar(ctx, task.getException().getMessage(), binding.getRoot());
                            }
                        }
                    });
        }
    }

    public static class BikeDetailsSheet extends BottomSheetDialogFragment {
        private String TAG = "BikeDetailsSheet.java";
        private Context ctx;
        private Activity activity;
        private BikeDrivingSheetBinding binding;
        private AppClass appClass;

        public BikeDetailsSheet(AppClass appClass) {
            this.appClass = appClass;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            binding = DataBindingUtil.inflate(inflater, R.layout.bike_driving_sheet, container, false);
            ctx = requireContext();
            activity = requireActivity();

            binding.submitBtn.setOnClickListener(view -> {
                String bikeNum = binding.etBikeNum.getText().toString();
                if (!bikeNum.isEmpty() && bikeNum.length() >= 4) {
                    updateBikeNum(bikeNum);
                } else
                    DialogCustoms.showSnackBar(ctx, "Invalid", binding.getRoot());
            });
            return binding.getRoot();
        }

        private void updateBikeNum(String bikeNum) {
            AlertDialog pDialog = ProgressDialog.createAlertDialog(ctx);
            pDialog.show();
            HashMap<String, Object> map = new HashMap<>();
            map.put("rideBikeNum", bikeNum);
            appClass.firestore.collection("partners")
                    .document(appClass.sharedPref.getUser().getPin())
                    .update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            pDialog.dismiss();
                            if (task.isSuccessful()) {
                                dismiss();
                                Toast.makeText(ctx, "Bike Details Added", Toast.LENGTH_SHORT).show();
                            } else {
                                DialogCustoms.showSnackBar(ctx, task.getException().getMessage(), binding.getRoot());
                            }
                        }
                    });
        }
    }
}