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
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

public class ScanCodeWithCameraFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener {
    private Button button;
    private TextView textView;
    private ImageView imageView;
    private DecompressTextTask decompressTextTask = null;

    private OnFragmentInteractionListener mListener;

    public ScanCodeWithCameraFragment() {
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult QRresult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (QRresult != null) {
            if (QRresult.getContents() != null && !QRresult.getContents().isEmpty()) {
                Log.d("tcqrres", "onActivityResult:" + QRresult.toString());
                try {
                    String path = QRresult.getBarcodeImagePath();
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), Uri.parse("file://" + QRresult.getBarcodeImagePath()));
                    int width = bitmap.getWidth(), height = bitmap.getHeight();
                    int[] pixels = new int[width * height];
                    bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
                    RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
                    BinaryBitmap bBitmap = new BinaryBitmap(new HybridBinarizer(source));
                    Hashtable hints = new Hashtable();
                    hints.put(DecodeHintType.CHARACTER_SET, "ISO-8859-1");
                    Result result = new QRCodeReader().decode(bBitmap, hints);
                    if (result.toString().length() > ("TCQREncoded:" + (char) 0x04).length() && result.toString().substring(0, ("TCQREncoded:" + (char) 0x04).length()).equals(("TCQREncoded:" + (char) 0x04))) {
                        String compressedValue = result.getText().substring(("TCQREncoded:" + (char) 0x04).length());
                        Toast.makeText(getContext(), "본 내용이 TCQR로 압축됨을 인식하였습니다. 압축해제를 합니다", Toast.LENGTH_SHORT).show();
                        decompressTextTask = new DecompressTextTask(compressedValue.length());
                        decompressTextTask.execute(compressedValue.getBytes("ISO-8859-1"));
                    } else {
                        textView.setText(new String(result.toString().getBytes("ISO-8859-1"), "UTF-8") + result.getBarcodeFormat().name());
                    }
                    Glide.with(getActivity()).load(QRresult.getBarcodeImagePath()).into(imageView);
                } catch (IOException | FormatException | NotFoundException | ChecksumException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getContext(), "스캔을 취소하였습니다.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "바코드를 스캔하는데 문제가 발생하였습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    public static ScanCodeWithCameraFragment newInstance() {
        ScanCodeWithCameraFragment fragment = new ScanCodeWithCameraFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan_code_with_camera, container, false);
        textView = (TextView) view.findViewById(R.id.text);
        textView.setOnLongClickListener(this);
        imageView = (ImageView) view.findViewById(R.id.imageView);
        imageView.setOnClickListener(this);
        imageView.setOnLongClickListener(this);
        button = (Button) view.findViewById(R.id.button);
        button.setOnClickListener(this);
        return view;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteractionCamera(uri);
        }
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
                IntentIntegrator intentIntegrator = IntentIntegrator.forSupportFragment(this);
                intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                intentIntegrator.setBarcodeImageEnabled(true);
                intentIntegrator.setBeepEnabled(true);
                intentIntegrator.setPrompt("카메라에 QR코드를 맞추어주세요.");
                intentIntegrator.initiateScan();
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
                                    final Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
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

    private String saveQRCode(Bitmap bitmap) throws FileNotFoundException {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EncodeQRActivity.REQUEST_WRITE_PERMISSION);
        }
        String ex_storage = Environment.getExternalStorageDirectory().getAbsolutePath();
        String foler_name = "/TCQR/Camera/";
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
                                    final Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
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

    public interface OnFragmentInteractionListener {
        void onFragmentInteractionCamera(Uri uri);
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
