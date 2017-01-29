package org.dyndns.wjdtmddnr24.tcqr.Fragment;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import org.dyndns.wjdtmddnr24.tcqr.EncodeQRActivity;
import org.dyndns.wjdtmddnr24.tcqr.R;
import org.tukaani.xz.XZInputStream;

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
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class ScanCodeWithURLFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener {

    @BindView(R.id.url_info)
    TextView urlInfo;
    private OnFragmentInteractionListener mListener;
    private TextView textView;
    private Button button;
    private EditText editText;
    private ImageView imageView;
    private Handler handler;
    private DecompressTextTask decompressTextTask = null;
    private Unbinder unbinder;

    public ScanCodeWithURLFragment() {
    }

    public static ScanCodeWithURLFragment newInstance() {
        ScanCodeWithURLFragment fragment = new ScanCodeWithURLFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_scan_code_with_url, container, false);
        textView = (TextView) view.findViewById(R.id.text);
        textView.setOnLongClickListener(this);
        editText = (EditText) view.findViewById(R.id.textinput);
        imageView = (ImageView) view.findViewById(R.id.imageView);
        imageView.setOnClickListener(this);
        imageView.setOnLongClickListener(this);

        button = (Button) view.findViewById(R.id.button);
        button.setOnClickListener(this);
        unbinder = ButterKnife.bind(this, view);
        urlInfo.setText(Html.fromHtml(getContext().getResources().getString(R.string.url_info)));

        return view;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == EncodeQRActivity.REQUEST_WRITE_PERMISSION) {
            if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "본 기능을 사용하려면 쓰기 권한이 필요합니다. 쓰기권한을 주시기 바랍니다.", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button:
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                String url = editText.getText().toString();
                if (TextUtils.isEmpty(url)) {
                    editText.setError("URL 주소를 입력해주세요.");
                    editText.requestFocus();
                    return;
                }
                if (url.length() < "http://".length() || !url.substring(0, "http".length()).equals("http")) {
                    url = "http://" + url;
                }
                new DecodeImageURLTask(url).execute();
                break;
            case R.id.imageView:
                if (imageView.getDrawable() != null) {
                    new AlertDialog.Builder(getContext()).setTitle("기능 선택").setItems(new CharSequence[]{
                            "이미지 저장", "공유", "클립보드로 복사"
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            switch (i) {
                                case 0:
                                    try {
                                        String filepath = saveQRCode(((GlideBitmapDrawable) imageView.getDrawable().getCurrent()).getBitmap());
                                        Toast.makeText(getContext(), filepath + " 로 파일을 저장하였습니다.", Toast.LENGTH_SHORT).show();
                                        MediaScannerConnection.scanFile(getContext(), new String[]{filepath}, null, null);
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                        Toast.makeText(getContext(), "파일을 저장하는데 문제가 발생하였습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                    break;
                                case 1: {
                                    if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                        Toast.makeText(getContext(), "먼저 쓰기 권한을 주시기 바랍니다", Toast.LENGTH_SHORT).show();
                                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EncodeQRActivity.REQUEST_WRITE_PERMISSION);
                                        return;
                                    }
                                    Bitmap QRCode = ((GlideBitmapDrawable) imageView.getDrawable().getCurrent()).getBitmap();
                                    String pathofBmp = MediaStore.Images.Media.insertImage(getContext().getContentResolver(), QRCode, "Created By TCQR", null);
                                    Uri bmpUri = Uri.parse(pathofBmp);
                                    final Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                    shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
                                    shareIntent.setType("image/png");
                                    startActivity(shareIntent);
                                    break;
                                }
                                case 2: {
                                    if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                        Toast.makeText(getContext(), "먼저 쓰기 권한을 주시기 바랍니다", Toast.LENGTH_SHORT).show();
                                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EncodeQRActivity.REQUEST_WRITE_PERMISSION);
                                        return;
                                    }
                                    ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                                    ContentResolver cr = getContext().getContentResolver();
                                    Bitmap QRCode = ((GlideBitmapDrawable) imageView.getDrawable().getCurrent()).getBitmap();
                                    String pathofBmp = MediaStore.Images.Media.insertImage(getContext().getContentResolver(), QRCode, "Created By TCQR", null);
                                    Uri bmpUri = Uri.parse(pathofBmp);
                                    ClipData clip = ClipData.newRawUri("uri", bmpUri);
//                                    ClipData clip = ClipData.newUri(getContentResolver(), "Image", bmpUri);
                                    clipboard.setPrimaryClip(clip);
                                    Toast.makeText(getContext(), "클립보드로 복사하였습니다.", Toast.LENGTH_SHORT).show();
                                    break;
                                }
                            }
                        }
                    }).create().show();
                }
                break;
        }
    }

    @Override
    public boolean onLongClick(View view) {
        switch (view.getId()) {
            case R.id.imageView:
                if (imageView.getDrawable() != null) {
                    new AlertDialog.Builder(getContext()).setTitle("기능 선택").setItems(new CharSequence[]{
                            "이미지 저장", "공유", "클립보드로 복사"
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            switch (i) {
                                case 0:
                                    try {
                                        String filepath = saveQRCode(((GlideBitmapDrawable) imageView.getDrawable().getCurrent()).getBitmap());
                                        Toast.makeText(getContext(), filepath + " 로 파일을 저장하였습니다.", Toast.LENGTH_SHORT).show();
                                        MediaScannerConnection.scanFile(getContext(), new String[]{filepath}, null, null);
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                        Toast.makeText(getContext(), "파일을 저장하는데 문제가 발생하였습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                    break;
                                case 1: {
                                    if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                        Toast.makeText(getContext(), "먼저 쓰기 권한을 주시기 바랍니다", Toast.LENGTH_SHORT).show();
                                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EncodeQRActivity.REQUEST_WRITE_PERMISSION);
                                        return;
                                    }
                                    Bitmap QRCode = ((GlideBitmapDrawable) imageView.getDrawable().getCurrent()).getBitmap();
                                    String pathofBmp = MediaStore.Images.Media.insertImage(getContext().getContentResolver(), QRCode, "Created By TCQR", null);
                                    Uri bmpUri = Uri.parse(pathofBmp);
                                    final Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                    shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
                                    shareIntent.setType("image/png");
                                    startActivity(shareIntent);
                                    break;
                                }
                                case 2: {
                                    if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                        Toast.makeText(getContext(), "먼저 쓰기 권한을 주시기 바랍니다", Toast.LENGTH_SHORT).show();
                                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EncodeQRActivity.REQUEST_WRITE_PERMISSION);
                                        return;
                                    }
                                    ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                                    ContentResolver cr = getContext().getContentResolver();
                                    Bitmap QRCode = ((GlideBitmapDrawable) imageView.getDrawable().getCurrent()).getBitmap();
                                    String pathofBmp = MediaStore.Images.Media.insertImage(getContext().getContentResolver(), QRCode, "Created By TCQR", null);
                                    Uri bmpUri = Uri.parse(pathofBmp);
                                    ClipData clip = ClipData.newRawUri("uri", bmpUri);
//                                    ClipData clip = ClipData.newUri(getContentResolver(), "Image", bmpUri);
                                    clipboard.setPrimaryClip(clip);
                                    Toast.makeText(getContext(), "클립보드로 복사하였습니다.", Toast.LENGTH_SHORT).show();
                                    break;
                                }
                            }
                        }
                    }).create().show();
                }
                break;
            case R.id.text:
                if (!TextUtils.isEmpty(textView.getText().toString())) {
                    new AlertDialog.Builder(getContext()).setTitle("기능 선택").setItems(new CharSequence[]{
                            "클립보드로 복사"
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (i == 0) {
                                ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                                ContentResolver cr = getContext().getContentResolver();
                                ClipData clip = ClipData.newPlainText("TCQRText", textView.getText().toString());
                                clipboard.setPrimaryClip(clip);
                                Toast.makeText(getContext(), "클립보드로 복사하였습니다", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).show();
                }
                return true;
        }
        return false;
    }

    private String saveQRCode(Bitmap bitmap) throws FileNotFoundException {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EncodeQRActivity.REQUEST_WRITE_PERMISSION);
        }
        String ex_storage = Environment.getExternalStorageDirectory().getAbsolutePath();
        String foler_name = "/TCQR/Download/";
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


    class DecodeImageURLTask extends AsyncTask<Void, Void, String> {
        private ProgressDialog progressDialog;
        private String url;

        public DecodeImageURLTask(String url) {
            super();
            this.url = url;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Glide.with(getContext()).load(url).into(imageView);
            progressDialog = ProgressDialog.show(getContext(), "이미지를 가져오는 중입니다", "잠시만 기다려주세요");
        }

        @Override
        protected String doInBackground(Void... voids) {
            Bitmap bitmap = null;
            String ret = "";
            try {
                bitmap = Glide.with(getContext()).load(url).asBitmap().listener(new RequestListener<String, Bitmap>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                        Toast.makeText(getContext(), "이미지를 불러오는데 문제가 발생하였습니다. 정확한 주소를 입력하였는지 다시 확인해주세요", Toast.LENGTH_SHORT).show();
                        cancel(true);
                        return true;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        return false;
                    }
                }).into(-1, -1).get();


                int width = bitmap.getWidth(), height = bitmap.getHeight();
                int[] pixels = new int[width * height];
                bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
                RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
                BinaryBitmap bBitmap = new BinaryBitmap(new HybridBinarizer(source));
                Hashtable hints = new Hashtable();
                hints.put(DecodeHintType.CHARACTER_SET, "ISO-8859-1");
                Result result = new QRCodeReader().decode(bBitmap, hints);
                ret = result.toString();
            } catch (InterruptedException | ExecutionException | FormatException | ChecksumException | NotFoundException e) {
                e.printStackTrace();
                ret = "";
            }
            return ret;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            if (result != null && !result.isEmpty()) {
                try {
                    if (result.length() > ("TCQREncoded:" + (char) 0x04).length() && result.substring(0, ("TCQREncoded:" + (char) 0x04).length()).equals(("TCQREncoded:" + (char) 0x04))) {
                        String compressedValue = result.substring(("TCQREncoded:" + (char) 0x04).length());
                        Toast.makeText(getContext(), "본 내용이 TCQR로 압축됨을 인식하였습니다. 압축해제를 합니다", Toast.LENGTH_SHORT).show();
                        decompressTextTask = new DecompressTextTask(compressedValue.length());
                        decompressTextTask.execute(compressedValue.getBytes("ISO-8859-1"));
                    } else {
                        textView.setText(new String(result.toString().getBytes("ISO-8859-1"), "UTF-8"));
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                textView.setText(result);
            } else {
                textView.setText(" ");
                Toast.makeText(getContext(), "인식에 문제가 발생하였습니다. 위의 이미지가   QR코드가 포함된 사진인지 다시 확인해보세요", Toast.LENGTH_SHORT).show();
            }

        }
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteractionURL(Result result);
    }

    class DecompressTextTask extends AsyncTask<byte[], Integer, String> {
        private ProgressDialog progressDialog;
        private int size;

        public DecompressTextTask(int size) {
            super();
            this.size = size;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMessage("문자를 압축헤제하는 중입니다");
            progressDialog.setCancelable(false);
            progressDialog.setMax(size / 4096);
            progressDialog.setMessage("잠시만 기다려주세요");
            progressDialog.show();
//            progressDialog = ProgressDialog.show(EncodeActivity.this, "문자를 압축하는 중입니다", "잠시만 기다려주세요.");
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progressDialog.setProgress(values[0]);
        }

        @Override
        protected String doInBackground(byte[]... bytes) {
            String result = null;
            try {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] buf = new byte[4096];
                byte[] res = bytes[0];
                ByteArrayInputStream c_input = new ByteArrayInputStream(bytes[0]);
                XZInputStream xz_input = new XZInputStream(c_input);
                StringBuffer sb = new StringBuffer();
                int read;
                int progress = 0;
                while ((read = xz_input.read(buf)) != -1) {
                    sb.append(new String(buf, 0, read));
                    publishProgress(++progress);
                }
                result = sb.toString();
            } catch (IOException e) {
                e.printStackTrace();
                result = null;
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (result != null) {
                textView.setText(result);
            }

        }
    }

}
