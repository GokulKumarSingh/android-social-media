package com.example.firechat.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;

import com.example.firechat.services.BackgroundService;

public class MyReceiver extends BroadcastReceiver {

    MediaPlayer mp;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("tag3333", "onReceive: reciver started");

        context.startService(new Intent(context, BackgroundService.class));
//        mp = MediaPlayer.create(context, Settings.System.DEFAULT_RINGTONE_URI);
//        mp.start();
    }

}
