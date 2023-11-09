package com.brorental.bropartner.models;

public class User {

    private String name, mobile, pin, totalRent, totalRide, profileUrl, wallet;
    private boolean termsCheck;

    public User(String name, String mobile, String pin, String totalRent, String totalRide, boolean termsCheck, String profileUrl, String wallet) {
        this.name = name;
        this.mobile = mobile;
        this.pin = pin;
        this.totalRent = totalRent;
        this.totalRide = totalRide;
        this.termsCheck = termsCheck;
        this.profileUrl = profileUrl;
        this.wallet = wallet;
    }

    public String getWallet() {
        return wallet;
    }

    public void setWallet(String wallet) {
        this.wallet = wallet;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getTotalRent() {
        return totalRent;
    }

    public void setTotalRent(String totalRent) {
        this.totalRent = totalRent;
    }

    public String getTotalRide() {
        return totalRide;
    }

    public void setTotalRide(String totalRide) {
        this.totalRide = totalRide;
    }

    public boolean isTermsCheck() {
        return termsCheck;
    }

    public void setTermsCheck(boolean termsCheck) {
        this.termsCheck = termsCheck;
    }
}
