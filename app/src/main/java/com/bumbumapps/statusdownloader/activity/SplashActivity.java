package com.bumbumapps.statusdownloader.activity;

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
import android.os.StrictMode;

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
        if (Build.VERSION.SDK_INT >= 24) {
            try {
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (Build.VERSION.SDK_INT < 30) {
            if (!checkBefore30()) {
                requestBefore30();
            } else {
                // User granted file permission, Access your file
               startActivity();
            }
        } else if (Build.VERSION.SDK_INT >= 30) {
            check30AndAfter();
        } else {
            // User already has file access permission
            startActivity();
        }
    }

    private boolean checkBefore30() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }


    private void requestBefore30() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(this, "Storage permission required. Please allow this permission", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);


        }

    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void check30AndAfter() {
        if (!Environment.isExternalStorageManager()) {
            allow.setVisibility(View.VISIBLE);
            allow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                        intent.addCategory("android.intent.category.DEFAULT");
                        intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
                        startActivityForResult(intent, 200);
                    } catch (Exception e) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                        startActivityForResult(intent, 200);
                    }
                }
            });

        }
        else{
            allow.setVisibility(View.GONE);
            startActivity();
        }

    }

    public void startActivity() {
        handlerSplash();
    }

    public void handlerSplash() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                startActivity(new Intent(SplashActivity.this, DrawerActivity.class));
                finish();
            }
        }, SPLASH_TIME_OUT);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED) {

                        //openFragment();
                        startActivity();
                    }

                } else {
                    /*ActivityCompat.requestPermissions(this,
                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                            1);*/
                    finish();
                }
                return;
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200) {
            if (30 >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    startActivity();
                }  // Allow permission for storage access!

            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


}