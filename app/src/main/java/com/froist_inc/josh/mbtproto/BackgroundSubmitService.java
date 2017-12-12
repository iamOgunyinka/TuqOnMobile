package com.froist_inc.josh.mbtproto;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class BackgroundSubmitService extends IntentService
{
    public static final String TAG = "BackgroundSubmitService";

    public BackgroundSubmitService()
    {
        super( TAG );
    }

    @Override
    protected void onHandleIntent( Intent intent )
    {
        ConnectivityManager connectivity_manager = ( ConnectivityManager )
                getSystemService(Context.CONNECTIVITY_SERVICE );
        NetworkInfo network_info = connectivity_manager.getActiveNetworkInfo();
        boolean is_network_available =  network_info != null && network_info.isConnected();
        if( !is_network_available ) {
            return;
        }
        Log.i( TAG, "Network is available" );
    }
}
