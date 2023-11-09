package com.brorental.bropartner.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.brorental.bropartner.MainActivity;
import com.brorental.bropartner.R;
import com.brorental.bropartner.databinding.ActivityOtpBinding;
import com.brorental.bropartner.localdb.SharedPref;
import com.brorental.bropartner.models.User;
import com.brorental.bropartner.utilities.AppClass;
import com.brorental.bropartner.utilities.DialogCustoms;
import com.brorental.bropartner.utilities.Utility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class OtpActivity extends AppCompatActivity {

    //Firebase Authentication product or library java class in android
    private FirebaseAuth mAuth;
    //verificationId store verificationId returns from firebase auth when otp sent successfully
    private String mVerificationId;
    private String name, pin, totalRide, totalRent, profileUrl, profileImgPath, wallet, aadhaarImgUrl, aadhaarImgPath, dLicenseImgUrl, dLicenseImgPath, status, email;
    private boolean termsCheck;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    String phone;
    String TAG = "OtpActivity.java", referCode;
    ProgressDialog dialog;
    Thread t;
    FirebaseFirestore mFirestore;
    SharedPref sharedPreferences;
    private ActivityOtpBinding binding;
    private AppClass appClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_otp);
        appClass = (AppClass) getApplication();
        //REGISTER BROADCAST RECEIVER FOR INTERNET
        Utility.registerConnectivityBR(OtpActivity.this, appClass);
        mAuth = FirebaseAuth.getInstance();
        Intent i = getIntent();
        phone = "+91 " + i.getStringExtra("phone");
        email = i.getStringExtra("email");
        referCode = i.getStringExtra("referCode");
        binding.phone2.setText(phone);
        binding.phone2.setVisibility(View.VISIBLE);
        binding.login.setEnabled(false);
        sharedPreferences = new SharedPref(OtpActivity.this);
        dialog = new ProgressDialog(OtpActivity.this);
        mFirestore = FirebaseFirestore.getInstance();

        //set the TextWatcher interface instance on otp pinView
        binding.otp.addTextChangedListener(mTextWatcher);
        //Send otp code of user number comes from previous activity by Intent
        if (isNetworkConnected()) {
            sendOtp(phone);
            resendCountFunction();
        } else {
            openAlertDialog1();
        }

        //set OnClickListener and verify otp when user click on login button
        //if the automatic sms detection did not work, user can also enter the code manually
        //so adding a click listener to the button
        binding.login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = Objects.requireNonNull(binding.otp.getText()).toString().trim();
                if (code.isEmpty() || code.length() < 6) {
                    binding.otp.setError("Enter valid code");
                    binding.otp.requestFocus();
                    return;
                }
                dialog.setMessage("Please wait...");
                //verifying the code entered manually
                verifyVerificationCode(code);
            }
        });

        binding.resendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.resendOTPTV.setVisibility(View.GONE);
                binding.resendOtp.setVisibility(View.GONE);
                resendCountFunction();
                Toast.makeText(OtpActivity.this, "Resending OTP", Toast.LENGTH_SHORT).show();
                sendOtp(phone);
            }
        });

        ArrayList<String> list = new ArrayList<>();
        list.add("Select a Language");
        list.add("English");
        list.add("Hindi");
        ArrayAdapter adapter = new ArrayAdapter<>(OtpActivity.this, android.R.layout.simple_spinner_dropdown_item, list);
        binding.langSpinner.setAdapter(adapter);
        binding.submitBtn.setOnClickListener(view -> {
            try {
                Log.d(TAG, "onCreate: " + sharedPreferences.getUser().getPin());
                if (binding.termsCb.isChecked() && !binding.langSpinner.getSelectedItem().toString().contains("Select")) {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("termsCheck", true);
                    map.put("language", binding.langSpinner.getSelectedItem());
                    mFirestore.collection("partners")
                            .document(pin)
                            .update(map)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Intent i = new Intent(OtpActivity.this, MainActivity.class);
                                        i.putExtra("phone", phone);//with +91 code in phone variable.
                                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(i);
                                        finish();
                                        sharedPreferences.setLogin(true);
                                        sharedPreferences.setAadhaarImg(aadhaarImgUrl);
                                        sharedPreferences.setAadhaarPath(aadhaarImgPath);
                                        sharedPreferences.setDLImg(dLicenseImgUrl);
                                        sharedPreferences.setDLPath(dLicenseImgPath);
                                        sharedPreferences.setProfilePath(profileImgPath);
                                        sharedPreferences.setStatus(status);
                                        sharedPreferences.setEmail(email);
                                        sharedPreferences.saveUser(new User(name, phone, pin, totalRent,
                                                totalRide, true, profileUrl, wallet));
                                        Log.d(TAG, "onComplete: " + task.getResult());
                                    } else {
                                        Log.d(TAG, "onComplete: " + task.getException());
                                    }
                                }
                            });
                } else {
                    DialogCustoms.showSnackBar(this, "Please select Terms & Condition's and language", binding.getRoot());
                }
            } catch (Exception e) {
                Log.d(TAG, "onCreate: " + e);
                sharedPreferences.logout();
            }
        });
    }

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            //Do some work before text changed
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            //Doing some work when text is changing
            if (binding.otp.getText().toString().length() == 6) {
                binding.login.setEnabled(true);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
            //Do some work after text changed
        }
    };

    //resendCount() method is start a timer and when the timer is up do certain operation
    private void resendCountFunction() {
        binding.otpTimer.setVisibility(View.VISIBLE);
        t = new Thread() {
            public void run() {
                for (int i = 0; i < 30; i++) {
                    try {
                        final int a = i;
                        runOnUiThread(new Runnable() {
                            public void run() {
                                if (a == 29) {
                                    binding.otpTimer.setVisibility(View.GONE);
                                    binding.resendOtp.setVisibility(View.VISIBLE);
                                    binding.resendOTPTV.setVisibility(View.VISIBLE);
                                    t.interrupt();
                                }
                                binding.otpTimer.setText("Resend Code in " + (30 - (a + 1)));
                            }
                        });
                        //   System.out.println("Value of i= " + i);
                        sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        t.start();

    }

    private void sendOtp(String phone) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phone)       // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(this)                 // Activity (for callback binding)
                .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        //onVerificationCompleted method called by android when the verification of otp is verified inside this method we get sms and auto
        //verify the user by calling
        @Override
        public void onVerificationCompleted(PhoneAuthCredential credential) {
            Log.d(TAG, "onVerificationCompleted:" + credential);
            //Getting the code sent by SMS
            String code = credential.getSmsCode();
            Log.v("OtpActivity.java", code + "");
            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            if (code != null) {
                dialog.setMessage("Wait while generating account");
                dialog.show();
                binding.otp.setText(code);
//              verifying the code
                verifyVerificationCode(code);
            } else {
                binding.login.setEnabled(true);
            }
        }

        //when verification of user otp is failed or send otp is failed
        @Override
        public void onVerificationFailed(FirebaseException e) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            Log.w(TAG, "onVerificationFailed", e);
            if (dialog.isShowing()) dialog.dismiss();
            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                Toast.makeText(OtpActivity.this, "Invalid request", Toast.LENGTH_SHORT).show();
            } else if (e instanceof FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                Toast.makeText(OtpActivity.this, "Otp limits exceeded", Toast.LENGTH_SHORT).show();
            }
        }

        //when firebase successfully sent otp to user phone number and return verification id of otp and token
        @Override
        public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            Log.d(TAG, "onCodeSent:" + verificationId);
            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            Toast.makeText(OtpActivity.this, "Otp sent on " + phone + " successfully", Toast.LENGTH_SHORT).show();
            // Save verification ID and resending token so we can use them later
            mVerificationId = verificationId;
            mResendToken = token;
        }
    };

    //This method is create PhoneAuthCredential class object by pass verification id and otp then call signInWithPhoneAuthCredential() method
    //and pass credential(PhoneAuthCredential class object) in order to sign-in user
    private void verifyVerificationCode(String code) {
        //Get credential of user enter otp like code and verificationId
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);

        //sign in user with phone auth credential on firebase using below method
        signInWithPhoneAuthCredential(credential);
    }

    //this method called signInWithCredential() method with credential input param in it on FirebaseAuth class instance.
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success");

