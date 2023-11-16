package com.brorental.bropartner.interfaces;

public interface UtilsInterface {

    interface RefreshInterface {

        void refresh(int catePosition);
    }

    interface RentRefreshListener {
        void updateLiveStatus(boolean status, String docId, int pos);
        void refresh();
    }
}


