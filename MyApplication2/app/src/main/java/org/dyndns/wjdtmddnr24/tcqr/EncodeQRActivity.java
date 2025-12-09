package org.dyndns.wjdtmddnr24.tcqr;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.ChecksumException;
import com.google.zxing.EncodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.NotFoundException;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.orhanobut.logger.Logger;

import org.dyndns.wjdtmddnr24.tcqr.Util.CompressUtils;
import org.dyndns.wjdtmddnr24.tcqr.Util.QRCodeUtils;
import org.dyndns.wjdtmddnr24.tcqr.databinding.ActivityEncodeqrBinding;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZ;
import org.tukaani.xz.XZInputStream;
import org.tukaani.xz.XZOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

public class EncodeQRActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, View.OnLongClickListener, DialogInterface.OnClickListener {

    private ActivityEncodeqrBinding binding;
    private CompressTextTask compressTextTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEncodeqrBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = binding.navView;
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(1).setChecked(true);

        binding.contentEncodeqr.imageView.setOnLongClickListener(this);
        binding.contentEncodeqr.imageView.setOnClickListener(this);
        binding.contentEncodeqr.button.setOnClickListener(this);

    }

    private String compress(String input) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
        LZMA2Options op = new LZMA2Options();
        op.setPreset(8);
        XZOutputStream out = new XZOutputStream(outputStream, op, XZ.CHECK_SHA256);
        byte[] buf = new byte[8192];
        int size;
        while ((size = inputStream.read(buf)) != -1)
            out.write(buf, 0, size);
        String cc = "";
        byte[] buff = outputStream.toByteArray();
        for (int i = 0; i < buff.length; i++) {
            cc += String.valueOf((char) buff[i]);
        }
        String compressed = cc;
        Log.d("XZ", cc);
        out.finish();
        return compressed;
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.navView.getMenu().getItem(1).setChecked(true);
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_share) {
            if (binding.contentEncodeqr.imageView.getDrawable() == null) {
                binding.contentEncodeqr.edittext.setError("먼저 코드를 생성해 주세요");
                binding.contentEncodeqr.edittext.requestFocus();
                return true;
            }
            try {
                File file = QRCodeUtils.saveQRCode(this, ((BitmapDrawable) binding.contentEncodeqr.imageView.getDrawable()).getBitmap(), "/TCQR/Create/");
                Uri bmpUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
                final Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
                shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
                shareIntent.setType("image/png");
                startActivity(shareIntent);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_recognize_decoder) {
            startActivity(new Intent(EncodeQRActivity.this, DecodeActivity.class));
        } else if (id == R.id.nav_text_encoder) {
            //                startActivity(new Intent(En));
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.button) {//키보드 내림
            Logger.d("키보드 내림");
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            //내용 없는 경우
            if (TextUtils.isEmpty(binding.contentEncodeqr.edittext.getText().toString())) {
                binding.contentEncodeqr.edittext.setError("생성하고자 하는 내용을 입력해주세요.");
                binding.contentEncodeqr.edittext.requestFocus();
                return;
            }
            String text = binding.contentEncodeqr.edittext.getText().toString();
            EncodeTask encodeTask = new EncodeTask(this);
            encodeTask.execute(text);
        } else if (viewId == R.id.imageView) {
            if (binding.contentEncodeqr.imageView.getDrawable() != null) {
                new AlertDialog.Builder(this).setTitle("기능 선택").setItems(new CharSequence[]{
                        "이미지 저장", "공유", "클립보드로 복사"
                }, this).create().show();
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onLongClick(View view) {
        if (view.getId() == R.id.imageView) {
            if (binding.contentEncodeqr.imageView.getDrawable() != null) {
                new AlertDialog.Builder(this).setTitle("기능 선택").setItems(new CharSequence[]{
                        "이미지 저장", "공유", "클립보드로 복사"
                }, this).create().show();
            }
        }
        return false;
    }

    @Override
    public void onClick(DialogInterface dialog, int i) {
        switch (i) {
            case 0:
                //파일 저장
                try {
                    QRCodeUtils.saveQRCode(EncodeQRActivity.this, ((BitmapDrawable) binding.contentEncodeqr.imageView.getDrawable()).getBitmap(), "/TCQR/Create/");
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(EncodeQRActivity.this, "파일을 저장하는데 문제가 발생하였습니다.", Toast.LENGTH_SHORT).show();
                }
                break;
            case 1: { //파일 공유
                try {
                    File file = QRCodeUtils.saveQRCode(this, ((BitmapDrawable) binding.contentEncodeqr.imageView.getDrawable()).getBitmap(), "/TCQR/Create/");
                    Uri bmpUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
                    final Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
                    shareIntent.setType("image/png");
                    startActivity(shareIntent);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
            case 2: { // 파일 클립보드로 저장
                try {
                    File file = QRCodeUtils.saveQRCode(this, ((BitmapDrawable) binding.contentEncodeqr.imageView.getDrawable()).getBitmap(), "/TCQR/Create/");
                    Uri bmpUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newUri(getContentResolver(), "Image", bmpUri);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(EncodeQRActivity.this, "클립보드로 복사하였습니다.", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    class EncodeTask extends AsyncTask<String, Void, Bitmap> {
        private ProgressDialog progressDialog;
        private Context context;
        private Handler handler;

        public EncodeTask(Context context) {
            this.context = context;
            handler = new Handler();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(context, "QR코드를 생성하는중입니다.", "잠시만 기다리세요");
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String text = params[0];
            //문자 압축
            if (text.getBytes().length > 2000) {
                handler.post(() -> Toast.makeText(EncodeQRActivity.this, "문자가 너무 커서 압축 후 QR코드를 생성합니다.", Toast.LENGTH_SHORT).show());
                try {
                    text = CompressUtils.compress(text);
                    text = CompressUtils.addMarker(text);
                } catch (IOException e) {
                    e.printStackTrace();
                    handler.post(() -> {
                        if (progressDialog.isShowing())
                            progressDialog.dismiss();
                        Toast.makeText(EncodeQRActivity.this, "문자를 압축하는데 문제가 발생하였습니다.", Toast.LENGTH_SHORT).show();
                    });
                    return null;
                }
            }
            try {
                return QRCodeUtils.EncodeToQRCode(500, 500, text);
            } catch (WriterException | FormatException | UnsupportedEncodingException | NotFoundException | ChecksumException e) {
                e.printStackTrace();
                new Handler().post(() -> {
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                    Toast.makeText(EncodeQRActivity.this, "QR코드를 생성하는데 문제가 발생하였습니다.", Toast.LENGTH_SHORT).show();
                });
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
                progressDialog = null;

            }
            if (bitmap != null) {
                binding.contentEncodeqr.imageView.setImageBitmap(bitmap);
            }
        }
    }

    class CompressTextTask extends AsyncTask<byte[], Integer, byte[]> {
        private ProgressDialog progressDialog;
        private int size;

        public CompressTextTask(int size) {
            super();
            this.size = size;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(EncodeQRActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMessage("문자를 압축하는 중입니다");
            progressDialog.setCancelable(false);
            progressDialog.setMax(size / 4096);
            progressDialog.setMessage("잠시만 기다려주세요");
            progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progressDialog.setProgress(values[0]);
        }

        @Override
        protected byte[] doInBackground(byte[]... bytes) {
            byte[] result = null;
            try {
                byte[] compressTarget = bytes[0];
                LZMA2Options options = new LZMA2Options();
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                ByteArrayInputStream input = new ByteArrayInputStream(compressTarget);
                options.setPreset(7);
                XZOutputStream out = new XZOutputStream(buffer, options);
                byte[] buf = new byte[4096];
                int size;
                int progress = 0;
                while ((size = input.read(buf)) != -1) {
                    out.write(buf, 0, size);
                    publishProgress(++progress);
                }
                out.finish();

                byte[] original = buffer.toByteArray();
                String rr = Base64.encodeToString(buffer.toByteArray(), 0);
                rr = new String(Base64.decode(rr, 0));
                byte[] results = rr.getBytes("ISO-8859-1");
                result = buffer.toByteArray();

                ByteArrayInputStream c_input = new ByteArrayInputStream(buffer.toByteArray());
//                ByteArrayInputStream c_input = new ByteArrayInputStream(results);
                XZInputStream xz_input = new XZInputStream(c_input);
                StringBuffer sb = new StringBuffer();
                int read;
                while ((read = xz_input.read(buf)) != -1)
                    sb.append(new String(buf, 0, read));
                String asdf = sb.toString();

            } catch (IOException e) {
                e.printStackTrace();
                result = null;
            }
            return result;
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (bytes != null) {
                try {
                    String encodeValue = "TCQREncoded:" + (char) 0x04 + new String(bytes, "ISO-8859-1");
//                    String encodeValue = new String(bytes, "ISO-8859-1");
//                    String encodeValue = Base64.encodeToString(new String(bytes, "ISO-8859-1"), 0);
                    QRCodeWriter writer = new QRCodeWriter();
                    Hashtable hints = new Hashtable();
                    hints.put(EncodeHintType.CHARACTER_SET, "ISO-8859-1");
                    BitMatrix result = new MultiFormatWriter().encode(encodeValue, BarcodeFormat.QR_CODE, 500, 500, hints);
                    int w = result.getWidth();
                    int h = result.getHeight();
                    int[] pixels = new int[w * h];
                    for (int y = 0; y < h; y++) {
                        int offset = y * w;
                        for (int x = 0; x < w; x++) {
                            pixels[offset + x] = result.get(x, y) ? 0xFF000000 : 0xFFFFFFFF;
                        }
                    }
                    Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                    bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
                    binding.contentEncodeqr.imageView.setImageBitmap(bitmap);
//                    Toast.makeText(EncodeActivity.this, "문자 압축 성공" + bytes.length + ":" + size, Toast.LENGTH_SHORT).show();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    Toast.makeText(EncodeQRActivity.this, "문자를 압축하는데 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show();
                } catch (WriterException e) {
                    e.printStackTrace();
                }

            } else {
                Toast.makeText(EncodeQRActivity.this, "문자를 압축하는데 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show();
            }
            compressTextTask = null;
        }
    }
}
