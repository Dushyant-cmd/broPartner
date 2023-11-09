package com.brorental.bropartner.models;

import java.util.Objects;

public class PaymentHistoryModel {
    public String advertId, amount, broPartnerId, broRentalId, date, info, name, type, status, id;
    public boolean isBroRental;
    public long timestamp;
    public PaymentHistoryModel(String advertId, String amount, String broPartnerId, String broRentalId, String date, String info, String name, String type, String status, boolean isBroRental, long timestamp, String id) {
        this.advertId = advertId;
        this.amount = amount;
        this.broPartnerId = broPartnerId;
        this.broRentalId = broRentalId;
        this.date = date;
        this.info = info;
        this.name = name;
        this.type = type;
        this.status = status;
        this.isBroRental = isBroRental;
        this.timestamp = timestamp;
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PaymentHistoryModel)) return false;
        PaymentHistoryModel that = (PaymentHistoryModel) o;
        return isBroRental == that.isBroRental && timestamp == that.timestamp && Objects.equals(advertId, that.advertId) && Objects.equals(amount, that.amount) && Objects.equals(broPartnerId, that.broPartnerId) && Objects.equals(broRentalId, that.broRentalId) && Objects.equals(date, that.date) && Objects.equals(info, that.info) && Objects.equals(name, that.name) && Objects.equals(type, that.type) && Objects.equals(status, that.status) && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(advertId, amount, broPartnerId, broRentalId, date, info, name, type, status, id, isBroRental, timestamp);
    }
}
