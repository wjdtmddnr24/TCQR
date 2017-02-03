package org.dyndns.wjdtmddnr24.tcqr;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.transition.AutoTransition;
import android.transition.Slide;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.flipboard.bottomsheet.BottomSheetLayout;
import com.flipboard.bottomsheet.commons.IntentPickerSheetView;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.WriterException;
import com.orhanobut.logger.Logger;

import org.dyndns.wjdtmddnr24.tcqr.Util.CompressUtils;
import org.dyndns.wjdtmddnr24.tcqr.Util.QRCodeUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Comparator;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import lombok.Getter;
import lombok.Setter;

public class RenderActivity extends AppCompatActivity implements DialogInterface.OnClickListener {
    private static final int REQUEST_WRITE_PERMISSION = 10;
    @BindView(R.id.rendering)
    ImageView rendering;
    @BindView(R.id.valueview)
    TextView valueview;
    @BindView(R.id.compressing)
    ProgressBar compressing;
    @BindView(R.id.cvalueview)
    TextView cvalueview;
    @BindView(R.id.compresscedard)
    CardView compresscedard;
    @BindView(R.id.bottomsheet)
    BottomSheetLayout bottomsheet;
    private String value;
    @Getter
    @Setter
    private Bitmap bitmap;
    private boolean compress;
    private Unbinder unbinder;
    private IntentPickerSheetView intentPickerSheetView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_render);
        unbinder = ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.title2);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setuptransition();

        Intent intent = getIntent();
        value = intent.getStringExtra("value");
        compress = intent.getBooleanExtra("compress", true);
        if (compress) {
            if (compresscedard.getVisibility() != View.VISIBLE) {
                compresscedard.setVisibility(View.VISIBLE);
            }
        }
        valueview.setText(value);
        EncodeTask encodeTask = new EncodeTask();
        encodeTask.execute(value);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.encode_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_share:
                if (intentPickerSheetView != null)
                    bottomsheet.showWithSheetView(intentPickerSheetView);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private IntentPickerSheetView getIntentPickerSheetView() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "QRCode", value);
        Uri uri = Uri.parse(path);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.setType("image/jpg");
        IntentPickerSheetView intentPickerSheetView = new IntentPickerSheetView(this, intent, getString(R.string.share), new IntentPickerSheetView.OnIntentPickedListener() {
            @Override
            public void onIntentPicked(IntentPickerSheetView.ActivityInfo activityInfo) {
                bottomsheet.dismissSheet();
                startActivity(activityInfo.getConcreteIntent(intent));
            }

        });
        intentPickerSheetView.setSortMethod(new Comparator<IntentPickerSheetView.ActivityInfo>() {
            @Override
            public int compare(IntentPickerSheetView.ActivityInfo lhs, IntentPickerSheetView.ActivityInfo rhs) {
                return rhs.label.compareTo(lhs.label);
            }
        });
        return intentPickerSheetView;
    }

    private void setuptransition() {
        Slide slide = new Slide();
        slide.excludeTarget(R.id.abp, true);
        slide.setSlideEdge(Gravity.RIGHT);
        getWindow().setEnterTransition(slide);
        getWindow().setExitTransition(new AutoTransition());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @Override
    public void onClick(DialogInterface dialog, int i) {
        if (ContextCompat.checkSelfPermission(RenderActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(RenderActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
            return;
        }
        switch (i) {
            case 0:
                //파일 저장
                try {
                    QRCodeUtils.saveQRCode(RenderActivity.this, bitmap, "/TCQR/Create/");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(RenderActivity.this, R.string.err_on_saveing_file, Toast.LENGTH_SHORT).show();
                }
                break;
            case 1: {
                //파일 공유
                if (intentPickerSheetView == null)
                    intentPickerSheetView = getIntentPickerSheetView();
                bottomsheet.showWithSheetView(intentPickerSheetView);
                break;
            }
            case 2: {
                // 파일 클립보드로 저장
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                String pathofBmp = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Created By TCQR", null);
                Uri bmpUri = Uri.parse(pathofBmp);
                ClipData clip = ClipData.newRawUri("uri", bmpUri);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(RenderActivity.this, R.string.copy_clipboard, Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }


    class EncodeTask extends AsyncTask<String, Void, Bitmap> {
        private Handler handler;

        public EncodeTask() {
            handler = new Handler();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String text = params[0];
            //문자 압축
            if (compress) {
                try {
                    text = CompressUtils.compress(text);
                    text = CompressUtils.addMarker(text);
                    String finalText = text;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            cvalueview.setText(finalText);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                Logger.d(text);
                return QRCodeUtils.EncodeToQRCode(800, 800, text);
            } catch (WriterException | FormatException | NotFoundException | ChecksumException e) {
                e.printStackTrace();
                return null;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return null;
            }

        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (bitmap != null) {
                if (rendering.getVisibility() != View.VISIBLE) {
                    rendering.setVisibility(View.VISIBLE);
                }
                if (compressing.getVisibility() != View.INVISIBLE) {
                    compressing.setVisibility(View.INVISIBLE);
                }
                setBitmap(bitmap);

                if (ContextCompat.checkSelfPermission(RenderActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    intentPickerSheetView = getIntentPickerSheetView();
                }

                rendering.setImageBitmap(bitmap);
                rendering.setOnLongClickListener(v -> {
                    new AlertDialog.Builder(RenderActivity.this).setTitle(R.string.choose_to).setItems(new CharSequence[]{
                            getString(R.string.save_image), getString(R.string.share_image), getString(R.string.copy_image)
                    }, RenderActivity.this).create().show();
                    return true;
                });
                rendering.setOnClickListener(v -> {
                    new AlertDialog.Builder(RenderActivity.this).setTitle(R.string.choose_to).setItems(new CharSequence[]{
                            getString(R.string.save_image), getString(R.string.share_image), getString(R.string.copy_image)
                    }, RenderActivity.this).create().show();
                });
            }
        }
    }

}
