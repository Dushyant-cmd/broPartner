package com.brorental.bropartner.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.brorental.bropartner.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class ConnectionBroadcast extends BroadcastReceiver {
    private String TAG = "ConnectionBroadcast.java";
    private BottomSheetDialog sheet;
    private Context context;

    @Override
    public void onReceive(Context ctx, Intent data) {
        try {
            ConnectivityManager manager = (ConnectivityManager) ctx.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            NetworkInfo info2 = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            if (info != null && info2 != null && "android.net.conn.CONNECTIVITY_CHANGE".equals(data.getAction()))
                if (info.getState() != NetworkInfo.State.CONNECTED && info2.getState() != NetworkInfo.State.CONNECTED) {
                    sheet = new BottomSheetDialog(ctx);
                    View view = LayoutInflater.from(ctx).inflate(R.layout.connection_broadcast_sheet, null);
                    view.findViewById(R.id.try_againBtn).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sheet.dismiss();
                            ctx.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                        }
                    });
                    sheet.setCancelable(false);
                    sheet.setContentView(view);
                    sheet.show();
                } else {
                    if (sheet != null)
                        sheet.dismiss();
                }
        } catch (Exception e) {
            Log.e(TAG, "onReceive: " + e);
        }
    }
}
