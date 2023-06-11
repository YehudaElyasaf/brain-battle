package com.example.trivia;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Observable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.ConditionVariable;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;


public class MusicService extends Service {

    private static final int ONGOING_NOTIFICATION_ID = 4242;
    private static final String MUSIC_NOTIFICATION_CHANNEL_ID = "MUSIC";
    private MediaPlayer mediaPlayer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //create player
        mediaPlayer = MediaPlayer.create(this, R.raw.bg_music);
        //play forever
        mediaPlayer.setLooping(true);

        //listen to volume changes
        SettingsFragment.backgroundMusicVolume = new MutableLiveData<>();
        SettingsFragment.backgroundMusicVolume.observeForever(new Observer<Float>() {
            @Override
            public void onChanged(Float aFloat) {
                mediaPlayer.setVolume(aFloat, aFloat);
            }
        });

        SettingsFragment.backgroundMusicVolume.setValue(1f);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //start playing
        mediaPlayer.start();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        //if SDK version is over 26, a notification channel is required
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "BackgroundMusic";
            NotificationChannel channel = new NotificationChannel(MUSIC_NOTIFICATION_CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Is playing music");
            notificationManager.createNotificationChannel(channel);
        }
        String channelId = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channelId = MUSIC_NOTIFICATION_CHANNEL_ID;
        }
        //add service notification
        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_play)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build();

        //start service
        startForeground(ONGOING_NOTIFICATION_ID, notification);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //stop service
        mediaPlayer.stop();
    }
}
