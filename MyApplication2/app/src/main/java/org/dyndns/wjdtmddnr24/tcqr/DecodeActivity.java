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
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
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

import com.flipboard.bottomsheet.BottomSheetLayout;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.zxing.Result;

import org.dyndns.wjdtmddnr24.tcqr.Fragment.ScanCodeWithCameraFragment;
import org.dyndns.wjdtmddnr24.tcqr.Fragment.ScanCodeWithGalleryFragment;
import org.dyndns.wjdtmddnr24.tcqr.Fragment.ScanCodeWithURLFragment;
import org.dyndns.wjdtmddnr24.tcqr.Fragment.SimpleScannerFragment;
import org.dyndns.wjdtmddnr24.tcqr.model.QRCode;

import java.io.IOException;
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
    private FirebaseAnalytics mFirebaseAnalytics;

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
        BottomSheetLayout bottomSheetLayout = ((ScanCodeWithGalleryFragment) adapter.getItem(1)).getBottomsheet();
        if (bottomSheetLayout != null && bottomSheetLayout.isSheetShowing()) {
            bottomSheetLayout.dismissSheet();
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
        toolbar.setTitle(R.string.title1);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

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

    private void showBtmSheet(QRCode qrCode) {
        if (!qrCode.isCompressed()) {
            qrInfoCompressed.setVisibility(View.GONE);
        } else {
            qrInfoCompressed.setVisibility(View.VISIBLE);
        }
        qrInfoText.setText(qrCode.getText());
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
        Bundle bundle = new Bundle();
        bundle.putString("decode_from", "Gallery");
        bundle.putString("decode_size", String.valueOf(result.getText().length()));
        mFirebaseAnalytics.logEvent("Decode",bundle);
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
        mFirebaseAnalytics.logEvent("Decode",bundle);
        QRInfoTask qrInfoTask = new QRInfoTask(2);
        qrInfoTask.execute(result);
    }

    @Override
    public void onFragmentInteractionSimpleCamera(Result result) {
        Bundle bundle = new Bundle();
        bundle.putString("decode_from", "Camera");
        bundle.putString("decode_size", String.valueOf(result.getText().length()));
        mFirebaseAnalytics.logEvent("Decode",bundle);
        QRInfoTask qrInfoTask = new QRInfoTask(1);
        qrInfoTask.execute(result);
    }

    @OnClick({R.id.qr_info_copy, R.id.qr_info_bottom_sheet})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.qr_info_copy: {
                String text = qrInfoText.getText().toString();
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("TCQR content", text);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, R.string.copy_clipboard, Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.qr_info_bottom_sheet: {
                String text = qrInfoText.getText().toString();
                Intent intent = new Intent(DecodeActivity.this, QRCodeInfoActivity.class);
                intent.putExtra("text", text);
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(DecodeActivity.this, qrInfoText, "content");
                startActivity(intent, optionsCompat.toBundle());
                break;
            }
        }
    }

    class MainViewPagerAdapter extends FragmentStatePagerAdapter {
        public List<Fragment> fragmentList;

        public MainViewPagerAdapter(FragmentManager fm) {
            super(fm);
            fragmentList = new ArrayList<>();
            fragmentList.add(new SimpleScannerFragment());
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
