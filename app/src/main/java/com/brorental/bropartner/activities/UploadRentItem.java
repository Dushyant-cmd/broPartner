package com.brorental.bropartner.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;

import com.brorental.bropartner.R;
import com.brorental.bropartner.databinding.ActivityUploadRentItemBinding;
import com.brorental.bropartner.localdb.RoomDb;
import com.brorental.bropartner.localdb.StateEntity;
import com.brorental.bropartner.retrofit.ApiService;
import com.brorental.bropartner.retrofit.RetrofitClient;
import com.brorental.bropartner.utilities.AppClass;
import com.brorental.bropartner.utilities.DialogCustoms;
import com.brorental.bropartner.utilities.ErrorDialog;
import com.brorental.bropartner.utilities.ProgressDialog;
import com.brorental.bropartner.utilities.Utility;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadRentItem extends AppCompatActivity {
    private ActivityUploadRentItemBinding binding;
    private Context ctx;
    private File photoFile;
    private ArrayList<File> capFileList = new ArrayList<>();
    private Activity activity;
    private String TAG = "UploadRentItem.java", currentPhotoPath, category;
    public String ownName = "", rcNum = "", bikeNum = "", aadhaarNum = "", pickupTimings = "Pickup Timings",
            perHourCharge = "", extraHourCharge = "", ownerDesc = "", pickUpLoc = "", productName = "", color = "", productYear = "";
    public int healthPos;
    private InputStream mFirstImg, mSecImg, mThirdImg;
    private boolean isImageUploaded = false, isCamera = false, isGallery = false;
    private Calendar cal;
    private SimpleDateFormat spf;
    private int REQUEST_IMAGE_CAPTURE = 1, REQUEST_PERM_CODE = 10;
    private boolean isPermissionGranted = false;
    private AppClass appClass;
    private RoomDb room;
    private List<String> stateList = new ArrayList<>();
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate the layout for this fragment
        binding = DataBindingUtil.setContentView(this, R.layout.activity_upload_rent_item);
        ctx = this;
        activity = UploadRentItem.this;
        binding.setModel(UploadRentItem.this);
        cal = Calendar.getInstance();
        appClass = (AppClass) getApplication();
        spf = new SimpleDateFormat("hh:mm", Locale.getDefault());
        room = RoomDb.getInstance(ctx);
        apiService = RetrofitClient.getInstance().create(ApiService.class);
        stateList.add("Select your state");
        ArrayList<String> list = new ArrayList<>();
        list.add("Select product health");
        list.add("Good");
        list.add("Excellent");
        list.add("Bad");
        list.add("Poor");
        ArrayAdapter<String> adapter = new
                ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, list);
        binding.spinnerHealth.setAdapter(adapter);
        getData();
        setListeners();
    }

    private void getData() {
        if (Utility.isNetworkAvailable(ctx)) {
            queries();
        } else {
            noNetworkDialog();
        }
    }

    private void noNetworkDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(activity);
        builder.setCancelable(false);
        builder.setMessage("No connection");
        builder.setPositiveButton("connect", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (Utility.isNetworkAvailable(activity)) {
                    queries();
                } else {
                    dialog.dismiss();
                    noNetworkDialog();
                }
            }
        });
        builder.create().show();
    }

    private void queries() {
        List<StateEntity> roomList = room.getStateDao().getStates();
        if (roomList.isEmpty()) {
            try {
                JSONObject json = new JSONObject();
                json.put("country", "india");
                RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), (json).toString());
                String url = "https://countriesnow.space/api/v0.1/countries/states";
                apiService.getCountryState(url, body)
                        .enqueue(new Callback<JsonObject>() {
                            @Override
                            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                if (response.isSuccessful()) {
                                    try {
                                        room.getStateDao().deleteStates();
                                        JSONObject json1 = new JSONObject(response.body().toString());
                                        JSONObject dataJs = json1.getJSONObject("data");
                                        JSONArray jsonArray1 = dataJs.getJSONArray("states");
                                        for (int i = 0; i < jsonArray1.length(); i++) {
                                            JSONObject js = jsonArray1.getJSONObject(i);
                                            room.getStateDao().insertState(new StateEntity(js.getString("name")));
                                            stateList.add(js.getString("name"));
                                        }

                                        ArrayAdapter<String> adapter = new ArrayAdapter<>(UploadRentItem.this, android.R.layout.simple_spinner_dropdown_item, stateList);
                                        binding.spinnerState.setAdapter(adapter);
                                    } catch (Exception e) {
                                        Log.d(TAG, "onResponse: " + e);
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<JsonObject> call, Throwable t) {
                                ErrorDialog.createErrorDialog(UploadRentItem.this, t.getMessage());
                            }
                        });
            } catch (Exception e) {
                Log.d(TAG, "getStates: " + e);
            }
        } else {
            for (StateEntity state : roomList) {
                stateList.add(state.getState());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_dropdown_item, stateList);
            binding.spinnerState.setAdapter(adapter);
            binding.spinnerState.setVisibility(View.VISIBLE);
        }

        appClass.firestore.collection("appData").document("constants")
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        try {
                            if (task.isSuccessful()) {
                                DocumentSnapshot d = task.getResult();
                                ArrayList<String> cateList = new ArrayList<>();
                                cateList.add("Select a category");
                                Collections.addAll(cateList, d.getString("categories").split(","));
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(UploadRentItem.this, android.R.layout.simple_spinner_dropdown_item, cateList);
                                binding.cateSpinner.setAdapter(adapter);
                                binding.cateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                        category = binding.cateSpinner.getSelectedItem().toString();
                                        if (!category.toLowerCase().contains("select")) {
                                            binding.formLl.setVisibility(View.VISIBLE);
                                            if (category.toLowerCase().matches("bike")) {
                                                binding.rcIl.setVisibility(View.VISIBLE);
                                                binding.bikeIl.setVisibility(View.VISIBLE);
                                                binding.aadhaarIl.setVisibility(View.VISIBLE);
                                            } else {
                                                binding.rcIl.setVisibility(View.GONE);
                                                binding.bikeIl.setVisibility(View.GONE);
                                                binding.aadhaarIl.setVisibility(View.GONE);
                                            }
                                        } else {
                                            binding.formLl.setVisibility(View.GONE);
                                        }
                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> adapterView) {

                                    }
                                });
                            } else {
                                Log.d(TAG, "onError: " + task.getException());
                            }
                        } catch (Exception e) {
                            Log.d(TAG, "onComplete: " + e);
                        }
                    }
                });
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
//        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
        // Create the File where the photo should go
        photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            // Error occurred while creating the File
            Log.d(TAG, "dispatchTakePictureIntent: " + ex);
        }
        // Continue only if the File was successfully created
        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(this,
                    "com.brorental.bropartner.provider",
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            contract.launch(takePictureIntent);
        }
