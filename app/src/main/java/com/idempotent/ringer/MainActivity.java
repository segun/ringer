package com.idempotent.ringer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;

import com.idempotent.ringer.ui.graph.GraphActivity;
import com.idempotent.ringer.ui.login.LoginActivity;

public class MainActivity extends AppCompatActivity {
    private Button stopSoundButton;
    private boolean isSoundPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            Toolbar toolbar = findViewById(R.id.toolBar);
            setSupportActionBar(toolbar);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Start the foreground service
        startChargingToneService();

        // Setup stop sound button
        stopSoundButton = findViewById(R.id.stopSoundButton);
        stopSoundButton.setEnabled(false);
        stopSoundButton.setOnClickListener(v -> {
            Intent stopIntent = new Intent("STOP_SOUND");
            sendBroadcast(stopIntent);
            isSoundPlaying = false;
            stopSoundButton.setEnabled(false);
        });

        findViewById(R.id.showReportsButton).setOnClickListener(v -> {
            Intent intent = new Intent(this, GraphActivity.class);
            startActivity(intent);
        });
        // Register broadcast receiver
        IntentFilter filter = new IntentFilter("SOUND_PLAYING_STATUS");
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                isSoundPlaying = intent.getBooleanExtra("isPlaying", false);
                stopSoundButton.setEnabled(isSoundPlaying);
            }
        }, filter);
    }

    // Inflate the menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.main_menu, menu);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Handle menu item clicks
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Logout method
    private void logout() {
        // Clear login preference
        SharedPreferences preferences = getSharedPreferences("myPrefs", MODE_PRIVATE);
        preferences.edit().remove("isRegistered").apply();

        // Navigate to LoginActivity
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void startChargingToneService() {
        Intent serviceIntent = new Intent(this, ChargingToneService.class);
        startForegroundService(serviceIntent);
    }
}
