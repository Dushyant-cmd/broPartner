package com.brorental.bropartner.models;

import java.util.Objects;

public class RideHistoryModel {
    private String from, to, broPartnerId, broRentalId, paymentMode, status, profileUrl, name, docId, pin, broRentalNumber;
    private long amount, distance, startTimestamp, endTimestamp, timestamp, rideId;

    public RideHistoryModel(String from, String to, String broPartnerId, String broRentalId, String paymentMode, String status, String profileUrl, String name, String docId, String pin, String broRentalNumber, long amount, long distance, long startTimestamp, long endTimestamp, long timestamp, long rideId) {
        this.from = from;
        this.to = to;
        this.broPartnerId = broPartnerId;
        this.broRentalId = broRentalId;
        this.paymentMode = paymentMode;
        this.status = status;
        this.profileUrl = profileUrl;
        this.name = name;
        this.docId = docId;
        this.pin = pin;
        this.broRentalNumber = broRentalNumber;
        this.amount = amount;
        this.distance = distance;
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
        this.timestamp = timestamp;
        this.rideId = rideId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RideHistoryModel)) return false;
        RideHistoryModel that = (RideHistoryModel) o;
        return getAmount() == that.getAmount() && getDistance() == that.getDistance() && getStartTimestamp() == that.getStartTimestamp() && getEndTimestamp() == that.getEndTimestamp() && getTimestamp() == that.getTimestamp() && getRideId() == that.getRideId() && Objects.equals(getFrom(), that.getFrom()) && Objects.equals(getTo(), that.getTo()) && Objects.equals(getBroPartnerId(), that.getBroPartnerId()) && Objects.equals(getBroRentalId(), that.getBroRentalId()) && Objects.equals(getPaymentMode(), that.getPaymentMode()) && Objects.equals(getStatus(), that.getStatus()) && Objects.equals(getProfileUrl(), that.getProfileUrl()) && Objects.equals(getName(), that.getName()) && Objects.equals(getDocId(), that.getDocId()) && Objects.equals(getPin(), that.getPin()) && Objects.equals(getBroRentalNumber(), that.getBroRentalNumber());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFrom(), getTo(), getBroPartnerId(), getBroRentalId(), getPaymentMode(), getStatus(), getProfileUrl(), getName(), getDocId(), getPin(), getBroRentalNumber(), getAmount(), getDistance(), getStartTimestamp(), getEndTimestamp(), getTimestamp(), getRideId());
    }

    public String getBroRentalNumber() {
        return broRentalNumber;
    }

    public void setBroRentalNumber(String broRentalNumber) {
        this.broRentalNumber = broRentalNumber;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
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

    public long getRideId() {
        return rideId;
    }

    public void setRideId(long rideId) {
        this.rideId = rideId;
    }

    public RideHistoryModel() {
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getBroPartnerId() {
        return broPartnerId;
    }

    public void setBroPartnerId(String broPartnerId) {
        this.broPartnerId = broPartnerId;
    }

    public String getBroRentalId() {
        return broRentalId;
    }

    public void setBroRentalId(String broRentalId) {
        this.broRentalId = broRentalId;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public long getDistance() {
        return distance;
    }

    public void setDistance(long distance) {
        this.distance = distance;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public long getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
