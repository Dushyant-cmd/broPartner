package com.brorental.bropartner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.brorental.bropartner.R;
import com.brorental.bropartner.adapters.SliderAdapter;
import com.brorental.bropartner.databinding.ActivityScreenSliderBinding;
import com.brorental.bropartner.utilities.AppClass;
import com.brorental.bropartner.utilities.Utility;

public class ScreenSliderActivity extends AppCompatActivity {

    private String TAG = "SliderActivity.java";

    private ActivityScreenSliderBinding binding;
    private AppClass application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(ScreenSliderActivity.this, R.layout.activity_screen_slider);
        SliderAdapter adapter = new SliderAdapter(getSupportFragmentManager());
        application = (AppClass) getApplication();
        //REGISTER BROADCAST RECEIVER FOR INTERNET
        Utility.registerConnectivityBR(ScreenSliderActivity.this, application);
        adapter.addTitle("welcome to BroPartner");
        adapter.addTitle("We will try our best to give you a better experience");
        adapter.addTitle("You can rent everything you need on BroPartner at a best price nearby.");
        binding.viewPager.setAdapter(adapter);
        binding.dotsIndicator.attachTo(binding.viewPager);
        setListeners();
    }

    private void setListeners() {
        binding.skipBtn.setOnClickListener(view -> {
            application.sharedPref.setFirstTime(false);
            Intent i = new Intent(ScreenSliderActivity.this, SignUpAndLogin.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        });

        binding.nxtBtn.setOnClickListener(view -> {
            Log.d(TAG, "setListeners: " + binding.viewPager.getCurrentItem());
            if(binding.viewPager.getCurrentItem() == 0) {
                binding.prevBtn.setVisibility(View.VISIBLE);
                binding.viewPager.setCurrentItem(binding.viewPager.getCurrentItem() + 1, true);
                return;
            }
            if (binding.viewPager.getCurrentItem() > 0 && binding.viewPager.getCurrentItem() < 2)
                binding.viewPager.setCurrentItem(binding.viewPager.getCurrentItem() + 1, true);
            else {
                application.sharedPref.setFirstTime(false);
                Intent i = new Intent(ScreenSliderActivity.this, SignUpAndLogin.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        });

        binding.prevBtn.setOnClickListener(view -> {
            if (binding.viewPager.getCurrentItem() > 1)
                binding.viewPager.setCurrentItem(binding.viewPager.getCurrentItem() - 1, true);
            else {
                binding.viewPager.setCurrentItem(binding.viewPager.getCurrentItem() - 1, true);
                binding.prevBtn.setVisibility(View.GONE);
            }
        });
    }
}