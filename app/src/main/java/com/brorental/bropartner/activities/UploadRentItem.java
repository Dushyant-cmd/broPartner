package com.brorental.bropartner.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.brorental.bropartner.R;
import com.brorental.bropartner.databinding.ActivityUploadRentItemBinding;
import com.brorental.bropartner.utilities.DialogCustoms;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class UploadRentItem extends AppCompatActivity {
    private ActivityUploadRentItemBinding binding;
    private Context ctx;
    private Activity activity;
    private String TAG = "UploadRentItem.java";
    public String ownName, rcNum, bikeNum, aadhaarNum, pickupTimings = "Pickup Timings",
            perHourCharge, extraHourCharge, ownerDesc, pickUpLoc, productName, color;
    public int productYearPos, healthPos;
    private InputStream mFirstImg, mSecImg, mThirdImg;
    private boolean isImageUploaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate the layout for this fragment
        binding = DataBindingUtil.setContentView(this, R.layout.activity_upload_rent_item);
        ctx = this;
        activity = UploadRentItem.this;
        binding.setModel(UploadRentItem.this);
        setListeners();
    }

    private void setListeners() {
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.onBackPressed();
            }
        });

        binding.rlSelectPhoto.setOnClickListener(view -> {
            AlertDialog uploadDialog = DialogCustoms.getUploadDialog(ctx);
            LinearLayout uploadFileLayout = uploadDialog.findViewById(R.id.upload_file_layout);
            LinearLayout takePhotoLayout = uploadDialog.findViewById(R.id.take_photo_layout);

            uploadFileLayout.setOnClickListener(v1 ->
            {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                galleryContract.launch(intent);
                uploadDialog.dismiss();
            });

            takePhotoLayout.setOnClickListener(v1 -> {

                uploadDialog.dismiss();
            });
        });
    }

    /**
     * Below is Activity contract which is triggered when user comeback to app again just
     * like onActivityResult
     */
    private ActivityResultLauncher<Intent> galleryContract = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            try {
                Log.d(TAG, "onActivityResult: " + result.getData());
                if (result.getData() != null) {
                    if (result.getData().getClipData() != null) {
                        if (result.getData().getClipData().getItemCount() == 3) {
                            ClipData clipData = result.getData().getClipData();
                            Uri uri1 = clipData.getItemAt(0).getUri();
                            Uri uri2 = clipData.getItemAt(1).getUri();
                            Uri uri3 = clipData.getItemAt(2).getUri();
                            mFirstImg = getContentResolver().openInputStream(uri1);
                            mSecImg = getContentResolver().openInputStream(uri2);
                            mThirdImg = getContentResolver().openInputStream(uri3);
                            SlideModel model1 = new SlideModel(uri1.toString(), ScaleTypes.CENTER_INSIDE);
                            SlideModel model2 = new SlideModel(uri2.toString(), ScaleTypes.CENTER_INSIDE);
                            SlideModel model3 = new SlideModel(uri3.toString(), ScaleTypes.CENTER_INSIDE);
                            List<SlideModel> list = new ArrayList<>();
                            list.add(model1);
                            list.add(model2);
                            list.add(model3);
                            binding.slider.setImageList(list);
                            binding.imageTv.setVisibility(View.GONE);
                            binding.slider.setVisibility(View.VISIBLE);
                            binding.imageTv.setText("Document Selected");
                            isImageUploaded = true;
                        } else {
                            DialogCustoms.showSnackBar(activity, "Please select 3 product images.", binding.getRoot());
                        }
                    } else {
                        DialogCustoms.showSnackBar(activity, "Please select 3 product images.", binding.getRoot());
                    }
                } else
                    DialogCustoms.showSnackBar(activity, "Please select product images.", binding.getRoot());
            } catch (Exception e) {
                Log.d(TAG, "onActivityResult: " + e);
            }
        }
    });
}