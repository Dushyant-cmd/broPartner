package com.brorental.bropartner.activities;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.brorental.bropartner.R;
import com.brorental.bropartner.databinding.ActivityRideBinding;
import com.brorental.bropartner.interfaces.UtilsInterface;
import com.brorental.bropartner.utilities.AppClass;
import com.brorental.bropartner.utilities.DialogCustoms;
import com.brorental.bropartner.utilities.Utility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

public class RideActivity extends AppCompatActivity {
    private AppClass appClass;
    private ActivityRideBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_ride);
        appClass = (AppClass) getApplication();
        //REGISTER BROADCAST RECEIVER FOR INTERNET
        Utility.registerConnectivityBR(RideActivity.this, appClass);
        //Check if bikeRideNum is empty or not in broPartner document, if true then display a button
        //to add riding bike number, else display points if added else display a gif for no points found
        //with a button to add a point which opens a sheet.
        if(Utility.isNetworkAvailable(RideActivity.this)) {
            queries();
        } else {
            Utility.noNetworkDialog(RideActivity.this, new UtilsInterface.RefreshInterface() {
                @Override
                public void refresh(int catePosition) {
                    queries();
                }
            });
        }
        setListeners();
    }

    private void queries() {
        appClass.firestore.collection("partners")
                .document(appClass.sharedPref.getUser().getPin())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()) {
                            String bikeNum = task.getResult().getString("rideBikeNum");
                            if(bikeNum.isEmpty()) {
                                binding.addBikeNumBtn.setVisibility(View.VISIBLE);
                                binding.addPointBtn.setVisibility(View.GONE);
                                binding.noDataLy.setVisibility(View.GONE);
                            }
                        } else {
                            DialogCustoms.showSnackBar(RideActivity.this, task.getException().getMessage(), binding.getRoot());
                        }
                    }
                });
    }

    private void setListeners() {
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }
}