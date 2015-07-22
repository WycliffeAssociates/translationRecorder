package wycliffeassociates.recordingapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * Created by Abi on 7/22/2015.
 */
public class OpenExportApp
{

    // ShareIt package com.lenovo.anyshare.gps/com.lenovo.anyshare.ApMainActivity
    public void startNewActivity(Context context, String packageName) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent == null) {
            // Bring user to the market or let them choose an app?
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=" + packageName));
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
