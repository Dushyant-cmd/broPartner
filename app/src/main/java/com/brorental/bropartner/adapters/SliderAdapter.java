package com.brorental.bropartner.adapters;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.brorental.bropartner.R;
import com.brorental.bropartner.fragments.SliderFragment;

import java.util.ArrayList;

public class SliderAdapter extends FragmentPagerAdapter {
    private Integer[] imageArr = {R.drawable.brorental_logo, R.drawable.brorental_logo,
            R.drawable.brorental_logo};
    private ArrayList<String> titleList = new ArrayList<>();

    public SliderAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        Bundle bundle = new Bundle();
        bundle.putLong("image", imageArr[position]);
        bundle.putString("title", titleList.get(position));
        SliderFragment fragment = new SliderFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getCount() {
        return titleList.size();
    }

    public void addTitle(String title) {
        titleList.add(title);
    }
}
