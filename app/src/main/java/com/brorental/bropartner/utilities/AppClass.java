package com.brorental.bropartner.utilities;

import android.app.Application;

import com.brorental.bropartner.broadcasts.ConnectionBroadcast;
import com.brorental.bropartner.localdb.SharedPref;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

public class AppClass extends Application {
    public SharedPref sharedPref;
    public FirebaseFirestore firestore;
    public FirebaseStorage storage;
    public ConnectionBroadcast broadcast;

    @Override
    public void onCreate() {
        super.onCreate();
        initialize();
    }

    private void initialize() {
        sharedPref = new SharedPref(getApplicationContext());
        firestore = FirebaseFirestore.getInstance();
//        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder(firestore.getFirestoreSettings())
//                .setPersistenceEnabled(false)
//                .build();
//        firestore.setFirestoreSettings(settings);
        storage = FirebaseStorage.getInstance();
        broadcast = new ConnectionBroadcast();
    }
}
