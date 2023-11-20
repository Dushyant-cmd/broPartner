package com.brorental.bropartner.interfaces;

import com.brorental.bropartner.models.HistoryModel;

public interface UtilsInterface {

    interface RefreshInterface {

        void refresh(int catePosition);
    }

    interface RentRefreshListener {
        void updateLiveStatus(boolean status, String docId, int pos);
        void refresh();
    }

    interface RentStatusListener {
        void updateStatus(String status, HistoryModel data);
        void contactListener(String type);
    }
}


