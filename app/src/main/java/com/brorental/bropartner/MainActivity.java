package com.brorental.bropartner;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.brorental.bropartner.activities.HistoryActivity;
import com.brorental.bropartner.activities.PaymentHistory;
import com.brorental.bropartner.activities.ProfileActivity;
import com.brorental.bropartner.activities.RentActivity;
import com.brorental.bropartner.activities.RideActivity;
import com.brorental.bropartner.adapters.RentHistoryAdapter;
import com.brorental.bropartner.adapters.RideHistoryAdapter;
import com.brorental.bropartner.databinding.ActivityMainBinding;
import com.brorental.bropartner.databinding.AuthPinDialogBinding;
import com.brorental.bropartner.fragments.RidesFragment;
import com.brorental.bropartner.interfaces.UtilsInterface;
import com.brorental.bropartner.models.HistoryModel;
import com.brorental.bropartner.models.RideHistoryModel;
import com.brorental.bropartner.models.User;
import com.brorental.bropartner.utilities.AppClass;
import com.brorental.bropartner.utilities.DialogCustoms;
import com.brorental.bropartner.utilities.ProgressDialog;
import com.brorental.bropartner.utilities.Utility;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private String TAG = "MainBinding.java";
    private ArrayList<HistoryModel> rentList = new ArrayList<>();
    private ArrayList<RideHistoryModel> rideList = new ArrayList<>();
    private FirebaseFirestore mFirestore;
    private AppClass appClass;
    private TextView headerWalletTV, viewProfileTV, headerNameTV;
    private ImageView headerImageView;
    private LinearLayout headerWalletLL;
    private RentHistoryAdapter rentListAdapter;
    private RideHistoryAdapter rideListAdapter;
    private AlertDialog pDialog;
    private DocumentSnapshot rentLastDoc, rideLastDoc;
    private long rentPage = 0, ridePage = 0;

    //    private RideHistoryAdapter rentHistoryAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        appClass = (AppClass) getApplication();
        mFirestore = appClass.firestore;
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar, 0, 0);
        mDrawerToggle.syncState();
        //After instantiating your ActionBarDrawerToggle
        mDrawerToggle.setDrawerIndicatorEnabled(false);
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.navigation_menu_ic, getTheme());
        mDrawerToggle.setHomeAsUpIndicator(drawable);
        mDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.drawerLayout.isDrawerVisible(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    binding.drawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });

        binding.drawerLayout.addDrawerListener(mDrawerToggle);

        //header listeners and dynamic text.
        View headerView = binding.navigationView.getHeaderView(0);
        headerWalletTV = headerView.findViewById(R.id.walletTV);
        headerImageView = headerView.findViewById(R.id.profileIV);
        viewProfileTV = headerView.findViewById(R.id.viewProfileTV);
        headerNameTV = headerView.findViewById(R.id.nameTV);
        headerWalletLL = headerView.findViewById(R.id.walletLL);
        headerWalletTV.setText("\u20B9 " + appClass.sharedPref.getUser().getWallet());
        headerNameTV.setText(appClass.sharedPref.getUser().getName());

        Glide.with(this).load(appClass.sharedPref.getUser().getProfileUrl()).placeholder(R.drawable.default_profile).into(headerImageView);
        Glide.with(this).load(appClass.sharedPref.getBannerImage()).placeholder(R.drawable.no_pictures).into(binding.bannerIV);
        setListeners();

        rentListAdapter = new RentHistoryAdapter(MainActivity.this);
        binding.recyclerViewRent.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        binding.recyclerViewRent.setAdapter(rentListAdapter);

        rideListAdapter = new RideHistoryAdapter(MainActivity.this, appClass);
        binding.recyclerViewRide.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewRide.setAdapter(rideListAdapter);

        pDialog = ProgressDialog.createAlertDialog(MainActivity.this);
        //REGISTER BROADCAST RECEIVER FOR INTERNET
        Utility.registerConnectivityBR(MainActivity.this, appClass);
        String status = appClass.sharedPref.getStatus();
        /** TODO kyc dialog */
        if (status.equalsIgnoreCase("pending")) {
            DialogCustoms.noKycDialog(MainActivity.this, this, appClass, new UtilsInterface.NoKycRefresh() {
                @Override
                public void refresh(androidx.appcompat.app.AlertDialog alertDialog) {
                    getProfile(alertDialog);
                }
            });
            Toast.makeText(this, "Upload Profile.", Toast.LENGTH_SHORT).show();
        }

        getData();
    }

    private void getData() {
        if (Utility.isNetworkAvailable(this)) {
            queries();
        } else {
            Snackbar bar = Snackbar.make(binding.getRoot(), "No Connection", Snackbar.LENGTH_INDEFINITE);
            bar.setAction("Refresh", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Utility.isNetworkAvailable(MainActivity.this)) {
                        queries();
                        bar.dismiss();
                        Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                    } else {
                        bar.dismiss();
                        bar.show();
                    }
                }
            });

            bar.show();
        }
    }

    private void setListeners() {
        binding.tvRentViewAll.setOnClickListener(view -> {
            Intent i = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(i);
        });

        binding.tvRideViewAll.setOnClickListener(view -> {
            openFragment(new RidesFragment());
//            Intent i = new Intent(MainActivity.this, HistoryActivity.class);
//            startActivity(i);
        });

        //header listeners and dynamic text.
        headerImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, ProfileActivity.class);
                startActivityForRes(i);
            }
        });

        viewProfileTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, ProfileActivity.class);
                startActivityForRes(i);
            }
        });

        headerWalletLL.setOnClickListener(view -> {
            Intent i = new Intent(MainActivity.this, PaymentHistory.class);
            startActivityForRes(i);
        });
        binding.withdrawalBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, PaymentHistory.class);
                startActivityForRes(i);
            }
        });

        headerWalletTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, PaymentHistory.class);
                startActivity(i);
            }
        });

        binding.navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.profile) {
                    Intent i = new Intent(MainActivity.this, ProfileActivity.class);
                    startActivity(i);
                } else if (id == R.id.rent) {
                    Intent i = new Intent(MainActivity.this, RentActivity.class);
                    startActivity(i);
                } else if (id == R.id.driving) {
                    Intent i = new Intent(MainActivity.this, RideActivity.class);
                    startActivity(i);
                } else if (id == R.id.history) {
                    Intent i = new Intent(MainActivity.this, HistoryActivity.class);
                    startActivity(i);
                } else if (id == R.id.termsCon) {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://brorental.com/terms"));
                    startActivity(i);
                }

                binding.drawerLayout.close();

                return true;
            }
        });

        binding.swipeRef.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getData();
            }
        });
    }

    public void queries() {
//        Log.d(TAG, "getData: " + selectedState + "," + category);
        binding.shimmer.setVisibility(View.VISIBLE);
        binding.mainContentLl.setVisibility(View.GONE);
        Query query = mFirestore.collection("rentHistory").whereEqualTo("broPartnerId", appClass.sharedPref.getUser().getPin())
                .orderBy("timestamp", Query.Direction.DESCENDING).limit(10);
        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        binding.swipeRef.setRefreshing(false);
                        binding.shimmer.setVisibility(View.GONE);
                        binding.mainContentLl.setVisibility(View.VISIBLE);
                        rentPage = 0;
                        if (task.isSuccessful()) {
                            rentList.clear();
                            List<DocumentSnapshot> docList = task.getResult().getDocuments();
                            for (int i = 0; i < docList.size(); i++) {
                                DocumentSnapshot d = docList.get(i);
                                HistoryModel model = d.toObject(HistoryModel.class);
                                rentList.add(model);
                            }

                            if (rentList.isEmpty())
                                binding.errorRent.setVisibility(View.VISIBLE);
                            else
                                binding.errorRent.setVisibility(View.GONE);


                            if (!docList.isEmpty())
                                rentLastDoc = docList.get(docList.size() - 1);

                            rentListAdapter.submitList(rentList);
                            rentListAdapter.notifyDataSetChanged();
                            rentListAdapter.setRentStatusListener(new UtilsInterface.RentStatusListener() {
                                @Override
                                public void updateStatus(String status, HistoryModel data) {
                                    Calendar cal = Calendar.getInstance();
                                    Date date = cal.getTime();
                                    SimpleDateFormat spf = new SimpleDateFormat("dd-MM-yyyy, hh:mm:ss a", Locale.getDefault());
                                    String dateAndTime = spf.format(date);
                                    if (status.equalsIgnoreCase("reject")) {
                                        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this);
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
                                                                                    String ttlRentItem = (Long.parseLong(task.getResult().getString("totalRentItem")) - 1) + "";
                                                                                    String currentWalAmt = task.getResult().getString("wallet");
                                                                                    String newWalAmt = String.valueOf(Long.parseLong(currentWalAmt) + Long.parseLong(data.totalRentCost) + 2500);
                                                                                    HashMap<String, Object> updateMap = new HashMap<>();
                                                                                    updateMap.put("wallet", newWalAmt);
                                                                                    updateMap.put("totalRentItem", ttlRentItem);
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
                                                                                                        appClass.firestore.collection("transactions").add(map)
                                                                                                                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                                                                                    @Override
                                                                                                                    public void onComplete(@NonNull Task<DocumentReference> task) {
                                                                                                                        if (task.isSuccessful()) {
                                                                                                                            pDialog.dismiss();
                                                                                                                            getData();
                                                                                                                            Log.d(TAG, "onComplete: success wallet added");
                                                                                                                        } else {
                                                                                                                            pDialog.dismiss();
                                                                                                                            DialogCustoms.showSnackBar(MainActivity.this, task.getException().getMessage(), binding.getRoot());
                                                                                                                        }
                                                                                                                    }
                                                                                                                });
                                                                                                        Log.d(TAG, "onComplete: update wallet success");
                                                                                                    } else {
                                                                                                        pDialog.dismiss();
                                                                                                        DialogCustoms.showSnackBar(MainActivity.this, task.getException().getMessage(), binding.getRoot());
                                                                                                    }
                                                                                                }
                                                                                            });
                                                                                } else {
                                                                                    pDialog.dismiss();
                                                                                    DialogCustoms.showSnackBar(MainActivity.this, task.getException().getMessage(), binding.getRoot());
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
                                        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this);
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
                                                                                            long creditAmt = (Long.parseLong(data.totalRentCost) - (Long.parseLong(data.totalRentCost) * appClass.sharedPref.getPartnerRentCom()) / 100);
                                                                                            String newWalAmt = String.valueOf(Long.parseLong(currWalAmt) + creditAmt);
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
                                                                                                                map.put("amount", String.valueOf(creditAmt));
                                                                                                                map.put("date", dateAndTime);
                                                                                                                map.put("info", null);
                                                                                                                map.put("name", appClass.sharedPref.getUser().getName());
                                                                                                                map.put("status", "completed");
                                                                                                                map.put("type", "rent");
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
                                                                                                                                    getData();
                                                                                                                                    onActivityResult(101, RESULT_OK, null);
                                                                                                                                    DialogCustoms.showSnackBar(MainActivity.this, "Payment Credited Successfully", binding.getRoot());
                                                                                                                                    Log.d(TAG, "onComplete: success wallet added");
                                                                                                                                } else {
                                                                                                                                    pDialog.dismiss();
                                                                                                                                    DialogCustoms.showSnackBar(MainActivity.this, task.getException().getMessage(), binding.getRoot());
                                                                                                                                }
                                                                                                                            }
                                                                                                                        });
                                                                                                            } else {
                                                                                                                pDialog.dismiss();
                                                                                                                DialogCustoms.showSnackBar(MainActivity.this, task.getException().getMessage(), binding.getRoot());
                                                                                                            }
                                                                                                        }
                                                                                                    });

                                                                                            //return security deposit
                                                                                            appClass.firestore.collection("users").document(data.broRentalId)
                                                                                                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                                                            if (task.isSuccessful()) {
                                                                                                                String ttlRentItem = (Long.parseLong(task.getResult().getString("totalRentItem")) - 1) + "";
                                                                                                                String currentWalAmt = task.getResult().getString("wallet");
                                                                                                                String newWalAmt = String.valueOf(Long.parseLong(currentWalAmt) + 2500);
                                                                                                                HashMap<String, Object> updateMap = new HashMap<>();
                                                                                                                updateMap.put("wallet", newWalAmt);
                                                                                                                updateMap.put("totalRentItem", ttlRentItem);
                                                                                                                appClass.firestore.collection("users").document(data.broRentalId)
                                                                                                                        .update(updateMap).addOnCompleteListener(new OnCompleteListener<Void>() {
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
                                                                                                                Log.d(TAG, "onComplete: " + task.getException());
                                                                                                            }
                                                                                                        }
                                                                                                    });
                                                                                        } else {
                                                                                            pDialog.dismiss();
                                                                                            DialogCustoms.showSnackBar(MainActivity.this, task.getException().getMessage(), binding.getRoot());
                                                                                        }
                                                                                    }
                                                                                });
                                                                    } else if (status.equalsIgnoreCase("ongoing")) {
                                                                        pDialog.dismiss();
                                                                        getData();
                                                                    }
                                                                } else {
                                                                    pDialog.dismiss();
                                                                    Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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
                                        i.setData(Uri.parse("tel:" + appClass.sharedPref.getCustomerCareNum()));
                                        startActivity(i);
                                    }
                                }
                            });
                        } else {
                            Log.d(TAG, "onComplete: " + task.getException());
                        }
                    }
                });

        //ride history.
        appClass.firestore.collection("rideHistory")
                .whereEqualTo("status", "pending")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(10)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        binding.recyclerViewRide.setVisibility(View.VISIBLE);
                        ridePage = 0;
                        if (task.isSuccessful()) {
                            List<DocumentSnapshot> dList = task.getResult().getDocuments();
                            for (DocumentSnapshot d : dList) {
                                rideList.add(d.toObject(RideHistoryModel.class));
                            }

                            if (rideList.isEmpty())
                                binding.errorRide.setVisibility(View.VISIBLE);
                            else
                                binding.errorRide.setVisibility(View.GONE);

                            if (!dList.isEmpty())
                                rideLastDoc = dList.get(dList.size() - 1);

                            rideListAdapter.submitList(rideList);
                            rideListAdapter.notifyDataSetChanged();
                            rideListAdapter.addRefreshListeners(new UtilsInterface.RideHistoryListener() {
                                @Override
                                public void updateStatus(String status, String docId, int pos, RideHistoryModel data) {
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
                                                            DialogCustoms.showSnackBar(MainActivity.this, "Add bike number", binding.getRoot());
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
                                                                                                                                map.put("name", appClass.sharedPref.getUser().getName());
                                                                                                                                map.put("profileUrl", appClass.sharedPref.getUser().getName());
                                                                                                                                map.put("status", status);
                                                                                                                                appClass.firestore.collection("rideHistory")
                                                                                                                                        .document(docId)
                                                                                                                                        .update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                                            @Override
                                                                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                                                                pDialog.dismiss();
                                                                                                                                                if (task.isSuccessful()) {
                                                                                                                                                    if (status.equalsIgnoreCase("completed"))
                                                                                                                                                        updateTotalRides(data.getBroRentalId());
                                                                                                                                                    rideList.remove(pos);
                                                                                                                                                    rideListAdapter.submitList(rideList);
                                                                                                                                                    rideListAdapter.notifyDataSetChanged();
                                                                                                                                                } else {
                                                                                                                                                    DialogCustoms.showSnackBar(MainActivity.this, "Please try again", binding.getRoot());
                                                                                                                                                }
                                                                                                                                            }
                                                                                                                                        });
                                                                                                                            } else {
                                                                                                                                pDialog.dismiss();
                                                                                                                                DialogCustoms.showSnackBar(MainActivity.this, "Please try again", binding.getRoot());
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
                                                                                                        DialogCustoms.showSnackBar(MainActivity.this, "Please try again", binding.getRoot());
                                                                                                    }
                                                                                                }
                                                                                            });
                                                                                } else {
                                                                                    if (rideList.isEmpty()) {
                                                                                        getData();
                                                                                    } else {
                                                                                        rideList.remove(pos);
                                                                                        rideListAdapter.submitList(rideList);
                                                                                        rideListAdapter.notifyDataSetChanged();
                                                                                    }
                                                                                    DialogCustoms.showSnackBar(MainActivity.this, "Ride already accepted", binding.getRoot());
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
                        } else {
                            DialogCustoms.showSnackBar(MainActivity.this, "Please try again", binding.getRoot());
                        }
                    }
                });

        //get updated profile.
        getProfile(null);
    }

    private void updateTotalRides(String broRentalId) {
        appClass.firestore.collection("users").document(broRentalId)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            long totalRides = Long.parseLong(task.getResult().getString("totalRides"));
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("totalRides", String.valueOf(--totalRides));
                            appClass.firestore.collection("users").document(broRentalId)
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

    private void getProfile(androidx.appcompat.app.AlertDialog alertDialog) {
        pDialog.show();
        //profile update.
        appClass.firestore.collection("partners").document(appClass.sharedPref.getUser().getPin())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot d) {
                        pDialog.dismiss();
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
                        onActivityResult(101, RESULT_OK, null);
                        headerWalletTV.setText(Utility.rupeeIcon + appClass.sharedPref.getUser().getWallet());
                        headerNameTV.setText(appClass.sharedPref.getUser().getName());
                        Glide.with(MainActivity.this).load(appClass.sharedPref.getUser().getProfileUrl()).placeholder(R.drawable.default_profile).into(headerImageView);

                        if (alertDialog != null)
                            if (!appClass.sharedPref.getStatus().equalsIgnoreCase("pending"))
                                alertDialog.dismiss();
                            else
                                Toast.makeText(appClass, "Please complete KYC", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: " + e);
                    }
                });
    }

    private void openFragment(Fragment searchFragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fT = fm.beginTransaction();
        fT.replace(R.id.fragmentContainer, searchFragment);
        fT.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        fT.addToBackStack(null);
        fT.commit();
    }

    @Override
    public void onActivityResult(int reqCode, int resCode, Intent intent) {
        super.onActivityResult(reqCode, resCode, intent);
        switch (reqCode) {
            case 101:
                headerWalletTV.setText(Utility.rupeeIcon + appClass.sharedPref.getUser().getWallet());
                headerNameTV.setText(appClass.sharedPref.getUser().getName());
                Glide.with(this).load(appClass.sharedPref.getUser().getProfileUrl()).placeholder(R.drawable.default_profile).into(headerImageView);
                break;
            default:
                break;
        }
    }

    public void startActivityForRes(Intent i) {
        if (!isFinishing())
            startActivityForResult(i, 101);
    }
}