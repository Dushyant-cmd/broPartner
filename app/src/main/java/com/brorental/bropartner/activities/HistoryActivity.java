package com.brorental.bropartner.activities;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.brorental.bropartner.MainActivity;
import com.brorental.bropartner.R;
import com.brorental.bropartner.adapters.HistoryPagerAdapter;
import com.brorental.bropartner.databinding.ActivityHistoryBinding;
import com.brorental.bropartner.fragments.RentHistoryFragment;
import com.brorental.bropartner.fragments.RideHistoryFragment;
import com.brorental.bropartner.utilities.AppClass;
import com.brorental.bropartner.utilities.Utility;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class HistoryActivity extends AppCompatActivity {
    private AppClass appClass;
    private ActivityHistoryBinding binding;
    private HistoryPagerAdapter viewPagerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_history);
        appClass = (AppClass) getApplication();
        //REGISTER BROADCAST RECEIVER FOR INTERNET
        Utility.registerConnectivityBR(HistoryActivity.this, appClass);
        viewPagerAdapter = new HistoryPagerAdapter(this);
        viewPagerAdapter.addFragmentAndTitle(new RentHistoryFragment());
        viewPagerAdapter.addFragmentAndTitle(new RideHistoryFragment());
        binding.viewPager.setAdapter(viewPagerAdapter);
        //ViewPager2 need a TabLayoutMediator instance where pass tabLayout, viewPager2, anonymous
        //class
        new TabLayoutMediator(binding.tabLayout, binding.viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(TabLayout.Tab tab, int position) {
                switch(position) {
                    case 0:
                        tab.setText("Rent History");
                        break;
                    case 1:
                        tab.setText("Ride History");
                        break;
                    default:
                        break;
                }
            }
        }).attach();

        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}