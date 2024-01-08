package com.brorental.bropartner.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import com.brorental.bropartner.R;
import com.brorental.bropartner.databinding.ActivityProfileBinding;
import com.brorental.bropartner.fragments.ProfileEditDetails;
import com.brorental.bropartner.interfaces.UtilsInterface;
import com.brorental.bropartner.utilities.AppClass;
import com.brorental.bropartner.utilities.Utility;
import com.bumptech.glide.Glide;

public class ProfileActivity extends AppCompatActivity {
    private ActivityProfileBinding binding;
    private String TAG = "ProfileActivity.java";
    private AppClass appClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile);
        appClass = (AppClass) getApplication();
        //REGISTER BROADCAST RECEIVER FOR INTERNET
        Utility.registerConnectivityBR(ProfileActivity.this, appClass);
        binding.nameTV.setText(appClass.sharedPref.getUser().getName());
        binding.pinTV.setText("Pin: " + appClass.sharedPref.getUser().getPin());
        binding.mobTV.setText("Mobile: " + appClass.sharedPref.getUser().getMobile());
        binding.altMobTV.setText("Alternate Mobile: " + appClass.sharedPref.getAlternateMob());
        binding.emailTv.setText("Email Id: " + appClass.sharedPref.getEmail());
        binding.stateTv.setText("State: " + appClass.sharedPref.getState());

        binding.addTv.setText("Address: " + appClass.sharedPref.getAddress());

        Glide.with(this).load(appClass.sharedPref.getUser().getProfileUrl()).placeholder(R.drawable.default_profile).into(binding.profileCirIV);
        Glide.with(this).load(appClass.sharedPref.getPanImgUrl()).placeholder(R.drawable.no_pictures).into(binding.panIv);
        Glide.with(this).load(appClass.sharedPref.getAadhaarImg()).placeholder(R.drawable.no_pictures).into(binding.aadhaarIv);
        setListeners();
    }

    private void setListeners() {
        binding.logoutTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                builder.setMessage("Are you sure to log-out");
                builder.setPositiveButton("Log-out", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(ProfileActivity.this, SignUpAndLogin.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                        finish();
                        dialog.dismiss();
                        appClass.sharedPref.logout();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builder.create().show();
            }
        });
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.editTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, new ProfileEditDetails(new UtilsInterface.RefreshInterface() {
                            @Override
                            public void refresh(int cate) {
                                binding.textImgLl.setVisibility(View.VISIBLE);
                                binding.nameTV.setText(appClass.sharedPref.getUser().getName());
                                binding.pinTV.setText("Pin: " + appClass.sharedPref.getUser().getPin());
                                binding.mobTV.setText("Mobile: " + appClass.sharedPref.getUser().getMobile());
                                binding.altMobTV.setText("Alternate Mobile: " + appClass.sharedPref.getAlternateMob());
                                binding.emailTv.setText("Email Id: " + appClass.sharedPref.getEmail());
                                binding.stateTv.setText("State: " + appClass.sharedPref.getState());
                                binding.addTv.setText("Address: " + appClass.sharedPref.getAddress());

                                Glide.with(ProfileActivity.this).load(appClass.sharedPref.getUser().getProfileUrl()).placeholder(R.drawable.default_profile).into(binding.profileCirIV);
                                Glide.with(ProfileActivity.this).load(appClass.sharedPref.getPanImgUrl()).placeholder(R.drawable.no_pictures).into(binding.panIv);
                                Glide.with(ProfileActivity.this).load(appClass.sharedPref.getAadhaarImg()).placeholder(R.drawable.no_pictures).into(binding.aadhaarIv);
                            }
                        }))
                        .addToBackStack(null)
                        .commit();

                binding.textImgLl.setVisibility(View.GONE);
            }
        });
    }
}