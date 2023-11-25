package com.brorental.bropartner.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.brorental.bropartner.R;
import com.brorental.bropartner.adapters.RentListAdapter;
import com.brorental.bropartner.databinding.ActivityRentBinding;
import com.brorental.bropartner.interfaces.UtilsInterface;
import com.brorental.bropartner.models.RentItemModel;
import com.brorental.bropartner.utilities.AppClass;
import com.brorental.bropartner.utilities.DialogCustoms;
import com.brorental.bropartner.utilities.ProgressDialog;
import com.brorental.bropartner.utilities.Utility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RentActivity extends AppCompatActivity {
    private AppClass appClass;
    private ActivityRentBinding binding;
    private String TAG = "RentActivity.java";
    private ArrayList<RentItemModel> list = new ArrayList<>();
    private AlertDialog pDialog;
    private RentListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_rent);
        appClass = (AppClass) getApplication();
        //REGISTER BROADCAST RECEIVER FOR INTERNET
        Utility.registerConnectivityBR(RentActivity.this, appClass);
        adapter = new RentListAdapter(this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
        pDialog = ProgressDialog.createAlertDialog(RentActivity.this);
        queries();
        setListeners();
    }

    private void queries() {
        binding.shimmer.setVisibility(View.VISIBLE);
        binding.recyclerView.setVisibility(View.GONE);
        appClass.firestore.collection("rent")
                .whereEqualTo("broPartnerId", appClass.sharedPref.getUser().getPin())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        binding.shimmer.setVisibility(View.GONE);
                        binding.recyclerView.setVisibility(View.VISIBLE);
                        binding.swipeRef.setRefreshing(false);
                        if(task.isSuccessful()) {
                            list.clear();
                            List<DocumentSnapshot> docList = task.getResult().getDocuments();
                            for (int i = 0; i < docList.size(); i++) {
                                DocumentSnapshot d = docList.get(i);
                                RentItemModel model = d.toObject(RentItemModel.class);
                                list.add(model);
                            }

                            adapter.submitList(list);
                            adapter.addRentRefreshListener(new UtilsInterface.RentRefreshListener() {
                                @Override
                                public void updateLiveStatus(boolean status, String docId, int pos) {
                                    pDialog.show();
                                    HashMap<String, Object> map = new HashMap<>();
                                    map.put("liveStatus", status);
                                    appClass.firestore.collection("rent")
                                            .document(docId)
                                            .update(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    pDialog.dismiss();
                                                    list.get(pos).setLiveStatus(status);
                                                    adapter.notifyDataSetChanged();
                                                    Log.d(TAG, "onSuccess: ");
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    pDialog.dismiss();
                                                    Log.d(TAG, "onFailure: " + e);
                                                }
                                            });
                                }

                                @Override
                                public void refresh() {
                                }
                            });
                        } else {
                            DialogCustoms.showSnackBar(RentActivity.this, task.getException().getMessage().toString(), binding.getRoot());
                            Log.d(TAG, "onComplete: " + task.getException());
                        }
                    }
                });
    }

    private void setListeners() {
        binding.swipeRef.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                queries();
            }
        });
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.addBtn.setOnClickListener(view -> {
            Intent i  = new Intent(this, UploadRentItem.class);
            startActivity(i);
        });
    }

//    private void updateLiveStatus(boolean status, String docId) {
//        HashMap<String, Object> map = new HashMap<>();
//        map.put("liveStatus", status);
//        appClass.firestore.collection("rent")
//                .document(docId)
//                .update(map)
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void unused) {
//                        binding.swipeRef.setRefreshing(false);
//                        Log.d(TAG, "onSuccess: ");
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        binding.swipeRef.setRefreshing(false);
//                        Log.d(TAG, "onFailure: " + e);
//                    }
//                });
//    }
}