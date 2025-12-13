package org.dyndns.wjdtmddnr24.tcqr.Fragment;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;

import org.dyndns.wjdtmddnr24.tcqr.R;
import org.dyndns.wjdtmddnr24.tcqr.Util.QRCodeUtils;
import org.dyndns.wjdtmddnr24.tcqr.databinding.FragmentScanCodeWithGalleryBinding;
import org.dyndns.wjdtmddnr24.tcqr.model.QRCode;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class ScanCodeWithGalleryFragment extends Fragment implements View.OnClickListener {

    private static final int REQUEST_PERMISSION_READ_MEDIA = 10;
    private static final int REQUEST_LOAD_IMAGE = 11;

    private FragmentScanCodeWithGalleryBinding binding;
    private OnFragmentInteractionListener mListener;

    private QRCode qrCode;

    public void setQrCode(QRCode qrCode) {
        this.qrCode = qrCode;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_LOAD_IMAGE) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null)
                decode(selectedImageUri);
        }
    }

    public ScanCodeWithGalleryFragment() {
    }

    public static ScanCodeWithGalleryFragment newInstance() {
        return new ScanCodeWithGalleryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentScanCodeWithGalleryBinding.inflate(inflater, container, false);
        binding.albumInfo.setText(Html.fromHtml(getContext().getResources().getString(R.string.album_info), Html.FROM_HTML_MODE_LEGACY));

        binding.albumImage.setOnClickListener(this);
        binding.albumClipboard.setOnClickListener(this);
        binding.albumReset.setOnClickListener(this);
        binding.albumImageview.setOnClickListener(this);

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
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


    private void openGallery() {
        Intent picImageIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (picImageIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(picImageIntent, REQUEST_LOAD_IMAGE);
        } else {
            Toast.makeText(getContext(), "Gallery not available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.album_image) {
            requestStoragePermission();
        } else if (viewId == R.id.album_clipboard) {
            loadFromClipboard();
        } else if (viewId == R.id.album_reset) {
            qrCode = null;
            if(binding != null) {
                binding.albumImageview.setImageDrawable(null);
                binding.albumImageview.setAnimation(new AlphaAnimation(0, 1));
            }
        } else if (viewId == R.id.album_imageview) {
            if (qrCode != null) {
                mListener.showQRCodeInfo(qrCode);
            }
        }
    }

    private void requestStoragePermission() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(getContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            requestPermissions(new String[]{permission}, REQUEST_PERMISSION_READ_MEDIA);
        }
    }

    private void loadFromClipboard() {
        try {
            Uri uri = getUrifromClipboard();
            if (uri != null) {
                decode(uri);
            } else {
                Toast.makeText(getContext(), R.string.error_clipboard, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if(getContext() != null) {
                Toast.makeText(getContext(), R.string.error_load, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void decode(Uri uri) {
        if (getContext() == null) return;
        Glide.with(this).asBitmap().load(uri).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                if (getContext() == null || binding == null) return;
                try {
                    binding.albumImageview.setImageBitmap(resource);
                    Result result = QRCodeUtils.DecodeToResult(resource);
                    if (mListener != null) {
                        mListener.onFragmentInteractionGallery(result);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), R.string.error_decode, Toast.LENGTH_SHORT).show();
                    qrCode = null;
                }
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                super.onLoadFailed(errorDrawable);
                 if (getContext() == null || binding == null) return;
                binding.albumImageview.setImageDrawable(null);
                Toast.makeText(getContext(), R.string.error_load, Toast.LENGTH_SHORT).show();
                qrCode = null;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_READ_MEDIA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(getContext(), "Permission denied to read storage", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteractionGallery(Result result);

        void showQRCodeInfo(QRCode qrCode);
    }


    private Uri getUrifromClipboard() {
        if (getContext() == null) return null;
        ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null && clipboard.hasPrimaryClip()) {
            ClipData clip = clipboard.getPrimaryClip();
            if (clip != null && clip.getItemCount() > 0) {
                ClipData.Item item = clip.getItemAt(0);
                return item.getUri();
            }
        }
        return null;
    }
}
