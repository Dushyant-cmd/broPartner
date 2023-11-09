package com.brorental.bropartner.adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;

public class HistoryPagerAdapter extends FragmentStateAdapter {
    private ArrayList<Fragment> list = new ArrayList<>();
    public HistoryPagerAdapter(FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    public void addFragmentAndTitle(Fragment fragment) {
        list.add(fragment);
    }
    @Override
    public Fragment createFragment(int pos) {
        return list.get(pos);
    }
    @Override
    public int getItemCount() {
        return list.size();
    }
}
