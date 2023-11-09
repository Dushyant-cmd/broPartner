package com.brorental.bropartner.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.brorental.bropartner.MainActivity;
import com.brorental.bropartner.R;
import com.brorental.bropartner.adapters.PaymentAdapter;
import com.brorental.bropartner.databinding.ActivityPaymentHistoryBinding;
import com.brorental.bropartner.models.PaymentHistoryModel;
import com.brorental.bropartner.utilities.AppClass;
import com.brorental.bropartner.utilities.Utility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class PaymentHistory extends AppCompatActivity {
    private String TAG = "PaymentHistory.java";
    private ActivityPaymentHistoryBinding binding;
    private AppClass appClass;
    private ArrayList<PaymentHistoryModel> list = new ArrayList<>();
    private PaymentAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_payment_history);
        appClass = (AppClass) getApplication();
        //REGISTER BROADCAST RECEIVER FOR INTERNET
        Utility.registerConnectivityBR(PaymentHistory.this, appClass);
        adapter = new PaymentAdapter(this);
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.walletTV.setText(appClass.sharedPref.getUser().getWallet());

        setListners();

        if(Utility.isNetworkAvailable(this)) {
            getTransactions();
        } else {
            Snackbar bar = Snackbar.make(binding.getRoot(), "No Connection", Snackbar.LENGTH_INDEFINITE);
            bar.setAction("connect", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(Utility.isNetworkAvailable(PaymentHistory.this)) {
                        bar.dismiss();
                        Toast.makeText(PaymentHistory.this, "Connected", Toast.LENGTH_SHORT).show();
                    } else {
                        bar.dismiss();
                        bar.show();
                    }
                }
            });
        }
    }

    private void setListners() {
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        binding.swipeRef.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getTransactions();
            }
        });
    }

    private void getTransactions() {
        binding.shimmer.setVisibility(View.VISIBLE);
        binding.recyclerView.setVisibility(View.GONE);
        appClass.firestore.collection("transactions").whereEqualTo("broRentalId", appClass.sharedPref.getUser().getPin())
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        binding.swipeRef.setRefreshing(false);
                        binding.shimmer.setVisibility(View.GONE);
                        binding.recyclerView.setVisibility(View.VISIBLE);
                        try {
                            if(task.isSuccessful()) {
                                List<DocumentSnapshot> dList = task.getResult().getDocuments();
                                list.clear();
                                for(int i=0; i<dList.size(); i++) {
                                    DocumentSnapshot d = dList.get(i);
                                    list.add(new PaymentHistoryModel(d.getString("advertisementId"), d.getString("amount"),
                                            d.getString("broPartnerId"), d.getString("broRentalId"), d.getString("date"),
                                            d.getString("info"), d.getString("name"), d.getString("type"), d.getString("status"),
                                            d.getBoolean("isBroRental"), d.getLong("timestamp"), d.getString("id")));
                                }

                                adapter.submitList(list);
                            } else {
                                Log.d(TAG, "onComplete: " + task.getException());
                            }
                        } catch (Exception e) {
                            Log.d(TAG, "onComplete: " + e);
                        }
                    }
                });
    }
}