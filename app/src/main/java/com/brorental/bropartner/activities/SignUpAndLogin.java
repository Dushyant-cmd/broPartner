package com.brorental.bropartner.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.brorental.bropartner.R;
import com.brorental.bropartner.databinding.ActivityLoginBinding;
import com.brorental.bropartner.utilities.AppClass;
import com.brorental.bropartner.utilities.ErrorDialog;
import com.brorental.bropartner.utilities.ProgressDialog;
import com.brorental.bropartner.utilities.Utility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.regex.Pattern;

public class SignUpAndLogin extends AppCompatActivity {
    private String TAG = "LoginActivity.java";
    private ActivityLoginBinding binding;
    private AppClass appClass;
    private String phone, email;
    private boolean isRegistered;
    private AlertDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        appClass = (AppClass) getApplication();
        progressDialog = ProgressDialog.createAlertDialog(this);
        //REGISTER BROADCAST RECEIVER FOR INTERNET
        Utility.registerConnectivityBR(SignUpAndLogin.this, appClass);
        setListeners();
    }

    private void isUserRegistered() {
        progressDialog.show();
        appClass.firestore.collection("partners").whereEqualTo("mobile", phone).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                Log.d(TAG, "onComplete: " + task.getResult());
                progressDialog.dismiss();
                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                    isRegistered = true;
                } else if(task.isSuccessful()){
                    isRegistered = false;
                    binding.emailInputTextLy.setVisibility(View.VISIBLE);
                    binding.etEmail.requestFocus();
                } else {
                    ErrorDialog.createErrorDialog(SignUpAndLogin.this, task.getException().getMessage());
                }
            }
        });
    }

    public void setListeners() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                phone = charSequence.toString();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(phone.length() >= 10) {
                    isUserRegistered();
                } else if(phone.length() >= 1) {
                    binding.emailInputTextLy.setVisibility(View.GONE);
                }
            }
        };

        binding.eTMobileNumber.addTextChangedListener(watcher);

        binding.nextBtn.setOnClickListener(view -> {
            if(isRegistered) {
                Intent i = new Intent(this, OtpActivity.class);
                i.putExtra("phone", binding.eTMobileNumber.getText().toString());
                i.putExtra("email", email);
                i.putExtra("referCode", "234123");
                startActivity(i);
                binding.eTMobileNumber.setText("");
                binding.etEmail.setText("");
            } else {
                email = binding.etEmail.getText().toString();
                Pattern pattern = Pattern.compile("^([a-zA-Z0-9_\\-\\.]+)@([a-zA-Z0-9_\\-\\.]+)\\.([a-zA-Z]{2,5})$");
                if(pattern.matcher(email).matches()) {
                    Intent i = new Intent(this, OtpActivity.class);
                    i.putExtra("phone", binding.eTMobileNumber.getText().toString());
                    i.putExtra("email", email);
                    i.putExtra("referCode", "234123");
                    startActivity(i);
                    binding.eTMobileNumber.setText("");
                    binding.etEmail.setText("");
                    phone = "";
                    email = "";
                } else {
                    binding.etEmail.setError("Invalid");
                }
            }
        });
    }
}