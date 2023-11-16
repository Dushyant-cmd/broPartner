package com.brorental.bropartner.models;

import java.util.Objects;

public class RentItemModel {
    public String advertisementId;
    private String address, adsImageUrl, broPartnerId, category, docId, extraCharge,
            ownerDescription, perHourCharge, state, status, vehicleNumber, timings,
            year, productHealth, productColor, name, broPartnerMobile;
    private boolean liveStatus;

    public RentItemModel() {
        //Mandatory for deserialization
    }

    public RentItemModel(String advertisementId, String address, String adsImageUrl, String broPartnerId, String category, String docId, String extraCharge, String ownerDescription, String perHourCharge, String state, String status, String vehicleNumber, String timings, String year, String productHealth, String productColor, String name, String broPartnerMobile, boolean liveStatus) {
        this.advertisementId = advertisementId;
        this.address = address;
        this.adsImageUrl = adsImageUrl;
        this.broPartnerId = broPartnerId;
        this.category = category;
        this.docId = docId;
        this.extraCharge = extraCharge;
        this.ownerDescription = ownerDescription;
        this.perHourCharge = perHourCharge;
        this.state = state;
        this.status = status;
        this.vehicleNumber = vehicleNumber;
        this.timings = timings;
        this.year = year;
        this.productHealth = productHealth;
        this.productColor = productColor;
        this.name = name;
        this.broPartnerMobile = broPartnerMobile;
        this.liveStatus = liveStatus;
    }

    public boolean getLiveStatus() {
        return liveStatus;
    }

    public void setLiveStatus(boolean liveStatus) {
        this.liveStatus = liveStatus;
    }

    public String getAdvertisementId() {
        return advertisementId;
    }

    public void setAdvertisementId(String advertisementId) {
        this.advertisementId = advertisementId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAdsImageUrl() {
        return adsImageUrl;
    }

    public void setAdsImageUrl(String adsImageUrl) {
        this.adsImageUrl = adsImageUrl;
    }

    public String getBroPartnerId() {
        return broPartnerId;
    }

    public void setBroPartnerId(String broPartnerId) {
        this.broPartnerId = broPartnerId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getExtraCharge() {
        return extraCharge;
    }

    public void setExtraCharge(String extraCharge) {
        this.extraCharge = extraCharge;
    }

    public String getOwnerDescription() {
        return ownerDescription;
    }

    public void setOwnerDescription(String ownerDescription) {
        this.ownerDescription = ownerDescription;
    }

    public String getPerHourCharge() {
        return perHourCharge;
    }

    public void setPerHourCharge(String perHourCharge) {
        this.perHourCharge = perHourCharge;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getTimings() {
        return timings;
    }

    public void setTimings(String timings) {
        this.timings = timings;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getProductHealth() {
        return productHealth;
    }

    public void setProductHealth(String productHealth) {
        this.productHealth = productHealth;
    }

    public String getProductColor() {
        return productColor;
    }

    public void setProductColor(String productColor) {
        this.productColor = productColor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBroPartnerMobile() {
        return broPartnerMobile;
    }

    public void setBroPartnerMobile(String broPartnerMobile) {
        this.broPartnerMobile = broPartnerMobile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RentItemModel that = (RentItemModel) o;
        return Objects.equals(advertisementId, that.advertisementId) && Objects.equals(address, that.address) && Objects.equals(adsImageUrl, that.adsImageUrl) && Objects.equals(broPartnerId, that.broPartnerId) && Objects.equals(category, that.category) && Objects.equals(docId, that.docId) && Objects.equals(extraCharge, that.extraCharge) && Objects.equals(ownerDescription, that.ownerDescription) && Objects.equals(perHourCharge, that.perHourCharge) && Objects.equals(state, that.state) && Objects.equals(status, that.status) && Objects.equals(vehicleNumber, that.vehicleNumber) && Objects.equals(timings, that.timings) && Objects.equals(year, that.year) && Objects.equals(productHealth, that.productHealth) && Objects.equals(productColor, that.productColor) && Objects.equals(name, that.name) && Objects.equals(broPartnerMobile, that.broPartnerMobile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(advertisementId, address, adsImageUrl, broPartnerId, category, docId, extraCharge, ownerDescription, perHourCharge, state, status, vehicleNumber, timings, year, productHealth, productColor, name, broPartnerMobile);
    }

    @Override
    public String toString() {
        return "RentItemModel{" +
                "advertisementId='" + advertisementId + '\'' +
                ", address='" + address + '\'' +
                ", adsImageUrl='" + adsImageUrl + '\'' +
                ", broPartnerId='" + broPartnerId + '\'' +
                ", category='" + category + '\'' +
                ", docId='" + docId + '\'' +
                ", extraCharge='" + extraCharge + '\'' +
                ", ownerDescription='" + ownerDescription + '\'' +
                ", perHourCharge='" + perHourCharge + '\'' +
                ", state='" + state + '\'' +
                ", status='" + status + '\'' +
                ", vehicleNumber='" + vehicleNumber + '\'' +
                ", timings='" + timings + '\'' +
                ", year='" + year + '\'' +
                ", productHealth='" + productHealth + '\'' +
                ", productColor='" + productColor + '\'' +
                ", name='" + name + '\'' +
                ", broPartnerMobile='" + broPartnerMobile + '\'' +
                '}';
    }
}
