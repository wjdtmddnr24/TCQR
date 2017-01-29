package org.dyndns.wjdtmddnr24.tcqr;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.transition.AutoTransition;
import android.transition.Slide;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.WriterException;
import com.orhanobut.logger.Logger;

import org.dyndns.wjdtmddnr24.tcqr.Util.CompressUtils;
import org.dyndns.wjdtmddnr24.tcqr.Util.QRCodeUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class RenderActivity extends AppCompatActivity {
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
    private String value;
    private Bitmap bitmap;
    private boolean compress;
    private Unbinder unbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_render);
        unbinder = ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("QR코드 만들기");
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
                rendering.setImageBitmap(bitmap);
            }
        }
    }

}
