package org.dyndns.wjdtmddnr24.tcqr.Fragment;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import org.dyndns.wjdtmddnr24.tcqr.EncodeActivity;
import org.dyndns.wjdtmddnr24.tcqr.R;
import org.tukaani.xz.XZInputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;

public class ScanCodeWithGalleryFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener {

    private static final int REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 10;
    private OnFragmentInteractionListener mListener;
    private TextView textView;
    private Button button;
    private Button button2;
    public static final int REQUEST_GALLERY_IMAGE_CROP = 1;
    public static final int REQUEST_READ_EXTERNAL_STORAGE = 1;

    private ImageView imageView;
    private DecompressTextTask decompressTextTask;


    public ScanCodeWithGalleryFragment() {
    }

    public static ScanCodeWithGalleryFragment newInstance() {
        ScanCodeWithGalleryFragment fragment = new ScanCodeWithGalleryFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan_code_with_gallery, container, false);
        textView = (TextView) view.findViewById(R.id.text);
        textView.setOnLongClickListener(this);
        imageView = (ImageView) view.findViewById(R.id.imageView);
        button = (Button) view.findViewById(R.id.button);
        button2 = (Button) view.findViewById(R.id.button2);
        button2.setOnClickListener(this);
        button.setOnClickListener(this);
        return view;
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

    private void clipboardDecode() throws IOException {
        ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ContentResolver cr = getContext().getContentResolver();
        ClipData clip = clipboard.getPrimaryClip();
        if (clip != null) {
            ClipData.Item item = clip.getItemAt(0);
            Uri pasteUri = item.getUri();
            if (pasteUri != null) {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), pasteUri);
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                    try {
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
                    } catch (FormatException | ChecksumException | NotFoundException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "인식에 문제가 발생하였습니다. 위의 이미지가 QR코드가 포함된 사진인지 다시 확인해보세요", Toast.LENGTH_SHORT).show();
                        textView.setText("");
                    }
                } else {
                    Toast.makeText(getContext(), "클립보드에서 이미지를 가져오는데 문제가 발생하였습니다", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "클립보드에서 이미지를 가져올 수 없습니다", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "클립보드에서 이미지가 없습니다", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "본 기능을 사용하려면 읽기 권한이 필요합니다. 읽기권한을 주시기 바랍니다", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    clipboardDecode();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "이미지를 선택하세요"), REQUEST_GALLERY_IMAGE_CROP);
                break;
            case R.id.button2:
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE);
                } else {
                    try {
                        clipboardDecode();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_GALLERY_IMAGE_CROP) {
                Bitmap bitmap = null;
                Uri uri = data.getData();
                if (uri != null) {
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri);
                        Glide.with(this).load(uri).asBitmap().into(imageView);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "이미지를 불러오는데 문제가 발생하였습니다", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    bitmap = (Bitmap) data.getExtras().get("data");
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                    }
                }
                if (bitmap == null) {
                    Toast.makeText(getContext(), "이미지를 불러오는데 문제가 발생하였습니다", Toast.LENGTH_SHORT).show();
                } else {
                    try {
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
                    } catch (FormatException | ChecksumException | NotFoundException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "인식에 문제가 발생하였습니다. 위의 이미지가 QR코드가 포함된 사진인지 다시 확인해보세요", Toast.LENGTH_SHORT).show();
                        textView.setText("");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            Toast.makeText(getContext(), "취소", Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    public boolean onLongClick(View view) {
        switch (view.getId()) {
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
        void onFragmentInteractionGallery(Uri uri);
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
