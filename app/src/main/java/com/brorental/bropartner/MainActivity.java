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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.brorental.bropartner.databinding.ActivityMainBinding;
import com.brorental.bropartner.databinding.AuthPinDialogBinding;
import com.brorental.bropartner.fragments.SearchFragment;
import com.brorental.bropartner.interfaces.UtilsInterface;
import com.brorental.bropartner.models.HistoryModel;
import com.brorental.bropartner.utilities.AppClass;
import com.brorental.bropartner.utilities.DialogCustoms;
import com.brorental.bropartner.utilities.ProgressDialog;
import com.brorental.bropartner.utilities.Utility;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
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
    private ArrayList<HistoryModel> rentlist = new ArrayList<>();
    private FirebaseFirestore mFirestore;
    private AppClass appClass;
    private TextView headerWalletTV, viewProfileTV, headerNameTV;
    private ImageView headerImageView;
    private LinearLayout headerWalletLL;
    private RentHistoryAdapter rentListAdapter;
    private AlertDialog pDialog;
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
        setListeners();
        rentListAdapter = new RentHistoryAdapter(MainActivity.this);
        binding.recyclerViewRent.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        binding.recyclerViewRent.setAdapter(rentListAdapter);
        pDialog = ProgressDialog.createAlertDialog(MainActivity.this);
        //REGISTER BROADCAST RECEIVER FOR INTERNET
        Utility.registerConnectivityBR(MainActivity.this, appClass);
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
            Intent i = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(i);
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
           startActivity(i);
        });
        binding.withdrawalBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog sheet = new BottomSheetDialog(MainActivity.this);
                View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.add_cash_sheet, null);
                sheet.setContentView(view);
                Button submitBtn = view.findViewById(R.id.confirmRec);
                Button cancelBtn = view.findViewById(R.id.cancelRec);
                EditText rechargeET = view.findViewById(R.id.rechargeAmt);
                submitBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        Intent i = new Intent(MainActivity.this, PaymentActivity.class);
//                        i.putExtra("addCash", true);
//                        i.putExtra("amt", rechargeET.getText().toString());
//                        startActivityForRes(i);
//                        sheet.dismiss();
                    }
                });
                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sheet.dismiss();
                    }
                });
                sheet.show();
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
                    DialogCustoms.showSnackBar(MainActivity.this, "Terms & Conditions", binding.getRoot());
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
                .orderBy("timestamp", Query.Direction.DESCENDING).limit(6);
        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        binding.swipeRef.setRefreshing(false);
                        binding.shimmer.setVisibility(View.GONE);
                        binding.mainContentLl.setVisibility(View.VISIBLE);
                        if (task.isSuccessful()) {
                            rentlist.clear();
                            List<DocumentSnapshot> docList = task.getResult().getDocuments();
                            for (int i = 0; i < docList.size(); i++) {
                                DocumentSnapshot d = docList.get(i);
                                HistoryModel model = d.toObject(HistoryModel.class);
                                rentlist.add(model);
                            }

                            rentListAdapter.submitList(rentlist);
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
                                                                                                                                    getData();
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
                                        i.setData(Uri.parse("tel:" + "+919773602742"));
                                        startActivity(i);
                                    }
                                }
                            });
                            Log.d(TAG, "onComplete: " + docList.size());
                        } else {
                            Log.d(TAG, "onComplete: " + task.getException());
                        }
                    }
                });
    }

    private void openFragment(SearchFragment searchFragment) {
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
        Log.d(TAG, "onActivityResult: 44");
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