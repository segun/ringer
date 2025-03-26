package com.idempotent.ringer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.IBinder;
import android.os.BatteryManager;
import android.os.PowerManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.idempotent.ringer.utils.ChargingStatusHelper;

public class ChargingToneService extends Service {
    private static final String CHANNEL_ID = "ChargingToneServiceChannel";
    private static final int NOTIFICATION_ID = 1;
    private PowerManager.WakeLock wakeLock;

    private SoundPool soundPool;
    private int chargingToneId;
    private int unplugToneId;
    private int currentStreamId = 0;
    private boolean wasPluggedIn = false;
    private AudioManager audioManager;
    private AudioFocusRequest audioFocusRequest;

    private BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
                int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                boolean isPluggedIn = chargePlug == BatteryManager.BATTERY_PLUGGED_AC
                        || chargePlug == BatteryManager.BATTERY_PLUGGED_USB
                        || chargePlug == BatteryManager.BATTERY_PLUGGED_WIRELESS;

                SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                String userLocation = sharedPreferences.getString("userLocation", "Unknown");
                boolean manualLocation = sharedPreferences.getBoolean("manualLocation", false);
                String userId = sharedPreferences.getString("userId", null);

                if (userId != null) {
                    if (isPluggedIn && !wasPluggedIn) {
                        playSound(chargingToneId);
                        ChargingStatusHelper.sendChargingStatusToServer(userId, true, userLocation, manualLocation);
                    } else if (!isPluggedIn && wasPluggedIn) {
                        // playSound(unplugToneId);
                        ChargingStatusHelper.sendChargingStatusToServer(userId, false, userLocation, manualLocation);
                    }
                } else {
                    Log.e("olu-ringer", "User ID not found in SharedPreferences");
                }

                wasPluggedIn = isPluggedIn;
            }
        }
    };

    private void broadcastSoundStatus(boolean isPlaying) {
        Intent intent = new Intent("SOUND_PLAYING_STATUS");
        intent.putExtra("isPlaying", isPlaying);
        sendBroadcast(intent);
    }

    private final BroadcastReceiver stopSoundReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (currentStreamId != 0) {
                soundPool.stop(currentStreamId);
                currentStreamId = 0;
                broadcastSoundStatus(false);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification());

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(2)
                .setAudioAttributes(audioAttributes)
                .build();

        chargingToneId = soundPool.load(this, R.raw.off, 1);
        unplugToneId = soundPool.load(this, R.raw.on, 1);

        IntentFilter batteryFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver, batteryFilter);

        IntentFilter stopFilter = new IntentFilter("STOP_SOUND");
        registerReceiver(stopSoundReceiver, stopFilter);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                    .setAudioAttributes(audioAttributes)
                    .build();
        }
    }

    private void playSound(int soundId) {
        acquireWakeLock();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.requestAudioFocus(audioFocusRequest);
        } else {
            audioManager.requestAudioFocus(null, AudioManager.STREAM_NOTIFICATION, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
        }
        currentStreamId = soundPool.play(soundId, 1, 1, 0, 0, 1);
        broadcastSoundStatus(true);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                if (status == 0) {
                    releaseWakeLock();
                } else {
                    Log.e("olu-ringer", "Error loading sound");
                    releaseWakeLock();
                }
            }
        });
    }

    private void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    private void acquireWakeLock() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE,
                "ChargingToneService::WakeLock"
        );
        wakeLock.acquire();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Charging Tone Service",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private Notification createNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Charging Tone Service")
                .setContentText("Monitoring battery status")
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseWakeLock();
        unregisterReceiver(batteryReceiver);
        unregisterReceiver(stopSoundReceiver);
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.abandonAudioFocusRequest(audioFocusRequest);
        } else {
            audioManager.abandonAudioFocus(null);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}