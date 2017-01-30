package org.dyndns.wjdtmddnr24.tcqr;

import android.Manifest;
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
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

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
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZ;
import org.tukaani.xz.XZInputStream;
import org.tukaani.xz.XZOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

public class EncodeQRActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, View.OnLongClickListener, DialogInterface.OnClickListener {

    public static final int REQUEST_WRITE_PERMISSION = 10;
    private ImageView imageView;
    private EditText editText;
    private Button button;
    private CompressTextTask compressTextTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encodeqr);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(1).setChecked(true);

        button = (Button) findViewById(R.id.button);
        editText = (EditText) findViewById(R.id.edittext);
        imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setOnLongClickListener(this);
        imageView.setOnClickListener(this);
        button.setOnClickListener(this);

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
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.getMenu().getItem(1).setChecked(true);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "먼저 쓰기 권한을 주시기 바랍니다", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EncodeQRActivity.REQUEST_WRITE_PERMISSION);
            return true;
        }
        int id = item.getItemId();
        switch (id) {
            /*case R.id.menu_save:
                if (imageView.getDrawable() == null) {
                    editText.setError("먼저 코드를 생성해 주세요");
                    editText.requestFocus();
                    return true;
                }
                try {
                    Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                    QRCodeUtils.saveQRCode(this, bitmap, "/TCQR/Create/");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(EncodeQRActivity.this, "파일을 저장하는데 문제가 발생하였습니다.", Toast.LENGTH_SHORT).show();
                }
                break;*/
            case R.id.menu_share:
                if (imageView.getDrawable() == null) {
                    editText.setError("먼저 코드를 생성해 주세요");
                    editText.requestFocus();
                    return true;
                }
                Bitmap QRCode = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                String pathofBmp = MediaStore.Images.Media.insertImage(getContentResolver(), QRCode, "Created By TCQR", null);
                Uri bmpUri = Uri.parse(pathofBmp);
                final Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
                shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
                shareIntent.setType("image/png");
                startActivity(shareIntent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_recognize_decoder:
                startActivity(new Intent(EncodeQRActivity.this, DecodeActivity.class));
                break;
            case R.id.nav_text_encoder:
//                startActivity(new Intent(En));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button:
                //키보드 내림
                Logger.d("키보드 내림");
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                //내용 없는 경우
                if (TextUtils.isEmpty(editText.getText().toString())) {
                    editText.setError("생성하고자 하는 내용을 입력해주세요.");
                    editText.requestFocus();
                    return;
                }
                String text = editText.getText().toString();
                EncodeTask encodeTask = new EncodeTask(this);
                encodeTask.execute(text);
                break;
            case R.id.imageView:
                if (imageView.getDrawable() != null) {
                    new AlertDialog.Builder(this).setTitle("기능 선택").setItems(new CharSequence[]{
                            "이미지 저장", "공유", "클립보드로 복사"
                    }, this).create().show();
                }
                break;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_WRITE_PERMISSION) {
            if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(EncodeQRActivity.this, "본 기능을 사용하려면 쓰기 권한이 필요합니다. 쓰기권한을 주시기 바랍니다.", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Deprecated
    private String saveQRCode(Bitmap bitmap) throws FileNotFoundException {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
        }
        String ex_storage = Environment.getExternalStorageDirectory().getAbsolutePath();
        String foler_name = "/TCQR/Create/";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmm");
        Date date = new Date();
        String file_name = dateFormat.format(date) + ".jpg";
        String fullpath = ex_storage + foler_name + file_name;
        File file = new File(ex_storage + foler_name, file_name);
        File dir = new File(ex_storage + foler_name);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
        return fullpath;
    }

    @Override
    public boolean onLongClick(View view) {
        switch (view.getId()) {
            case R.id.imageView:
                if (imageView.getDrawable() != null) {
                    new AlertDialog.Builder(this).setTitle("기능 선택").setItems(new CharSequence[]{
                            "이미지 저장", "공유", "클립보드로 복사"
                    }, this).create().show();
                }
                break;
        }
        return false;
    }

    @Override
    public void onClick(DialogInterface dialog, int i) {
        if (ContextCompat.checkSelfPermission(EncodeQRActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(EncodeQRActivity.this, "먼저 쓰기 권한을 주시기 바랍니다", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(EncodeQRActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
            return;
        }
        switch (i) {
            case 0:
                //파일 저장
                try {
                    Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                    QRCodeUtils.saveQRCode(EncodeQRActivity.this, bitmap, "/TCQR/Create/");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(EncodeQRActivity.this, "파일을 저장하는데 문제가 발생하였습니다.", Toast.LENGTH_SHORT).show();
                }
                break;
            case 1: {
                //파일 공유
                Bitmap QRCode = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                String pathofBmp = MediaStore.Images.Media.insertImage(getContentResolver(), QRCode, "Created By TCQR", null);
                Uri bmpUri = Uri.parse(pathofBmp);
                final Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
                shareIntent.setType("image/png");
                startActivity(shareIntent);
                break;
            }
            case 2: {
                // 파일 클립보드로 저장
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                Bitmap QRCode = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                String pathofBmp = MediaStore.Images.Media.insertImage(getContentResolver(), QRCode, "Created By TCQR", null);
                Uri bmpUri = Uri.parse(pathofBmp);
                ClipData clip = ClipData.newRawUri("uri", bmpUri);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(EncodeQRActivity.this, "클립보드로 복사하였습니다.", Toast.LENGTH_SHORT).show();
                break;
            }
        }
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
                imageView.setImageBitmap(bitmap);
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
                    imageView.setImageBitmap(bitmap);
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
