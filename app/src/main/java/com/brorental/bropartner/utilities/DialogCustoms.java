package com.brorental.bropartner.utilities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.brorental.bropartner.R;
import com.google.android.material.snackbar.Snackbar;

public class DialogCustoms {
    private static String TAG = "DialogCustoms.java";
    /**Below method will display a snackbar */
    public static void showSnackBar(Context ctx, String msg, View root) {
        try {
//            Log.d(TAG, "showSnackBar: " + msg);
            Snackbar bar = Snackbar.make(root, msg, Snackbar.LENGTH_SHORT);
            bar.setAction("Contact-Us", view -> {
                Intent i = new Intent(Intent.ACTION_DIAL);
                i.setData(Uri.parse("tel:" + "+919773602742"));
                ctx.startActivity(i);
            });
            bar.show();
        } catch (Exception e) {
            Log.d(TAG, "showSnackBar: " + e);
        }
    }

    public static AlertDialog getUploadDialog(Context context) {
        android.app.AlertDialog uploadDialog = new android.app.AlertDialog.Builder(context).create();
        LayoutInflater inflater = LayoutInflater.from(context);
        View convertView = inflater.inflate(R.layout.upload_select_dialog, null);
        uploadDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        uploadDialog.setView(convertView);
        uploadDialog.setCancelable(false);
        uploadDialog.show();

        ImageView imgClose = convertView.findViewById(R.id.img_close);

        imgClose.setOnClickListener(v1 -> {
            uploadDialog.dismiss();
        });

        return uploadDialog;
    }
}
