package org.dyndns.wjdtmddnr24.tcqr;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.transition.AutoTransition;
import android.transition.Slide;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.WriterException;
import com.orhanobut.logger.Logger;

import org.dyndns.wjdtmddnr24.tcqr.Util.CompressUtils;
import org.dyndns.wjdtmddnr24.tcqr.Util.QRCodeUtils;
import org.dyndns.wjdtmddnr24.tcqr.databinding.ActivityRenderBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class RenderActivity extends AppCompatActivity implements DialogInterface.OnClickListener {
    private ActivityRenderBinding binding;
    private String value;
    private Bitmap bitmap;
    private boolean compress;

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRenderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Toolbar toolbar = binding.toolbar;
        toolbar.setTitle(R.string.title2);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setuptransition();

        Intent intent = getIntent();
        value = intent.getStringExtra("value");
        compress = intent.getBooleanExtra("compress", true);
        if (compress) {
            if (binding.contentRender.compresscedard.getVisibility() != View.VISIBLE) {
                binding.contentRender.compresscedard.setVisibility(View.VISIBLE);
            }
        }
        binding.contentRender.valueview.setText(value);
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
        if (id == R.id.menu_share) {
            shareImage();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareImage() {
        if (bitmap == null) {
            Toast.makeText(this, "QR Code not generated yet.", Toast.LENGTH_SHORT).show();
            return;
        }
        Uri bmpUri = saveImageToCache(bitmap);
        if (bmpUri != null) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra(Intent.EXTRA_STREAM, bmpUri);
            intent.setType("image/png");
            startActivity(Intent.createChooser(intent, getString(R.string.share)));
        }
    }

    private void setuptransition() {
        Slide slide = new Slide();
        slide.excludeTarget(binding.abp, true);
        slide.setSlideEdge(Gravity.RIGHT);
        getWindow().setEnterTransition(slide);
        getWindow().setExitTransition(new AutoTransition());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    @Override
    public void onClick(DialogInterface dialog, int i) {
        if (bitmap == null) {
            Toast.makeText(this, "QR Code not generated yet.", Toast.LENGTH_SHORT).show();
            return;
        }
        switch (i) {
            case 0:
                //파일 저장
                try {
                    QRCodeUtils.saveQRCode(RenderActivity.this, bitmap, "/TCQR/Create/");
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(RenderActivity.this, R.string.err_on_saveing_file, Toast.LENGTH_SHORT).show();
                }
                break;
            case 1: { //파일 공유
                shareImage();
                break;
            }
            case 2: { // 파일 클립보드로 저장 (temporarily disabled)
//                Uri bmpUri = saveImageToCache(bitmap);
//                if (bmpUri != null) {
//                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
//                    ClipData clip = ClipData.newUri(getContentResolver(), "Image", bmpUri);
//                    clipboard.setPrimaryClip(clip);
//                    Toast.makeText(RenderActivity.this, R.string.copy_clipboard, Toast.LENGTH_SHORT).show();
//                }
                break;
            }
        }
    }

    private Uri saveImageToCache(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        try {
            File cachePath = new File(getCacheDir(), "images");
            cachePath.mkdirs();
            File file = new File(cachePath, "qr_code_to_share.png");
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();
            return FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
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
                    handler.post(() -> binding.contentRender.cvalueview.setText(finalText));
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
                if (binding.contentRender.rendering.getVisibility() != View.VISIBLE) {
                    binding.contentRender.rendering.setVisibility(View.VISIBLE);
                }
                if (binding.contentRender.compressing.getVisibility() != View.INVISIBLE) {
                    binding.contentRender.compressing.setVisibility(View.INVISIBLE);
                }
                setBitmap(bitmap);

                binding.contentRender.rendering.setImageBitmap(bitmap);
                binding.contentRender.rendering.setOnLongClickListener(v -> {
                    new AlertDialog.Builder(RenderActivity.this).setTitle(R.string.choose_to).setItems(new CharSequence[]{
                            getString(R.string.save_image), getString(R.string.share_image)
                    }, RenderActivity.this).create().show();
                    return true;
                });
                binding.contentRender.rendering.setOnClickListener(v -> {
                    new AlertDialog.Builder(RenderActivity.this).setTitle(R.string.choose_to).setItems(new CharSequence[]{
                            getString(R.string.save_image), getString(R.string.share_image)
                    }, RenderActivity.this).create().show();
                });
            }
        }
    }

}
