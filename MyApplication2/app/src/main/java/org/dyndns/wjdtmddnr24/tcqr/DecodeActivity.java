package org.dyndns.wjdtmddnr24.tcqr;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import org.dyndns.wjdtmddnr24.tcqr.Fragment.ScanCodeWithCameraFragment;
import org.dyndns.wjdtmddnr24.tcqr.Fragment.ScanCodeWithGalleryFragment;
import org.dyndns.wjdtmddnr24.tcqr.Fragment.ScanCodeWithURLFragment;
import org.dyndns.wjdtmddnr24.tcqr.Fragment.SimpleScannerFragment;

import java.util.ArrayList;
import java.util.List;

public class DecodeActivity extends AppCompatActivity implements ScanCodeWithCameraFragment.OnFragmentInteractionListener, ScanCodeWithGalleryFragment.OnFragmentInteractionListener, ScanCodeWithURLFragment.OnFragmentInteractionListener {

    private ViewPager viewPager;
    private MainViewPagerAdapter adapter;
    private TabLayout tablayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decode);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tablayout = (TabLayout) findViewById(R.id.tablayout);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        adapter = new MainViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);
        tablayout.setupWithViewPager(viewPager);
    }

    @Override
    public void onFragmentInteractionCamera(Uri uri) {

    }

    @Override
    public void onFragmentInteractionGallery(Uri uri) {

    }

    @Override
    public void onFragmentInteractionURL(Uri uri) {

    }

    class MainViewPagerAdapter extends FragmentStatePagerAdapter {
        List<Fragment> fragmentList;

        public MainViewPagerAdapter(FragmentManager fm) {
            super(fm);
            fragmentList = new ArrayList<>();
            fragmentList.add(new SimpleScannerFragment());
//            fragmentList.add(ScanCodeWithCameraFragment.newInstance());
            fragmentList.add(ScanCodeWithGalleryFragment.newInstance());
            fragmentList.add(ScanCodeWithURLFragment.newInstance());
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "카메라";
                case 1:
                    return "앨범 이미지";
                case 2:
                    return "URL 주소";
                default:
                    return "탭" + position;
            }

        }

    }


}
