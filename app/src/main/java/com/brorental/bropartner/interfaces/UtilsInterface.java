package com.brorental.bropartner.interfaces;

import androidx.appcompat.app.AlertDialog;

import com.brorental.bropartner.models.HistoryModel;
import com.brorental.bropartner.models.RideHistoryModel;

import java.util.HashMap;

public interface UtilsInterface {

    interface RefreshInterface {

        void refresh(int catePosition);
    }

    interface NoKycRefresh {

        void refresh(AlertDialog alertDialog);
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

    interface RideHistoryListener {
        void updateStatus(String status, String docId, int pos, RideHistoryModel data);
        void contactListener(String type);
    }
}


