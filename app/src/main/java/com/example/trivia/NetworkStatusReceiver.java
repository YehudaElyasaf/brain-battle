package com.example.trivia;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

public class NetworkStatusReceiver extends BroadcastReceiver {
    private ImageView networkStatusImg;

    public NetworkStatusReceiver(ImageView networkStatusImg) {
        this.networkStatusImg = networkStatusImg;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        NetworkInfo networkInfo = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            //has network connection
            networkStatusImg.setVisibility(View.GONE);
        } else {
            //no network connection
            networkStatusImg.setVisibility(View.VISIBLE);
        }
    }
}
