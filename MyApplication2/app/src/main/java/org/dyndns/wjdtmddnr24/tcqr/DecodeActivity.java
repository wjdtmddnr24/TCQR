package org.dyndns.wjdtmddnr24.tcqr;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;
import com.orhanobut.logger.Logger;

import org.dyndns.wjdtmddnr24.tcqr.Fragment.ScanCodeWithCameraFragment;
import org.dyndns.wjdtmddnr24.tcqr.Fragment.ScanCodeWithGalleryFragment;
import org.dyndns.wjdtmddnr24.tcqr.Fragment.ScanCodeWithURLFragment;
import org.dyndns.wjdtmddnr24.tcqr.Fragment.SimpleScannerFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class DecodeActivity extends AppCompatActivity implements ScanCodeWithCameraFragment.OnFragmentInteractionListener, ScanCodeWithGalleryFragment.OnFragmentInteractionListener, ScanCodeWithURLFragment.OnFragmentInteractionListener, SimpleScannerFragment.OnFragmentInteractionListener {

    private static final int REQUEST_WRITE_PERMISSION = 10;
    @BindView(R.id.qr_info_compressed)
    LinearLayout qrInfoCompressed;
    @BindView(R.id.qr_info_text)
    TextView qrInfoText;
    @BindView(R.id.qr_info_copy)
    Button qrInfoCopy;
    @BindView(R.id.qr_info_bottom_sheet)
    CardView qrInfoBottomSheet;
    @BindView(R.id.btm_sheet_back)
    View btmSheetBack;
    private ViewPager viewPager;
    private MainViewPagerAdapter adapter;
    private TabLayout tablayout;
    private boolean btmstate = false;
    private Unbinder unbinder;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == REQUEST_WRITE_PERMISSION) {
//            if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(EncodeQRActivity.this, "본 기능을 사용하려면 쓰기 권한이 필요합니다. 쓰기권한을 주시기 바랍니다.", Toast.LENGTH_SHORT).show();
//            }
//        }
    }

    @Override
    public void onBackPressed() {
        if (btmstate) {
            hideBtmSheet();
            return;
        }
        super.onBackPressed();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decode);
        unbinder = ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("QR코드 인식");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tablayout = (TabLayout) findViewById(R.id.tablayout);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        adapter = new MainViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);
        tablayout.setupWithViewPager(viewPager);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_WRITE_PERMISSION);
        }
        btmSheetBack.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (btmstate) {
                    hideBtmSheet();
                }
                return true;
            }
        });


    }

    private void showBtmSheet() {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.pop_up);
        qrInfoBottomSheet.setVisibility(View.VISIBLE);
        qrInfoBottomSheet.startAnimation(animation);
        btmstate = true;
        Animation animation1 = new AlphaAnimation(0, 1f);
        btmSheetBack.setVisibility(View.VISIBLE);
        btmSheetBack.startAnimation(animation1);
    }

    private void hideBtmSheet() {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.pop_down);
        qrInfoBottomSheet.setVisibility(View.INVISIBLE);
        qrInfoBottomSheet.startAnimation(animation);
        btmstate = false;
        btmSheetBack.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @Override
    public void onFragmentInteractionCamera(Uri uri) {

    }

    @Override
    public void onFragmentInteractionGallery(Result result) {
        Logger.d("hi");
        if (btmstate) {
            hideBtmSheet();
        } else {
            showBtmSheet();
        }

    }

    @Override
    public void onFragmentInteractionURL(Result result) {

    }

    @Override
    public void onFragmentInteractionSimpleCamera(Result result) {

    }

    @OnClick(R.id.qr_info_bottom_sheet)
    public void onClick() {
        Toast.makeText(this, "hi", Toast.LENGTH_SHORT).show();
    }

    class MainViewPagerAdapter extends FragmentStatePagerAdapter {
        List<Fragment> fragmentList;

        public MainViewPagerAdapter(FragmentManager fm) {
            super(fm);
            fragmentList = new ArrayList<>();
            fragmentList.add(new SimpleScannerFragment());
//          fragmentList.add(ScanCodeWithCameraFragment.newInstance());
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