//        }
    }

    private void setListeners() {
        binding.pickupTimingsTv.setOnClickListener(view -> {
            TimePickerDialog timeDialog1 = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                    pickupTimings = "From " + hour + ":" + minute;
                    TimePickerDialog timeDialog2 = new TimePickerDialog(ctx, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                            pickupTimings += " To " + hour + ":" + minute;
                            binding.pickupTimingsTv.setText(pickupTimings);
                        }
                    }, cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE), true);
                    timeDialog2.show();
                }
            }, cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE), true);
            timeDialog1.show();
        });

        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.onBackPressed();
            }
        });

        binding.rlSelectPhoto.setOnClickListener(view -> {
            if (checkPermissions()) {
                AlertDialog uploadDialog = DialogCustoms.getUploadDialog(ctx);
                LinearLayout uploadFileLayout = uploadDialog.findViewById(R.id.upload_file_layout);
                LinearLayout takePhotoLayout = uploadDialog.findViewById(R.id.take_photo_layout);

                uploadFileLayout.setOnClickListener(v1 ->
                {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    contract.launch(intent);
                    isCamera = false;
                    isGallery = true;
                    uploadDialog.dismiss();
                });

                takePhotoLayout.setOnClickListener(v1 -> {
                    if (checkPermissions()) {
                        dispatchTakePictureIntent();
                        isCamera = true;
                        isGallery = false;
                        uploadDialog.dismiss();
                    }
                });
            }
        });

        binding.uploadBtn.setOnClickListener(view -> {
            String health = binding.spinnerHealth.getSelectedItem().toString();
            String state = binding.spinnerState.getSelectedItem().toString();
            if (category.equalsIgnoreCase("bike")) {
                if (isImageUploaded && !ownName.isEmpty() && !rcNum.isEmpty() && !bikeNum.isEmpty() &&
                        !aadhaarNum.isEmpty() && !pickupTimings.toLowerCase().contains("pickup timings") &&
                        !perHourCharge.isEmpty() && !extraHourCharge.isEmpty() && !ownerDesc.isEmpty() &&
                        !productName.isEmpty() && !pickUpLoc.isEmpty() && !productYear.isEmpty() && !color.isEmpty()
                        && !health.toLowerCase().contains("select") && !state.toLowerCase().contains("select")) {
                    String firstImgPath = "productImages/" + UUID.randomUUID().toString();
                    String secImgPath = "productImages/" + UUID.randomUUID().toString();
                    String thirdImgPath = "productImages/" + UUID.randomUUID().toString();
                    StorageReference rootRef = appClass.storage.getReference();
                    StorageReference firstRef = rootRef.child(firstImgPath);
                    StorageReference secRef = rootRef.child(secImgPath);
                    StorageReference thirdRef = rootRef.child(thirdImgPath);
                    binding.uploadBtn.setEnabled(false);
                    if (Utility.isNetworkAvailable(ctx)) {
                        AlertDialog dialog = ProgressDialog.createAlertDialog(ctx);
                        dialog.show();
                        appClass.firestore.collection("ids").document("appid")
                                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            String rentId = task.getResult().getString("rentid");
                                            HashMap<String, Object> map = new HashMap<>();
                                            map.put("rentid", String.valueOf(Long.parseLong(rentId) + 1));
                                            appClass.firestore.collection("ids")
                                                    .document("appid")
                                                    .update(map)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            firstRef.putStream(mFirstImg).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                                                    if (task.isSuccessful()) {
                                                                        firstRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                                            @Override
                                                                            public void onSuccess(Uri uri) {
                                                                                String firstImgUrl = uri.toString();
                                                                                secRef.putStream(mSecImg).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                                                                        if (task.isSuccessful()) {
                                                                                            secRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                                                                @Override
                                                                                                public void onSuccess(Uri uri) {
                                                                                                    String secImgUrl = uri.toString();
                                                                                                    thirdRef.putStream(mThirdImg).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                                                                                            if (task.isSuccessful()) {
                                                                                                                thirdRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                                                                                    @Override
                                                                                                                    public void onSuccess(Uri uri) {
                                                                                                                        String thirdImgUrl = uri.toString();
                                                                                                                        HashMap<String, Object> map = new HashMap<>();
                                                                                                                        map.put("address", pickUpLoc);
                                                                                                                        map.put("adsImageUrl", firstImgUrl + "," + secImgUrl + "," + thirdImgUrl);
                                                                                                                        map.put("advertisementId", rentId);
                                                                                                                        map.put("broPartnerId", appClass.sharedPref.getUser().getPin());
                                                                                                                        map.put("broPartnerMobile", appClass.sharedPref.getUser().getMobile());
                                                                                                                        map.put("category", category.toLowerCase());
                                                                                                                        map.put("docId", rentId);
                                                                                                                        map.put("extraCharge", extraHourCharge);
                                                                                                                        map.put("name", productName);
                                                                                                                        map.put("ownerName", ownName);
                                                                                                                        map.put("ownerDescription", ownerDesc);
                                                                                                                        map.put("perHourCharge", perHourCharge);
                                                                                                                        map.put("productColor", color);
                                                                                                                        map.put("productHealth", health);
                                                                                                                        map.put("state", state);
                                                                                                                        map.put("status", "pending");
                                                                                                                        map.put("liveStatus", true);
                                                                                                                        map.put("timings", pickupTimings);
                                                                                                                        map.put("rcNumber", rcNum);
                                                                                                                        map.put("vehicleNumber", bikeNum);
                                                                                                                        map.put("ownerAadhaarNumber", aadhaarNum);
                                                                                                                        map.put("year", productYear);
                                                                                                                        map.put("timestamp", System.currentTimeMillis());
                                                                                                                        map.put("productImagePaths", firstImgPath + "," + secImgPath + "," + thirdImgPath);
                                                                                                                        appClass.firestore.collection("rent")
                                                                                                                                .document(rentId)
                                                                                                                                .set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                                    @Override
                                                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                                                        if (task.isSuccessful()) {
                                                                                                                                            getData();
                                                                                                                                            dialog.dismiss();
                                                                                                                                            Toast.makeText(UploadRentItem.this, "Add Uploaded Successfully", Toast.LENGTH_SHORT).show();
                                                                                                                                            onBackPressed();
                                                                                                                                        } else {
                                                                                                                                            binding.uploadBtn.setEnabled(true);
                                                                                                                                            dialog.dismiss();
                                                                                                                                            DialogCustoms.showSnackBar(ctx, task.getException().getMessage(), binding.getRoot());
                                                                                                                                            Log.d(TAG, "onComplete: " + task.isSuccessful());
                                                                                                                                        }
                                                                                                                                    }
                                                                                                                                });
                                                                                                                    }
                                                                                                                }).addOnFailureListener(new OnFailureListener() {
                                                                                                                    @Override
                                                                                                                    public void onFailure(@NonNull Exception e) {
                                                                                                                        binding.uploadBtn.setEnabled(true);
                                                                                                                        dialog.dismiss();
                                                                                                                    }
                                                                                                                });
                                                                                                            } else {
                                                                                                                binding.uploadBtn.setEnabled(true);
                                                                                                                dialog.dismiss();
                                                                                                                DialogCustoms.showSnackBar(ctx, task.getException().getMessage(), binding.getRoot());
                                                                                                                Log.d(TAG, "onComplete: " + task.isSuccessful());
                                                                                                            }
                                                                                                        }
                                                                                                    });
                                                                                                }
                                                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                                                @Override
                                                                                                public void onFailure(@NonNull Exception e) {
                                                                                                    binding.uploadBtn.setEnabled(true);
                                                                                                    dialog.dismiss();
                                                                                                }
                                                                                            });
                                                                                            ;
                                                                                        }
                                                                                    }
                                                                                });
                                                                            }
                                                                        }).addOnFailureListener(new OnFailureListener() {
                                                                            @Override
                                                                            public void onFailure(@NonNull Exception e) {
                                                                                binding.uploadBtn.setEnabled(true);
                                                                                dialog.dismiss();
                                                                            }
                                                                        });
                                                                        ;
                                                                    } else {
                                                                        binding.uploadBtn.setEnabled(true);
                                                                        dialog.dismiss();
                                                                        DialogCustoms.showSnackBar(ctx, task.getException().getMessage(), binding.getRoot());
                                                                        Log.d(TAG, "onComplete: " + task.isSuccessful());
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            binding.uploadBtn.setEnabled(true);
                                                            dialog.dismiss();
                                                        }
                                                    });
                                            ;
                                        } else {
                                            binding.uploadBtn.setEnabled(true);
                                            dialog.dismiss();
                                            DialogCustoms.showSnackBar(ctx, task.getException().getMessage(), binding.getRoot());
                                            Log.d(TAG, "onComplete: " + task.isSuccessful());
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        binding.uploadBtn.setEnabled(true);
                                        dialog.dismiss();
                                    }
                                });
                        ;
                    } else {
                        noNetworkDialog();
                    }
                } else {
                    DialogCustoms.showSnackBar(ctx, "Fill All Details", binding.getRoot());
                }
            } else {
                if (isImageUploaded && !ownName.isEmpty() && !pickupTimings.toLowerCase().contains("pickup timings") &&
                        !perHourCharge.isEmpty() && !extraHourCharge.isEmpty() && !ownerDesc.isEmpty() &&
                        !productName.isEmpty() && !pickUpLoc.isEmpty() && !productYear.isEmpty() && !color.isEmpty()
                        && !health.toLowerCase().contains("select") && !state.toLowerCase().contains("select")) {
                    String firstImgPath = "productImages/" + UUID.randomUUID().toString();
                    String secImgPath = "productImages/" + UUID.randomUUID().toString();
                    String thirdImgPath = "productImages/" + UUID.randomUUID().toString();
                    StorageReference rootRef = appClass.storage.getReference();
                    StorageReference firstRef = rootRef.child(firstImgPath);
                    StorageReference secRef = rootRef.child(secImgPath);
                    StorageReference thirdRef = rootRef.child(thirdImgPath);
                    binding.uploadBtn.setEnabled(false);
                    if (Utility.isNetworkAvailable(ctx)) {
                        AlertDialog dialog = ProgressDialog.createAlertDialog(ctx);
                        dialog.show();
                        appClass.firestore.collection("ids").document("appid")
                                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            String rentId = task.getResult().getString("rentid");
                                            HashMap<String, Object> map = new HashMap<>();
                                            map.put("rentid", String.valueOf(Long.parseLong(rentId) + 1));
                                            appClass.firestore.collection("ids")
                                                    .document("appid")
                                                    .update(map)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            firstRef.putStream(mFirstImg).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                                                    if (task.isSuccessful()) {
                                                                        firstRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                                            @Override
                                                                            public void onSuccess(Uri uri) {
                                                                                String firstImgUrl = uri.toString();
                                                                                secRef.putStream(mSecImg).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                                                                        if (task.isSuccessful()) {
                                                                                            secRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                                                                @Override
                                                                                                public void onSuccess(Uri uri) {
                                                                                                    String secImgUrl = uri.toString();
                                                                                                    thirdRef.putStream(mThirdImg).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                                                                                            if (task.isSuccessful()) {
                                                                                                                thirdRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                                                                                    @Override
                                                                                                                    public void onSuccess(Uri uri) {
                                                                                                                        String thirdImgUrl = uri.toString();
                                                                                                                        HashMap<String, Object> map = new HashMap<>();
                                                                                                                        map.put("address", pickUpLoc);
                                                                                                                        map.put("adsImageUrl", firstImgUrl + "," + secImgUrl + "," + thirdImgUrl);
                                                                                                                        map.put("advertisementId", rentId);
                                                                                                                        map.put("broPartnerId", appClass.sharedPref.getUser().getPin());
                                                                                                                        map.put("broPartnerMobile", appClass.sharedPref.getUser().getMobile());
                                                                                                                        map.put("category", category.toLowerCase());
                                                                                                                        map.put("docId", rentId);
                                                                                                                        map.put("extraCharge", extraHourCharge);
                                                                                                                        map.put("name", productName);
                                                                                                                        map.put("ownerName", ownName);
                                                                                                                        map.put("ownerDescription", ownerDesc);
                                                                                                                        map.put("perHourCharge", perHourCharge);
                                                                                                                        map.put("productColor", color);
                                                                                                                        map.put("productHealth", health);
                                                                                                                        map.put("state", state);
                                                                                                                        map.put("status", "pending");
                                                                                                                        map.put("liveStatus", true);
                                                                                                                        map.put("timestamp", System.currentTimeMillis());
                                                                                                                        map.put("timings", pickupTimings);
                                                                                                                        map.put("rcNumber", rcNum);
                                                                                                                        map.put("vehicleNumber", bikeNum);
                                                                                                                        map.put("ownerAadhaarNumber", aadhaarNum);
                                                                                                                        map.put("year", productYear);
                                                                                                                        map.put("productImagePaths", firstImgPath + "," + secImgPath + "," + thirdImgPath);
                                                                                                                        appClass.firestore.collection("rent")
                                                                                                                                .document(rentId)
                                                                                                                                .set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                                    @Override
                                                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                                                        if (task.isSuccessful()) {
                                                                                                                                            dialog.dismiss();
                                                                                                                                            Toast.makeText(UploadRentItem.this, "Add Uploaded Successfully", Toast.LENGTH_SHORT).show();
                                                                                                                                            onBackPressed();
                                                                                                                                        } else {
                                                                                                                                            binding.uploadBtn.setEnabled(true);
                                                                                                                                            dialog.dismiss();
                                                                                                                                            DialogCustoms.showSnackBar(ctx, task.getException().getMessage(), binding.getRoot());
                                                                                                                                            Log.d(TAG, "onComplete: " + task.isSuccessful());
                                                                                                                                        }
                                                                                                                                    }
                                                                                                                                });
                                                                                                                    }
                                                                                                                }).addOnFailureListener(new OnFailureListener() {
                                                                                                                    @Override
                                                                                                                    public void onFailure(@NonNull Exception e) {
                                                                                                                        binding.uploadBtn.setEnabled(true);
                                                                                                                        dialog.dismiss();
                                                                                                                    }
                                                                                                                });
                                                                                                            } else {
                                                                                                                binding.uploadBtn.setEnabled(true);
                                                                                                                dialog.dismiss();
                                                                                                                DialogCustoms.showSnackBar(ctx, task.getException().getMessage(), binding.getRoot());
                                                                                                                Log.d(TAG, "onComplete: " + task.isSuccessful());
                                                                                                            }
                                                                                                        }
                                                                                                    });
                                                                                                }
                                                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                                                @Override
                                                                                                public void onFailure(@NonNull Exception e) {
                                                                                                    binding.uploadBtn.setEnabled(true);
                                                                                                    dialog.dismiss();
                                                                                                }
                                                                                            });
                                                                                            ;
                                                                                        }
                                                                                    }
                                                                                });
                                                                            }
                                                                        }).addOnFailureListener(new OnFailureListener() {
                                                                            @Override
                                                                            public void onFailure(@NonNull Exception e) {
                                                                                binding.uploadBtn.setEnabled(true);
                                                                                dialog.dismiss();
                                                                            }
                                                                        });
                                                                        ;
                                                                    } else {
                                                                        binding.uploadBtn.setEnabled(true);
                                                                        dialog.dismiss();
                                                                        DialogCustoms.showSnackBar(ctx, task.getException().getMessage(), binding.getRoot());
                                                                        Log.d(TAG, "onComplete: " + task.isSuccessful());
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            binding.uploadBtn.setEnabled(true);
                                                            dialog.dismiss();
                                                        }
                                                    });
                                            ;
                                        } else {
                                            binding.uploadBtn.setEnabled(true);
                                            dialog.dismiss();
                                            DialogCustoms.showSnackBar(ctx, task.getException().getMessage(), binding.getRoot());
                                            Log.d(TAG, "onComplete: " + task.isSuccessful());
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        binding.uploadBtn.setEnabled(true);
                                        dialog.dismiss();
                                    }
                                });
                        ;
                    } else {
                        noNetworkDialog();
                    }
                } else {
                    DialogCustoms.showSnackBar(ctx, "Fill All Details", binding.getRoot());
                }
            }

        });
    }

    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_DENIED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_DENIED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_PERM_CODE);
                return false;
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_DENIED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_DENIED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_DENIED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERM_CODE);
                return false;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERM_CODE) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    isPermissionGranted = true;
                } else if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    isPermissionGranted = false;
                    break;
                }
            }

            if (!isPermissionGranted) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                        .setMessage("Please allow all permissions to continue")
                        .setTitle("Permission denied")
                        .setCancelable(false)
                        .setIcon(R.drawable.brorental_logo)
                        .setPositiveButton("Allow Permissions", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.fromParts("package", getPackageName(), null));
                                startActivity(intent);
                                dialogInterface.dismiss();
                            }
                        });
                builder.show();
            }
        }
    }

    /**
     * Below is Activity contract which is triggered when user comeback to app again just
     * like onActivityResult
     */
    private ActivityResultLauncher<Intent> contract = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            try {
                if (isGallery) {
                    Log.d(TAG, "onActivityResult contract: " + result.getData());
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
                                binding.imageTv.setText("Images Selected");
                                isImageUploaded = true;
                            } else {
                                DialogCustoms.showSnackBar(activity, "Please select 3 product images.", binding.getRoot());
                            }
                        } else {
                            DialogCustoms.showSnackBar(activity, "Please select 3 product images.", binding.getRoot());
                        }
                    } else
                        DialogCustoms.showSnackBar(activity, "Please select product images.", binding.getRoot());
                    isGallery = false;
                } else if (isCamera) {
                    if (REQUEST_IMAGE_CAPTURE == 3) {
                        InputStream stream = getContentResolver().openInputStream(Uri.fromFile(photoFile));
                        Bitmap bitmap = BitmapFactory.decodeStream(stream);
                        if (bitmap != null) {
                            capFileList.add(photoFile);
                        } else {
                            REQUEST_IMAGE_CAPTURE = 1;
                            capFileList.clear();
                            DialogCustoms.showSnackBar(ctx, "Please select 3 product images", binding.getRoot());
                            return;
                        }
                        REQUEST_IMAGE_CAPTURE = 1;
                        isCamera = false;
                        mFirstImg = getContentResolver().openInputStream(Uri.fromFile(capFileList.get(0)));
                        mSecImg = getContentResolver().openInputStream(Uri.fromFile(capFileList.get(1)));
                        mThirdImg = getContentResolver().openInputStream(Uri.fromFile(capFileList.get(2)));
                        ArrayList<SlideModel> list = new ArrayList<>();
                        for (File file : capFileList) {
                            list.add(new SlideModel(Uri.fromFile(file).toString(), ScaleTypes.CENTER_CROP));
                        }
                        binding.slider.setImageList(list);
                        binding.imageTv.setVisibility(View.GONE);
                        binding.slider.setVisibility(View.VISIBLE);
                        binding.imageTv.setText("Images Selected");
                        isImageUploaded = true;
                    } else {
                        InputStream stream = getContentResolver().openInputStream(Uri.fromFile(photoFile));
                        Bitmap bitmap = BitmapFactory.decodeStream(stream);
                        if (bitmap != null) {
                            capFileList.add(photoFile);
                        } else {
                            REQUEST_IMAGE_CAPTURE = 1;
                            capFileList.clear();
                            DialogCustoms.showSnackBar(ctx, "Please select 3 product images", binding.getRoot());
                            return;
                        }
                        REQUEST_IMAGE_CAPTURE++;
                        dispatchTakePictureIntent();
                    }
                }

            } catch (Exception e) {
                Log.d(TAG, "onActivityResult: " + e);
            }
        }
    });
}