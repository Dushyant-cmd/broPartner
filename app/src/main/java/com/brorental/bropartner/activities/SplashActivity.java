package com.brorental.bropartner.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.brorental.bropartner.MainActivity;
import com.brorental.bropartner.R;
import com.brorental.bropartner.databinding.ActivitySplashBinding;
import com.brorental.bropartner.models.User;
import com.brorental.bropartner.utilities.AppClass;
import com.brorental.bropartner.utilities.Utility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Locale;

public class SplashActivity extends AppCompatActivity {
    private String TAG = "SplashActivity.java";
    private ActivitySplashBinding binding;
    private AppClass appClass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(SplashActivity.this, R.layout.activity_splash);
        appClass = (AppClass) getApplication();
        setLocale("en");
        Utility.registerConnectivityBR(SplashActivity.this, appClass);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: " + appClass.sharedPref.isFirstTime());
                if(!appClass.sharedPref.isFirstTime()) {
                    if(!appClass.sharedPref.isLogin()) {
                        Intent i = new Intent(SplashActivity.this, SignUpAndLogin.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                        finish();
                    } else {
                        getProfile();
                    }
                } else {
                    appClass.sharedPref.setFirstTime(false);
                    Intent i = new Intent(SplashActivity.this, ScreenSliderActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    finish();
                }

                getBaseData();
            }
        }, 2000);
    }

    private void getBaseData() {
        appClass.firestore.collection("appData")
                .document("constants")
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()) {
                            DocumentSnapshot d = task.getResult();
                            long rentCom = d.getLong("partnerRentCommission");
                            long rideCom = d.getLong("partnerRideCommission");
                            String conNum = d.getString("customerCareNum");
                            String banner = d.getString("partnerBannerImgUrl");
                            appClass.sharedPref.setPartnerRentCom(rentCom);
                            appClass.sharedPref.setPartnerRideCom(rideCom);
                            appClass.sharedPref.setCustomerCareNum(conNum);
                            appClass.sharedPref.setBannerImage(banner);
                        } else {
                            Log.d(TAG, "onComplete: " + task.getException());
                        }
                    }
                });
    }

    private void getProfile() {
        Log.d(TAG, "getProfile: " + appClass.sharedPref.getUser().getPin());
        appClass.firestore.collection("partners").document(appClass.sharedPref.getUser().getPin())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot d) {
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
                        Intent i = new Intent(SplashActivity.this, MainActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: " + e);
                        Intent i = new Intent(SplashActivity.this, MainActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                        finish();
                    }
                });
    }

    public void setLocale(String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
    }
}