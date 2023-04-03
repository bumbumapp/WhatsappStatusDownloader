package com.bumbumapps.statusdownloader.activity;

import android.content.Context;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.bumbumapps.statusdownloader.R;
import com.bumbumapps.statusdownloader.fragment.BWAPictureFragment;
import com.bumbumapps.statusdownloader.fragment.BWASaveFragment;
import com.bumbumapps.statusdownloader.fragment.BWAVideosFragment;
//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.AdView;
//import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.List;

public class BWAActivity extends AppCompatActivity {

    private final String TAG = DrawerActivity.class.getSimpleName();
    private TabLayout tabLayout;
    private ViewPager viewPager;
    ImageView imageviewback;
  //  private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gb);
       // MobileAds.initialize(this, String.valueOf(R.string.admob_app_id));
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BWAActivity.super.onBackPressed();
            }
        });
        imageviewback=findViewById(R.id.bwhatsappback);
        imageviewback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        //setSupportActionBar(toolbar);


        viewPager = findViewById(R.id.viewPager);
        viewPager.setOffscreenPageLimit(0);
        setupViewPager(viewPager);

        tabLayout = findViewById(R.id.tabs);
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


    private void setupViewPager(ViewPager viewPager) {
        BWAActivity.ViewPagerAdapter adapter = new BWAActivity.ViewPagerAdapter(BWAActivity.this, getSupportFragmentManager());
        adapter.addFragment(new BWAPictureFragment(), "Picture");
        adapter.addFragment(new BWAVideosFragment(), "Videos");
        adapter.addFragment(new BWASaveFragment(), "Saved");
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(0);
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
