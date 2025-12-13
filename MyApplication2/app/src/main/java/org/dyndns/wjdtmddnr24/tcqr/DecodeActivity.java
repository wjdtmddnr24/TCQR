package org.dyndns.wjdtmddnr24.tcqr;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.zxing.Result;

import org.dyndns.wjdtmddnr24.tcqr.Fragment.ScanCodeWithCameraFragment;
import org.dyndns.wjdtmddnr24.tcqr.Fragment.ScanCodeWithGalleryFragment;
import org.dyndns.wjdtmddnr24.tcqr.Fragment.ScanCodeWithURLFragment;
import org.dyndns.wjdtmddnr24.tcqr.Fragment.SimpleScannerFragment;
import org.dyndns.wjdtmddnr24.tcqr.databinding.ActivityDecodeBinding;
import org.dyndns.wjdtmddnr24.tcqr.model.QRCode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DecodeActivity extends AppCompatActivity implements ScanCodeWithCameraFragment.OnFragmentInteractionListener, ScanCodeWithGalleryFragment.OnFragmentInteractionListener, ScanCodeWithURLFragment.OnFragmentInteractionListener, SimpleScannerFragment.OnFragmentInteractionListener {

    private static final int REQUEST_WRITE_PERMISSION = 10;
    private ActivityDecodeBinding binding;
    private MainViewPagerAdapter adapter;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onBackPressed() {
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            return;
        }
        super.onBackPressed();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDecodeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Toolbar toolbar = binding.toolbar;
        toolbar.setTitle(R.string.title1);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        adapter = new MainViewPagerAdapter(getSupportFragmentManager());
        binding.contentDecode.viewpager.setAdapter(adapter);
        binding.contentDecode.viewpager.setOffscreenPageLimit(3);
        binding.tablayout.setupWithViewPager(binding.contentDecode.viewpager);

        bottomSheetBehavior = BottomSheetBehavior.from(binding.qrInfoLayout.getRoot());
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_WRITE_PERMISSION);
        }

        binding.qrInfoLayout.qrInfoCopy.setOnClickListener(v -> {
            String text = binding.qrInfoLayout.qrInfoText.getText().toString();
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("TCQR content", text);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, R.string.copy_clipboard, Toast.LENGTH_SHORT).show();
        });

        binding.qrInfoLayout.getRoot().setOnClickListener(v -> {
            String text = binding.qrInfoLayout.qrInfoText.getText().toString();
            Intent intent = new Intent(DecodeActivity.this, QRCodeInfoActivity.class);
            intent.putExtra("text", text);
            ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(DecodeActivity.this, binding.qrInfoLayout.qrInfoText, "content");
            startActivity(intent, optionsCompat.toBundle());
        });
    }

    private void showBtmSheet(QRCode qrCode) {
        if (!qrCode.isCompressed()) {
            binding.qrInfoLayout.qrInfoCompressed.setVisibility(View.GONE);
        } else {
            binding.qrInfoLayout.qrInfoCompressed.setVisibility(View.VISIBLE);
        }
        binding.qrInfoLayout.qrInfoText.setText(qrCode.getText());
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    @Override
    public void onFragmentInteractionCamera(Uri uri) {

    }

    @Override
    public void onFragmentInteractionGallery(Result result) {
        Bundle bundle = new Bundle();
        bundle.putString("decode_from", "Gallery");
        bundle.putString("decode_size", String.valueOf(result.getText().length()));
        mFirebaseAnalytics.logEvent("Decode", bundle);
        QRInfoTask qrInfoTask = new QRInfoTask(1);
        qrInfoTask.execute(result);
    }

    @Override
    public void showQRCodeInfo(QRCode qrCode) {
        showBtmSheet(qrCode);
    }

    @Override
    public void onFragmentInteractionURL(Result result) {
        Bundle bundle = new Bundle();
        bundle.putString("decode_from", "URL");
        bundle.putString("decode_size", String.valueOf(result.getText().length()));
        mFirebaseAnalytics.logEvent("Decode", bundle);
        QRInfoTask qrInfoTask = new QRInfoTask(2);
        qrInfoTask.execute(result);
    }

    @Override
    public void onFragmentInteractionSimpleCamera(Result result) {
        Bundle bundle = new Bundle();
        bundle.putString("decode_from", "Camera");
        bundle.putString("decode_size", String.valueOf(result.getText().length()));
        mFirebaseAnalytics.logEvent("Decode", bundle);
        QRInfoTask qrInfoTask = new QRInfoTask(1);
        qrInfoTask.execute(result);
    }

    class MainViewPagerAdapter extends FragmentStatePagerAdapter {
        public List<Fragment> fragmentList;

        public MainViewPagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            fragmentList = new ArrayList<>();
            fragmentList.add(new SimpleScannerFragment());
            fragmentList.add(ScanCodeWithGalleryFragment.newInstance());
            fragmentList.add(ScanCodeWithURLFragment.newInstance());
        }

        @NonNull
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
                    return getResources().getString(R.string.tab_camera);
                case 1:
                    return getResources().getString(R.string.tab_album);
                case 2:
                    return getResources().getString(R.string.tab_url);
                default:
                    return "탭" + position;
            }

        }

    }

    class QRInfoTask extends AsyncTask<Result, Void, QRCode> {
        private int requestfrom;

        public QRInfoTask() {
            requestfrom = 0;
        }

        public QRInfoTask(int requestfrom) {
            this.requestfrom = requestfrom;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected QRCode doInBackground(Result... params) {
            QRCode qrCode;
            try {
                qrCode = new QRCode(params[0]);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            switch (requestfrom) {
                case 1:
                    ScanCodeWithGalleryFragment scanCodeWithGalleryFragment = (ScanCodeWithGalleryFragment) adapter.getItem(1);
                    scanCodeWithGalleryFragment.setQrCode(qrCode);
                    break;
                case 2:
                    ScanCodeWithURLFragment scanCodeWithURLFragment = (ScanCodeWithURLFragment) adapter.getItem(2);
                    scanCodeWithURLFragment.setQrCode(qrCode);
                    break;
                default:
            }
            return qrCode;
        }

        @Override
        protected void onPostExecute(QRCode qrCode) {
            if (qrCode == null) {
                Toast.makeText(DecodeActivity.this, "QR코드를 인식하는데 문제가 발생하였습니다.", Toast.LENGTH_SHORT).show();
            } else {
                showBtmSheet(qrCode);
            }

        }
    }

}
