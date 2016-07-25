package org.dyndns.wjdtmddnr24.tcqr.Fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import org.dyndns.wjdtmddnr24.tcqr.R;

import java.io.File;
import java.io.IOException;

public class ScanCodeWithGalleryFragment extends Fragment implements View.OnClickListener {

    private static final int REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 10;
    private OnFragmentInteractionListener mListener;
    private TextView textView;
    private Button button;
    public static final int REQUEST_GALLERY_IMAGE_CROP = 1;
    private ImageView imageView;


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
        imageView = (ImageView) view.findViewById(R.id.imageView);
        button = (Button) view.findViewById(R.id.button);
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "이미지를 선택하세요"), REQUEST_GALLERY_IMAGE_CROP);
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
                        Result result = new QRCodeReader().decode(bBitmap);
                        textView.setText(result.toString() + result.getBarcodeFormat().name());
                    } catch (FormatException | ChecksumException | NotFoundException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "인식에 문제가 발생하였습니다. 위의 이미지가 QR코드가 포함된 사진인지 다시 확인해보세요", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } else {
            imageView.setImageBitmap(null);
            Toast.makeText(getContext(), "취소", Toast.LENGTH_SHORT).show();

        }
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteractionGallery(Uri uri);
    }
}
