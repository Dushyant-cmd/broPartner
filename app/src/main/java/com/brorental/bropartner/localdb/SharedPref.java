package com.brorental.bropartner.localdb;

import android.content.Context;
import android.content.SharedPreferences;

import com.brorental.bropartner.models.User;

public class SharedPref {
    private SharedPreferences sharedPreferences;
    private String TAG = "SharedPreferences.java";

    public SharedPref(Context ctx) {
        this.sharedPreferences = ctx.getSharedPreferences(ctx.getPackageName(), Context.MODE_PRIVATE);
    }

    public boolean isFirstTime() {
        return sharedPreferences.getBoolean("isFirstTime", true);
    }

    public void setFirstTime(boolean isFirstTime) {
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putBoolean("isFirstTime", isFirstTime);
        edit.apply();
    }

    public boolean isLogin() {
        return sharedPreferences.getBoolean("isLogin", false);
    }

    public void setLogin(boolean isLogin) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLogin", isLogin);
        editor.apply();
    }

    public User getUser() {
        return new User(sharedPreferences.getString("name", ""), sharedPreferences.getString("mobile", "")
                , sharedPreferences.getString("pin", ""), sharedPreferences.getString("totalRent", ""),
                sharedPreferences.getString("totalRide", ""), sharedPreferences.getBoolean("termsCheck", false)
                , sharedPreferences.getString("profileUrl", ""), sharedPreferences.getString("wallet", ""));
    }

    public void saveUser(User user) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name", user.getName());
        editor.putString("mobile", user.getMobile());
        editor.putString("totalRent", user.getTotalRent());
        editor.putString("totalRide", user.getTotalRide());
        editor.putString("pin", user.getPin());
        editor.putBoolean("termsCheck", user.isTermsCheck());
        editor.putString("profileUrl", user.getProfileUrl());
        editor.putString("wallet", user.getWallet());
        editor.apply();
    }

    public void setEmail(String email) {
        sharedPreferences.edit().putString("email", email).apply();
    }

    public String getEmail() {
        return sharedPreferences.getString("email", "");
    }

    public void setName(String name) {
        sharedPreferences.edit().putString("name", name).apply();
    }

    public void setProfileUrl(String url) {
        sharedPreferences.edit().putString("profileUrl", url).apply();
    }

    public void setWallet(String wallet) {
        sharedPreferences.edit().putString("wallet", wallet).apply();
    }

    public void setState(String state) {
        sharedPreferences.edit().putString("state", state).apply();
    }

    public String getState() {
        return sharedPreferences.getString("state", "");
    }

    public String getAlternateMob() {
        return sharedPreferences.getString("alternateMob", "");
    }

    public void setAlternateMob(String mob) {
        sharedPreferences.edit().putString("alternateMob", mob).apply();
    }

    public String getAadhaarImg() {
        return sharedPreferences.getString("aadhaarImgUrl", "");
    }

    public void setAadhaarImg(String img) {
        sharedPreferences.edit().putString("aadhaarImgUrl", img).apply();
    }

    public String getDLImg() {
        return sharedPreferences.getString("dLicenseImg", "");
    }

    public void setDLImg(String img) {
        sharedPreferences.edit().putString("dLicenseImg", img).apply();
    }

    public String getProfilePath() {
        return sharedPreferences.getString("profileImgPath", "");
    }

    public void setProfilePath(String path) {
        sharedPreferences.edit().putString("profileImgPath", path).apply();
    }

    public String getAadhaarPath() {
        return sharedPreferences.getString("aadhaarImgPath", "");
    }

    public void setAadhaarPath(String path) {
        sharedPreferences.edit().putString("aadhaarImgPath", path).apply();
    }

    public String getDLPath() {
        return sharedPreferences.getString("dLicenseImgPath", "");
    }

    public void setDLPath(String path) {
        sharedPreferences.edit().putString("dLicenseImgPath", path).apply();
    }

    public String getPanImgPath() {
        return sharedPreferences.getString("panImgPath", "");
    }

    public void setPanImgPath(String path) {
        sharedPreferences.edit().putString("panImgPath", path).apply();
    }

    public String getPanImgUrl() {
        return sharedPreferences.getString("panImgUrl", "");
    }

    public void setPanImgUrl(String path) {
        sharedPreferences.edit().putString("panImgUrl", path).apply();
    }

    public String getStatus() {
        return sharedPreferences.getString("status", "");
    }

    public void setStatus(String status) {
        sharedPreferences.edit().putString("status", status).apply();
    }

    public void setAddress(String address) {
        sharedPreferences.edit().putString("address", address).apply();
    }

    public String getAddress() {
        return sharedPreferences.getString("address", "");
    }

    public String getRideBikeNum() {
        return sharedPreferences.getString("rideBikeNum", "");
    }

    public String setRideBikeNum() {
        return sharedPreferences.getString("rideBikeNum", "");
    }

    public long getPartnerRentCom() {
        return sharedPreferences.getLong("partnerRentCommission", 0L);
    }

    public void setPartnerRentCom(long partnerRentCom) {
        sharedPreferences.edit().putLong("partnerRentCommission", partnerRentCom).apply();
    }

    public long getPartnerRideCom() {
        return sharedPreferences.getLong("partnerRideCommission", 0L);
    }

    public void setPartnerRideCom(long partnerRideCom) {
        sharedPreferences.edit().putLong("partnerRideCommission", partnerRideCom).apply();
    }

    public String getCustomerCareNum() {
        return sharedPreferences.getString("customerCareNum", "");
    }

    public void setCustomerCareNum(String num) {
        sharedPreferences.edit().putString("customerCareNum", num).apply();
    }


    public void setBannerImage(String url) {
        sharedPreferences.edit().putString("bannerUrl", url).apply();
    }

    public String getBannerImage() {
        return sharedPreferences.getString("bannerUrl", "");
    }
    public void logout() {
        sharedPreferences.edit().clear().apply();
    }
}
