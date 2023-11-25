package com.brorental.bropartner.models;

import java.util.Objects;

public class PointsHistoryModel {
    private String from, to, broPartnerId, docId;
    private boolean status;
    private long amount, distance, timestamp, totalRides;

    public PointsHistoryModel() {
    }

    public PointsHistoryModel(String from, String to, String broPartnerId, String docId, boolean status, long amount, long distance, long timestamp, long totalRides) {
        this.from = from;
        this.to = to;
        this.broPartnerId = broPartnerId;
        this.docId = docId;
        this.status = status;
        this.amount = amount;
        this.distance = distance;
        this.timestamp = timestamp;
        this.totalRides = totalRides;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PointsHistoryModel)) return false;
        PointsHistoryModel that = (PointsHistoryModel) o;
        return isStatus() == that.isStatus() && getAmount() == that.getAmount() && getDistance() == that.getDistance() && getTimestamp() == that.getTimestamp() && getTotalRides() == that.getTotalRides() && Objects.equals(getFrom(), that.getFrom()) && Objects.equals(getTo(), that.getTo()) && Objects.equals(getBroPartnerId(), that.getBroPartnerId()) && Objects.equals(getDocId(), that.getDocId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFrom(), getTo(), getBroPartnerId(), getDocId(), isStatus(), getAmount(), getDistance(), getTimestamp(), getTotalRides());
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

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTotalRides() {
        return totalRides;
    }

    public void setTotalRides(long totalRides) {
        this.totalRides = totalRides;
    }
}
