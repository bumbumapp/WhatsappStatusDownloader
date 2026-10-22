package com.bumbumapps.statusdownloader.activity;

import com.bumbumapps.statusdownloader.utils.StatusStorage;
import com.bumbumapps.statusdownloader.utils.Config;
import androidx.annotation.NonNull;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.bumbumapps.statusdownloader.R;

import java.lang.reflect.Method;




public class SplashActivity extends Activity {

    private static int SPLASH_TIME_OUT = 1000;
    private static int SPLASH_TIME = 4000;
    Context context;
    Button allow;
    boolean isCheck = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        allow=findViewById(R.id.allow);
        if (StatusStorage.usesTreeAccess()) {
            // Android 11+: statuses live in Android/media and are read through a folder grant (SAF),
            // instead of the "All files access" permission that Play does not allow for this app.
            if (StatusStorage.hasAccess(this, Config.WhatsAppDirectoryPath)) {
                startActivity();
            } else {
                allow.setVisibility(View.VISIBLE);
                allow.setOnClickListener(v -> {
                    try {
                        startActivityForResult(StatusStorage.requestAccessIntent(Config.WhatsAppDirectoryPath), REQUEST_TREE);
                    } catch (RuntimeException e) {
                        startActivity();
                    }
                });
            }
        } else if (hasLegacyPermission()) {
            startActivity();
        } else {
            requestLegacyPermission();
        }
    }

    private static final int REQUEST_STORAGE = 100;
    private static final int REQUEST_TREE = 200;

    private boolean hasLegacyPermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLegacyPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE);
    }

    public void startActivity() {
        handlerSplash();
    }

    public void handlerSplash() {
        allow.setVisibility(View.GONE);
        new Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            if (!isFinishing()) {
                startActivity(new Intent(SplashActivity.this, DrawerActivity.class));
                finish();
            }
        }, SPLASH_TIME_OUT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Was "case 1" while the request used code 100, so granting left the user stuck on the splash.
        if (requestCode == REQUEST_STORAGE) {
            if (hasLegacyPermission()) {
                startActivity();
            } else {
                allow.setVisibility(View.VISIBLE);
                allow.setOnClickListener(v -> requestLegacyPermission());
                Toast.makeText(this, "Storage permission required. Please allow this permission", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TREE) {
            if (resultCode == RESULT_OK) {
                StatusStorage.onAccessGranted(this, Config.WhatsAppDirectoryPath, data);
            }
            // Continue either way; the status tabs offer the folder picker again when needed.
            startActivity();
        }
    }
}
