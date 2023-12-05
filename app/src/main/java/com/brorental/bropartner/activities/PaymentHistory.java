package com.brorental.bropartner.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.brorental.bropartner.R;
import com.brorental.bropartner.adapters.PaymentAdapter;
import com.brorental.bropartner.databinding.ActivityPaymentHistoryBinding;
import com.brorental.bropartner.models.PaymentHistoryModel;
import com.brorental.bropartner.models.User;
import com.brorental.bropartner.utilities.AppClass;
import com.brorental.bropartner.utilities.DialogCustoms;
import com.brorental.bropartner.utilities.Utility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
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
        binding.walletTV.setText("\u20b9 " + appClass.sharedPref.getUser().getWallet());

        setListners();
        getData();
    }

    private void getData() {
        if (Utility.isNetworkAvailable(this)) {
            getTransactions();
        } else {
            Snackbar bar = Snackbar.make(binding.getRoot(), "No Connection", Snackbar.LENGTH_INDEFINITE);
            bar.setAction("connect", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Utility.isNetworkAvailable(PaymentHistory.this)) {
                        bar.dismiss();
                        getTransactions();
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
                getData();
            }
        });

        binding.wdLy.setOnClickListener(view1 -> {
            Calendar cal = Calendar.getInstance();
            Date date = cal.getTime();
            SimpleDateFormat spf = new SimpleDateFormat("dd-MM-yyyy, hh:mm:ss a", Locale.getDefault());
            String dateAndTime = spf.format(date);

            BottomSheetDialog sheet = new BottomSheetDialog(PaymentHistory.this);
            View view = LayoutInflater.from(PaymentHistory.this).inflate(R.layout.add_cash_sheet, null);
            sheet.setContentView(view);
            Button submitBtn = view.findViewById(R.id.confirmRec);
            Button cancelBtn = view.findViewById(R.id.cancelRec);
            EditText rechargeET = view.findViewById(R.id.rechargeAmt);
            submitBtn.setOnClickListener(v -> {
                String amt = rechargeET.getText().toString();
                if(Long.parseLong(amt) > 0) {
                    if (Long.parseLong(amt) < Long.parseLong(appClass.sharedPref.getUser().getWallet())) {
                        long newAmt = Long.parseLong(appClass.sharedPref.getUser().getWallet()) - Long.parseLong(amt);
                        HashMap<String, Object> map1 = new HashMap<>();
                        map1.put("wallet", String.valueOf(newAmt));
                        appClass.firestore.collection("partners")
                                .document(appClass.sharedPref.getUser().getPin())
                                .update(map1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()) {
                                            appClass.sharedPref.setWallet(String.valueOf(newAmt));
                                            HashMap<String, Object> map = new HashMap<>();
                                            map.put("amount", amt);
                                            map.put("date", dateAndTime);
                                            map.put("info", null);
                                            map.put("name", appClass.sharedPref.getUser().getName());
                                            map.put("status", "pending");
                                            map.put("type", "withdraw");
                                            map.put("advertisementId", "");
                                            map.put("timestamp", System.currentTimeMillis());
                                            map.put("isBroRental", false);
                                            map.put("broRentalId", "");
                                            map.put("broPartnerId", appClass.sharedPref.getUser().getPin());
                                            appClass.firestore.collection("transactions")
                                                    .add(map).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentReference> task) {
                                                            if(task.isSuccessful()) {
                                                                sheet.dismiss();
                                                                binding.walletTV.setText(Utility.rupeeIcon + appClass.sharedPref.getUser().getWallet());
                                                                getData();
                                                                DialogCustoms.showSnackBar(PaymentHistory.this, "Withdrawal Successfully", binding.getRoot());
                                                            } else {
                                                                sheet.dismiss();
                                                                DialogCustoms.showSnackBar(PaymentHistory.this, "Please check internet and try again", binding.getRoot());
                                                                Log.d(TAG, "onComplete: " + task.getException());
                                                            }
                                                        }
                                                    });
                                        } else {
                                            Toast.makeText(appClass, "Please try again later.", Toast.LENGTH_SHORT).show();
                                            Log.d(TAG, "onComplete: " + task.getException());
                                        }
                                    }
                                });
                    } else {
                        Toast.makeText(appClass, "Balance is too low", Toast.LENGTH_SHORT).show();
                    }
                } else
                    Toast.makeText(appClass, "Please Enter Amount", Toast.LENGTH_SHORT).show();
            });

            cancelBtn.setOnClickListener(v -> sheet.dismiss());
            sheet.show();
        });
    }

    private void getTransactions() {
        binding.shimmer.setVisibility(View.VISIBLE);
        binding.recyclerView.setVisibility(View.GONE);
        appClass.firestore.collection("transactions").whereEqualTo("broPartnerId", appClass.sharedPref.getUser().getPin())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        binding.swipeRef.setRefreshing(false);
                        binding.shimmer.setVisibility(View.GONE);
                        binding.recyclerView.setVisibility(View.VISIBLE);
                        try {
                            if (task.isSuccessful()) {
                                List<DocumentSnapshot> dList = task.getResult().getDocuments();
                                list.clear();
                                for (int i = 0; i < dList.size(); i++) {
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

        getProfile();
    }

    /**Below method to get profile from firebase */
    private void getProfile() {
        //profile update.
        appClass.firestore.collection("partners").document(appClass.sharedPref.getUser().getPin())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot d) {
                        binding.swipeRef.setRefreshing(false);
                        appClass.sharedPref.saveUser(new User(d.getString("name"), d.getString("mobile"), d.getString("pin"),
                                d.getString("totalRent"), d.getString("totalRide"), true,
                                d.getString("profileUrl"), d.getString("wallet")));
                        appClass.sharedPref.setAadhaarImg(d.getString("aadhaarImgUrl"));
                        appClass.sharedPref.setAadhaarPath(d.getString("aadhaarImgPath"));
                        appClass.sharedPref.setPanImgUrl(d.getString("panImgUrl"));
                        appClass.sharedPref.setPanImgPath(d.getString("panImgPath"));
                        appClass.sharedPref.setProfilePath(d.getString("profileImgPath"));
                        appClass.sharedPref.setStatus(d.getString("status"));
                        appClass.sharedPref.setState(d.getString("state"));
                        appClass.sharedPref.setAddress(d.getString("address"));

                        binding.walletTV.setText(Utility.rupeeIcon + appClass.sharedPref.getUser().getWallet());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        binding.swipeRef.setRefreshing(false);
                        Log.d(TAG, "onFailure: " + e);
                    }
                });
    }

}