package com.brorental.bropartner.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.brorental.bropartner.R;
import com.brorental.bropartner.databinding.FragmentSliderBinding;

public class SliderFragment extends Fragment {
    private String TAG = "SliderFragment.java";
    private FragmentSliderBinding binding;
    public SliderFragment() {
        //required empty constructor.
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_slider, container, false);
        if(getArguments() != null) {
            binding.img.setImageResource((int) getArguments().getLong("image"));
            binding.title.setText(getArguments().getString("title"));
        }
        return binding.getRoot();
    }
}