package com.brorental.bropartner.utilities;


import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;

import com.brorental.bropartner.R;

public class ProgressDialog {

    private ProgressDialog() {
    }

    public static AlertDialog createAlertDialog(Context context) {
       /* LayoutInflater inflater = activity.getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.custom_progress_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(dialogLayout);
        AlertDialog alert = builder.create();
        alert.show();
        alert.setCancelable(false);
        return alert;*/

        AlertDialog pDialog = new AlertDialog.Builder(context).create();
        LayoutInflater inflater = LayoutInflater.from(context);
        View convertView = inflater.inflate(R.layout.custom_progress_dialog, null);
        pDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        pDialog.setView(convertView);
        pDialog.setCancelable(false);
        return pDialog;
    }
}
