package com.brorental.bropartner.utilities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;

import com.brorental.bropartner.R;
import com.brorental.bropartner.activities.SignUpAndLogin;
import com.brorental.bropartner.localdb.SharedPref;

public class ErrorDialog {

    public static AlertDialog createErrorDialog(Activity activity, String msg) {
        final AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
        LayoutInflater inflater = activity.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.error_view, null);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView showErrorTT = convertView.findViewById(R.id.showErrorTT);
        TextView okayBtn = convertView.findViewById(R.id.okayBtn);
        ImageView iv_close = convertView.findViewById(R.id.iv_close);
        okayBtn.setOnClickListener(v -> alertDialog.dismiss());
        iv_close.setOnClickListener(v -> alertDialog.dismiss());
        showErrorTT.setText(msg);
        alertDialog.setView(convertView);
        alertDialog.setCancelable(false);
        return alertDialog;
    }

    public static AlertDialog logoutDialog(Context context, Activity activity, String msg) {
        final AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
        LayoutInflater inflater = activity.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.error_view, null);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView showErrorTT = convertView.findViewById(R.id.showErrorTT);
        TextView okayBtn = convertView.findViewById(R.id.okayBtn);

        okayBtn.setOnClickListener(v ->
        {
            alertDialog.dismiss();
            activity.startActivity(new Intent(activity, SignUpAndLogin.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
            activity.finish();
            SharedPref sharedPref = new SharedPref(activity);
            sharedPref.logout();
        });

        showErrorTT.setText(msg);
        alertDialog.setView(convertView);
        alertDialog.setCancelable(false);
        return alertDialog;
    }

    public static AlertDialog createErrorDialogFromContext(Context context, String msg) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        LayoutInflater inflater = LayoutInflater.from(context);
        View convertView = inflater.inflate(R.layout.error_view, null);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView showErrorTT = convertView.findViewById(R.id.showErrorTT);
        ImageView ivClose = convertView.findViewById(R.id.iv_close);
        ivClose.setOnClickListener(v -> alertDialog.dismiss());
        TextView okayBtn = convertView.findViewById(R.id.okayBtn);
        okayBtn.setOnClickListener(v -> alertDialog.dismiss());
        showErrorTT.setText(msg);
        alertDialog.setView(convertView);
        alertDialog.setCancelable(false);
        return alertDialog;
    }

//    public static AlertDialog somethingWentWrongDialog(Activity activity) {
//        final AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
//        LayoutInflater inflater = activity.getLayoutInflater();
//        View convertView = inflater.inflate(R.layout.something_went_wrong, null);
//        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        AppCompatButton refreshButton = convertView.findViewById(R.id.refreshButton);
//        refreshButton.setOnClickListener(v -> alertDialog.dismiss());
//        alertDialog.setView(convertView);
//        alertDialog.setCancelable(false);
//        alertDialog.show();
//        return alertDialog;
//    }


}
