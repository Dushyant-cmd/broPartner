package com.brorental.bropartner.interfaces;

import com.brorental.bropartner.models.HistoryModel;

import java.util.HashMap;

public interface UtilsInterface {

    interface RefreshInterface {

        void refresh(int catePosition);
    }

    interface RentRefreshListener {
        void updateLiveStatus(boolean status, String docId, int pos);
        void refresh();
    }

    interface PointRefreshListener {
        void refresh(HashMap<String, Object> map);
    }

    interface RentStatusListener {
        void updateStatus(String status, HistoryModel data);
        void contactListener(String type);
    }
}


