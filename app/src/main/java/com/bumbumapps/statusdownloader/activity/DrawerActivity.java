package com.bumbumapps.statusdownloader.activity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.RequiresApi;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumbumapps.statusdownloader.utils.ForceUpdateAsync;
import com.bumbumapps.statusdownloader.utils.Observer;
import com.bumbumapps.statusdownloader.R;
import com.bumbumapps.statusdownloader.fragment.WAPictureFragment;
import com.bumbumapps.statusdownloader.fragment.WASaveFragment;
import com.bumbumapps.statusdownloader.fragment.WAVideosFragment;
import com.bumbumapps.statusdownloader.utils.Config;
//import com.google.android.gms.ads.AdListener;
//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.AdView;
//import com.google.android.gms.ads.InterstitialAd;
//import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.List;

public class DrawerActivity extends AppCompatActivity
        implements Observer {

    private final String TAG = "DrawerTAG";
    boolean doubleBackToExitPressedOnce = false;
    NavigationView navigationView;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    ImageView whatsapbusiness;
//    private AdView adView;
//    private InterstitialAd interstitialAd;





    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
//        MobileAds.initialize(this, String.valueOf(R.string.admob_app_id));
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("WhatsApp Status");


        whatsapbusiness=findViewById(R.id.bwhatsapp);
        whatsapbusiness.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DrawerActivity.this, BWAActivity.class));
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        //DrawerLayout drawer = findViewById(R.id.drawer_layout);


        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabs);


        viewPager.setOffscreenPageLimit(0);
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);

//        adView = (AdView) findViewById(R.id.admob_adview);
//        AdRequest adRequest = new AdRequest.Builder()
//                .build();
//        adView.loadAd(adRequest);

    }


    @Override
    protected void onDestroy() {
//        if (adView != null) {
//            adView.destroy();
//        }

        super.onDestroy();

    }

    @Override
    public void onBackPressed() {

        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            if (doubleBackToExitPressedOnce) {
                finish();
                //super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        } else {
            super.onBackPressed();
        }
    }







    private void setupViewPager(ViewPager viewPager) {

        ForceUpdateAsync forceUpdateAsync = new ForceUpdateAsync(this);
        forceUpdateAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        ViewPagerAdapter adapter = new ViewPagerAdapter(DrawerActivity.this, getSupportFragmentManager());
        adapter.addFragment(new WAPictureFragment(), "Picture");
        adapter.addFragment(new WAVideosFragment(), "Videos");
        adapter.addFragment(new WASaveFragment(), "Saved");
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(0);
    }

    @Override
    protected void onResume() {
        super.onResume();


    }






    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    private void logUser() {
        // TODO: Use the current user's information
        // You can call any combination of these three methods
    }


    @Override
    public void update(String value, Context context) {
        Log.v("KKKKKKU", value);
        int count = Integer.parseInt(value);
        if (count > Config.count) {
//            interstitialAd = new InterstitialAd(context);
//            interstitialAd.setAdUnitId(adsint);
//            interstitialAd.loadAd(new AdRequest.Builder().build());
//            interstitialAd.setAdListener(new AdListener() {
//                @Override
//                public void onAdLoaded() {
//                    // TODO Auto-generated method stub
//                    super.onAdLoaded();
//                    if (interstitialAd.isLoaded()) {
//                        interstitialAd.show();
//                    }
//                }
//            });
        }
        }


    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();
        private Context mContext;

        public ViewPagerAdapter(Context context, FragmentManager manager) {
            super(manager);
            mContext = context;
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}

