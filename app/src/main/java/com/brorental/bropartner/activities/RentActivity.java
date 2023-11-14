package com.brorental.bropartner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.brorental.bropartner.R;
import com.brorental.bropartner.databinding.ActivityRentBinding;
import com.brorental.bropartner.utilities.AppClass;
import com.brorental.bropartner.utilities.Utility;

public class RentActivity extends AppCompatActivity {
    private AppClass appClass;
    private ActivityRentBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_rent);
        appClass = (AppClass) getApplication();
        //REGISTER BROADCAST RECEIVER FOR INTERNET
        Utility.registerConnectivityBR(RentActivity.this, appClass);
        setListeners();
    }

    private void setListeners() {
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
}