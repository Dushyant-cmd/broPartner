package com.brorental.bropartner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.brorental.bropartner.R;
import com.brorental.bropartner.databinding.ActivityLoginBinding;
import com.brorental.bropartner.utilities.AppClass;
import com.brorental.bropartner.utilities.Utility;

import java.util.regex.Pattern;

public class SignUpAndLogin extends AppCompatActivity {
    private String TAG = "LoginActivity.java";
    private ActivityLoginBinding binding;
    private AppClass appClass;
    private String phone, email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        appClass = (AppClass) getApplication();
        //REGISTER BROADCAST RECEIVER FOR INTERNET
        Utility.registerConnectivityBR(SignUpAndLogin.this, appClass);
        setListeners();
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
                    binding.emailInputTextLy.setVisibility(View.VISIBLE);
                    binding.etEmail.requestFocus();
                } else if(phone.length() >= 1) {
                    binding.emailInputTextLy.setVisibility(View.GONE);
                }
            }
        };
        binding.eTMobileNumber.addTextChangedListener(watcher);
        binding.nextBtn.setOnClickListener(view -> {
            email = binding.etEmail.getText().toString();
            Pattern pattern = Pattern.compile("^([a-zA-Z0-9_\\-\\.]+)@([a-zA-Z0-9_\\-\\.]+)\\.([a-zA-Z]{2,5})$");
            if(pattern.matcher(email).matches()) {
                Intent i = new Intent(this, OtpActivity.class);
                i.putExtra("phone", binding.eTMobileNumber.getText().toString());
                i.putExtra("email", email);
                i.putExtra("referCode", "234123");
                startActivity(i);
            } else {
                binding.etEmail.setError("Invalid");
            }
        });
    }
}