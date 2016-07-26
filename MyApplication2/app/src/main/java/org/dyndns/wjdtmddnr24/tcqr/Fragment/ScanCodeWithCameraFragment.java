package org.dyndns.wjdtmddnr24.tcqr.Fragment;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.dyndns.wjdtmddnr24.tcqr.EncodeActivity;
import org.dyndns.wjdtmddnr24.tcqr.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ScanCodeWithCameraFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener {
    private Button button;
    private TextView textView;
    private ImageView imageView;

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
                textView.setText(QRresult.toString());
                Glide.with(getActivity()).load(QRresult.getBarcodeImagePath()).into(imageView);
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
        if (requestCode == EncodeActivity.REQUEST_WRITE_PERMISSION) {
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
                            "이미지 저장", "공유", "클립보드에 복사"
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
                                    ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                                    ContentResolver cr = getContext().getContentResolver();
                                    Bitmap QRCode = ((GlideBitmapDrawable) imageView.getDrawable().getCurrent()).getBitmap();
                                    String pathofBmp = MediaStore.Images.Media.insertImage(getContext().getContentResolver(), QRCode, "Created By TCQR", null);
                                    Uri bmpUri = Uri.parse(pathofBmp);
                                    ClipData clip = ClipData.newRawUri("uri", bmpUri);
//                                    ClipData clip = ClipData.newUri(getContentResolver(), "Image", bmpUri);
                                    clipboard.setPrimaryClip(clip);
                                    Toast.makeText(getContext(), "클립보드에 복사하였습니다.", Toast.LENGTH_SHORT).show();
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
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EncodeActivity.REQUEST_WRITE_PERMISSION);
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
                            "이미지 저장", "공유", "클립보드에 복사"
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
                                    ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                                    ContentResolver cr = getContext().getContentResolver();
                                    Bitmap QRCode = ((GlideBitmapDrawable) imageView.getDrawable().getCurrent()).getBitmap();
                                    String pathofBmp = MediaStore.Images.Media.insertImage(getContext().getContentResolver(), QRCode, "Created By TCQR", null);
                                    Uri bmpUri = Uri.parse(pathofBmp);
                                    ClipData clip = ClipData.newRawUri("uri", bmpUri);
//                                    ClipData clip = ClipData.newUri(getContentResolver(), "Image", bmpUri);
                                    clipboard.setPrimaryClip(clip);
                                    Toast.makeText(getContext(), "클립보드에 복사하였습니다.", Toast.LENGTH_SHORT).show();
                                    break;
                                }
                            }
                        }
                    }).create().show();
                }
                break;
        }
        return false;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteractionCamera(Uri uri);
    }
}