//                            FirebaseUser user = task.getResult().getUser();
//                            assert user != null;
////                            Log.v(TAG, user.getPhoneNumber() + "");
                    if (isNetworkConnected()) {
                        isUserExist(phone);
                    } else {
                        openAlertDialog();
                    }
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.getException());
                    if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        Toast.makeText(OtpActivity.this, "OTP is invalid", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void openAlertDialog1() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(OtpActivity.this);
        builder.setMessage("Check network connection!");
        builder.setCancelable(false);
        builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (isNetworkConnected()) {
                    Toast.makeText(getApplicationContext(), "Network connected successfully", Toast.LENGTH_SHORT).show();
                    sendOtp(phone);
                    resendCountFunction();
                } else {
                    openAlertDialog1();
                }
            }
        });
        builder.create().show();//create() method returns AlertDialog class object which is subclass of Dialog class and show() to display
        //AlertDialog
    }

    private void openAlertDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(OtpActivity.this);
        builder.setMessage("Check network connection!");
        builder.setCancelable(false);
        builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (isNetworkConnected()) {
                    isUserExist(phone);
                    Toast.makeText(getApplicationContext(), "Network connected successfully", Toast.LENGTH_SHORT).show();
                } else {
                    openAlertDialog();
                }
            }
        });
        builder.create().show();//create() method returns AlertDialog class object which is subclass of Dialog class and show() to display
        //AlertDialog
    }

    private void isUserExist(String phone) {
        dialog.show();
        mFirestore.collection("partners").whereEqualTo("mobile", phone).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                Log.d(TAG, "onComplete: " + task.getResult());
                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                    DocumentSnapshot d = task.getResult().getDocuments().get(0);
                    if (d.getBoolean("termsCheck")) {
                        //login
                        Intent i = new Intent(OtpActivity.this, MainActivity.class);
                        i.putExtra("phone", phone);//with +91 code in phone variable.
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                        finish();
                        dialog.dismiss();
                        sharedPreferences.setLogin(true);
                        sharedPreferences.setAadhaarImg(d.getString("aadhaarImgUrl"));
                        sharedPreferences.setAadhaarPath(d.getString("aadhaarImgPath"));
                        sharedPreferences.setDLImg(d.getString("drivingLicenseImg"));
                        sharedPreferences.setDLPath(d.getString("drivingLicImgPath"));
                        sharedPreferences.setProfilePath(d.getString("profileImgPath"));
                        sharedPreferences.setStatus(d.getString("status"));
                        sharedPreferences.setEmail(d.getString("email"));
                        sharedPreferences.saveUser(new User(d.getString("name"), phone, d.getString("pin"),
                                d.getString("totalRent"),
                                d.getString("totalRide"), true, d.getString("profileUrl"), d.getString("wallet")));
                    } else {
                        dialog.dismiss();
                        name = d.getString("name");
                        pin = d.getString("pin");
                        totalRent = d.getString("totalRent");
                        totalRide = d.getString("totalRide");
                        termsCheck = d.getBoolean("termsCheck");
                        profileUrl = d.getString("profileUrl");
                        profileImgPath = d.getString("profileImgPath");
                        wallet = d.getString("wallet");
                        aadhaarImgUrl = d.getString("aadhaarImgUrl");
                        aadhaarImgPath = d.getString("aadhaarImgPath");
                        dLicenseImgUrl = d.getString("drivingLicenseImg");
                        dLicenseImgPath = d.getString("drivingLicImgPath");
                        status = d.getString("status");
                        email = d.getString("email");
                        binding.otpLl.setVisibility(View.GONE);
                        binding.termsLangLL.setVisibility(View.VISIBLE);
                    }
                } else if (task.isSuccessful()) {
                    try {
                        //sign-up
                        mFirestore.collection("ids").document("pins")
                                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            pin = task.getResult().getString("broPartnerPin");
                                            HashMap<String, Object> map = new HashMap<>();
                                            map.put("broPartnerPin", "" + Long.parseLong(pin) + 1);
                                            mFirestore.collection("ids").document("pins")
                                                    .update(map)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            String username = "partner_" + pin;
                                                            HashMap<String, Object> map2 = new HashMap<>();
                                                            map2.put("name", username);
                                                            map2.put("mobile", phone);
                                                            map2.put("pin", pin);
                                                            map2.put("totalRentItem", "0");
                                                            map2.put("totalRides", "0");
                                                            map2.put("termsCheck", false);
                                                            map2.put("profileUrl", "");
                                                            map2.put("profileImgPath", "");
                                                            map2.put("aadhaarImgUrl", "");
                                                            map2.put("aadhaarImgPath", "");
                                                            map2.put("drivingLicenseImg", "");
                                                            map2.put("drivingLicImgPath", "");
                                                            map2.put("status", "pending");
                                                            mFirestore.collection("users")
                                                                    .document(pin).set(map2).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void unused) {
//                                                                            Intent i = new Intent(OtpActivity.this, MainActivity.class);
//                                                                            i.putExtra("phone", phone);//with +91 code in phone variable.
//                                                                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                                                            startActivity(i);
//                                                                            finish();
                                                                            name = username;
                                                                            dialog.dismiss();
//                                                                            sharedPreferences.setLogin(true);
//                                                                            sharedPreferences.setAadhaarImg("");
//                                                                            sharedPreferences.setAadhaarPath("");
//                                                                            sharedPreferences.setDLImg("");
//                                                                            sharedPreferences.setDLPath("");
//                                                                            sharedPreferences.setProfilePath("");
//                                                                            sharedPreferences.setStatus("pending");
//                                                                            sharedPreferences.setEmail("");
//                                                                            sharedPreferences.saveUser(new User(username, phone, pin, "0", "0", false, "", "0"));
                                                                            binding.otpLl.setVisibility(View.GONE);
                                                                            binding.otpLl.setVisibility(View.VISIBLE);
                                                                            Toast.makeText(OtpActivity.this, "Sign-Up Successfully", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    }).addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            Log.d(TAG, "onFailure: " + t);
                                                                        }
                                                                    });
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.d(TAG, "onFailure: " + t);
                                                        }
                                                    });
                                        } else {
                                            Toast.makeText(OtpActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            Log.d(TAG, "onComplete: " + task.getException());
                                        }
                                    }
                                });
                    } catch (Exception e) {
                        Log.v("OtpActivity.java", e + "");
                    }
                } else {
                }
            }
        });
    }

    //check mobile device is connected to network or not.
    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean connected = (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED || connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED);
        return connected;
    }
}