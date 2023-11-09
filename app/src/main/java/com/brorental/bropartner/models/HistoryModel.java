package com.brorental.bropartner.models;

import java.util.Objects;

public class HistoryModel {
    public String advertisementId, broPartnerId, broRentalId, extraCharge, id, name, paymentMode, perHourCharge, rentEndTime, rentImages, rentStartTime, status, totalHours, totalRentCost, category, address, broPartnerMobile, broRentalMobile;
    long timestamp;

    public HistoryModel() {
        //mandatory if you deserializing
    }

    public HistoryModel(String advertisementId, String broPartnerId, String broRentalId, String extraCharge, String id, String name, String paymentMode, String perHourCharge, String rentEndTime, String rentImages, String rentStartTime, String status, String totalHours, String totalRentCost, String category, String address, String broPartnerMobile, String broRentalMobile, long timestamp) {
        this.advertisementId = advertisementId;
        this.broPartnerId = broPartnerId;
        this.broRentalId = broRentalId;
        this.extraCharge = extraCharge;
        this.id = id;
        this.name = name;
        this.paymentMode = paymentMode;
        this.perHourCharge = perHourCharge;
        this.rentEndTime = rentEndTime;
        this.rentImages = rentImages;
        this.rentStartTime = rentStartTime;
        this.status = status;
        this.totalHours = totalHours;
        this.totalRentCost = totalRentCost;
        this.category = category;
        this.address = address;
        this.broPartnerMobile = broPartnerMobile;
        this.broRentalMobile = broRentalMobile;
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HistoryModel that = (HistoryModel) o;
        return timestamp == that.timestamp && Objects.equals(advertisementId, that.advertisementId) && Objects.equals(broPartnerId, that.broPartnerId) && Objects.equals(broRentalId, that.broRentalId) && Objects.equals(extraCharge, that.extraCharge) && Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(paymentMode, that.paymentMode) && Objects.equals(perHourCharge, that.perHourCharge) && Objects.equals(rentEndTime, that.rentEndTime) && Objects.equals(rentImages, that.rentImages) && Objects.equals(rentStartTime, that.rentStartTime) && Objects.equals(status, that.status) && Objects.equals(totalHours, that.totalHours) && Objects.equals(totalRentCost, that.totalRentCost);
    }

    @Override
    public int hashCode() {
        return Objects.hash(advertisementId, broPartnerId, broRentalId, extraCharge, id, name, paymentMode, perHourCharge, rentEndTime, rentImages, rentStartTime, status, totalHours, totalRentCost, timestamp);
    }

    @Override
    public String toString() {
        return "HistoryModel{" +
                "advertisementId='" + advertisementId + '\'' +
                ", broPartnerId='" + broPartnerId + '\'' +
                ", broRentalId='" + broRentalId + '\'' +
                ", extraCharge='" + extraCharge + '\'' +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", paymentMode='" + paymentMode + '\'' +
                ", perHourCharge='" + perHourCharge + '\'' +
                ", rentEndTime='" + rentEndTime + '\'' +
                ", rentImages='" + rentImages + '\'' +
                ", rentStartTime='" + rentStartTime + '\'' +
                ", status='" + status + '\'' +
                ", totalHours='" + totalHours + '\'' +
                ", totalRentCost='" + totalRentCost + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
