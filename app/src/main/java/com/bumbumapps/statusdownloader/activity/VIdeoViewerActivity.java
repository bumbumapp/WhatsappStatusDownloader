package com.bumbumapps.statusdownloader.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumbumapps.statusdownloader.utils.Observer;
import com.bumbumapps.statusdownloader.R;
import com.bumbumapps.statusdownloader.utils.Subject;
import com.bumbumapps.statusdownloader.utils.Config;
import com.bumbumapps.statusdownloader.utils.StatusStorage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class VIdeoViewerActivity extends AppCompatActivity implements Subject {

    private final String TAG = "VideoViewer";
    public LinearLayout btn_download, btn_share, btn_re_post;
    public ImageButton img_btn_download, img_btn_share, img_re_post;
    public int count = Config.count;
    public List<Observer> observers;
    String videoPath = "", path = "", atype = "", package_name = "";
    String type = "";
    VideoView video_view;
    String listenerValue = "";
    DrawerActivity drawer = new DrawerActivity();
    private int position = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        video_view = findViewById(R.id.video_view);

        observers = new ArrayList<>();

        Intent intent = getIntent();
        if (intent != null) {
            videoPath = intent.getStringExtra("video");
            type = intent.getStringExtra("type");
            atype = intent.getStringExtra("atype");
            video_view.setVideoPath(videoPath);
            video_view.start();
        }
        MediaController mediaController = new MediaController(this);
        video_view.setMediaController(mediaController);
        try {
            video_view.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    video_view.seekTo(position);
                    if (position == 0) {
                        video_view.start();
                    } else {
                        video_view.resume();

                    }
                }
            });
        } catch (Exception ex) {
        }

        if (type.equals("0")) {
            path = Config.WhatsAppSaveStatus;
            package_name = "com.whatsapp";

        } else if (type.equals("1")) {
            path = Config.GBWhatsAppSaveStatus;
            package_name = "com.gbwhatsapp";

        } else if (type.equals("2")) {
            path = Config.WhatsAppBusinessSaveStatus;
            package_name = "com.whatsapp.w4b";

        }

        btn_download = findViewById(R.id.btn_download);
        btn_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                   /* type == 0 (WhatsApp Normal)
                    type == 1 (WhatsApp GB)
                    type == 2 (WhatsApp Business)*/
                copyFileOrDirectory(videoPath, path);

                sharePerAds();
            }
        });

        img_btn_download = findViewById(R.id.img_btn_download);
        img_btn_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                   /* type == 0 (WhatsApp Normal)
                    type == 1 (WhatsApp GB)
                    type == 2 (WhatsApp Business)*/
                copyFileOrDirectory(videoPath, path);

                sharePerAds();

            }
        });

        btn_share = findViewById(R.id.btn_share);
        btn_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (StatusStorage.isImage(videoPath)) {
                    shareVia("image/jpg", videoPath);
                } else if (StatusStorage.isVideo(videoPath)) {
                    shareVia("video/mp4", videoPath);
                }

                sharePerAds();

            }
        });
        img_btn_share = findViewById(R.id.img_btn_share);
        img_btn_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (StatusStorage.isImage(videoPath)) {
                    shareVia("image/jpg", videoPath);
                } else if (StatusStorage.isVideo(videoPath)) {
                    shareVia("video/mp4", videoPath);
                }

                sharePerAds();

            }
        });

        btn_re_post = findViewById(R.id.btn_re_post);
        btn_re_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (StatusStorage.isImage(videoPath)) {
                    shareViaWhatsApp("image/jpg", videoPath, package_name);
                } else if (StatusStorage.isVideo(videoPath)) {
                    shareViaWhatsApp("video/mp4", videoPath, package_name);
                }

                sharePerAds();

            }
        });
        img_re_post = findViewById(R.id.img_re_post);
        img_re_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (StatusStorage.isImage(videoPath)) {
                    shareViaWhatsApp("image/jpg", videoPath, package_name);
                } else if (StatusStorage.isVideo(videoPath)) {
                    shareViaWhatsApp("video/mp4", videoPath, package_name);
                }

                sharePerAds();

            }
        });

        if (atype.equals("0")) {
            btn_download.setVisibility(View.GONE);
        } else if (atype.equals("1")) {
            btn_download.setVisibility(View.VISIBLE);
        }

        sharePerAds();


    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("Position", video_view.getCurrentPosition());
        video_view.pause();

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        position = savedInstanceState.getInt("Position");
        video_view.seekTo(position);

    }

    public void copyFileOrDirectory(String srcDir, String dstDir) {
        // Streams through ContentResolver/MediaStore (scoped storage); srcDir is a path or content Uri.
        boolean ok = StatusStorage.save(this, srcDir, dstDir);
        Toast.makeText(this, ok ? "Saved" : "Save failed", Toast.LENGTH_SHORT).show();
    }



    public void shareVia(String type, String path) {
        StatusStorage.share(this, path, type);
    }


    public void shareViaWhatsApp(String type, String path, String package_name) {
        try {
            getPackageManager().getPackageInfo(package_name, 0);
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType(type);
            sharingIntent.putExtra(Intent.EXTRA_STREAM, StatusStorage.shareUri(this, path));
            sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            sharingIntent.setPackage(package_name);
            startActivity(sharingIntent);
        } catch (PackageManager.NameNotFoundException | RuntimeException e) {
            Toast.makeText(this, "WhatsApp not Installed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        video_view.start();
        super.onResume();
    }


    public void allSharedPreference(int i) {
        SharedPreferences preferences = getSharedPreferences("PREFRENCE", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("ALL", String.valueOf(i));
        editor.commit();
    }

    public void sharePerAds() {
        int i;
        if (Config.getALLState(VIdeoViewerActivity.this).length() > 0) {
            i = Integer.parseInt(Config.getALLState(VIdeoViewerActivity.this));
            if (i > count) {

                allSharedPreference(0);
            } else {
                i++;
                allSharedPreference(i);
            }
        } else {
            i = 1;
            allSharedPreference(i);
        }
        listenerValue = String.valueOf(i);
        register(drawer);
        notifyObservers();
        unregister(drawer);
    }


    @Override
    public void register(final Observer observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void unregister(final Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        for (final Observer observer : observers) {
            Log.v("KKKKKKKKK", "" + listenerValue);
            observer.update(listenerValue, VIdeoViewerActivity.this);
        }
    }
}
