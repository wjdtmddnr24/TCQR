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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
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

    private static final int REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 10;
    private static final int REQUEST_LOAD_IMAGE = 11;
    private static final int REQUEST_WRITE_PERMISSION = 12;

    private FragmentScanCodeWithGalleryBinding binding;
    private OnFragmentInteractionListener mListener;
    public static final int REQUEST_READ_EXTERNAL_STORAGE = 1;

    private QRCode qrCode;

    public void setQrCode(QRCode qrCode) {
        this.qrCode = qrCode;
    }

    public BottomSheetBehavior getBottomsheet() {
        return null; // This is no longer used
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_LOAD_IMAGE) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null)
                Glide.with(this).asBitmap().load(selectedImageUri).into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        try {
                            binding.albumImageview.setImageBitmap(resource);
                            Result result = QRCodeUtils.DecodeToResult(resource);
                            mListener.onFragmentInteractionGallery(result);
                        } catch (FormatException | ChecksumException | UnsupportedEncodingException | NotFoundException e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), R.string.error_decode, Toast.LENGTH_SHORT).show();
                            qrCode = null;
                        }
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
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


    @Nullable
    private Intent createPickIntent() {
        Intent picImageIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (picImageIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            return picImageIntent;
        } else {
            return null;
        }
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.album_image) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) getContext(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
                return;
            }
            startActivityForResult(createPickIntent(), REQUEST_LOAD_IMAGE);
        } else if (viewId == R.id.album_clipboard) {
            try {
                Uri uri = getUrifromClipboard();
                Glide.with(this).asBitmap().load(uri).into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        try {
                            binding.albumImageview.setImageBitmap(resource);
                            Result result = QRCodeUtils.DecodeToResult(resource);
                            mListener.onFragmentInteractionGallery(result);
                        } catch (FormatException | ChecksumException | UnsupportedEncodingException | NotFoundException e) {
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
                        binding.albumImageview.setImageDrawable(null);
                        Toast.makeText(getContext(), R.string.error_load, Toast.LENGTH_SHORT).show();
                        qrCode = null;
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                binding.albumImageview.setImageDrawable(null);
                Toast.makeText(getContext(), R.string.error_load, Toast.LENGTH_SHORT).show();
                qrCode = null;
            }
        } else if (viewId == R.id.album_reset) {
            qrCode = null;
            binding.albumImageview.setImageDrawable(null);
            binding.albumImageview.setAnimation(new AlphaAnimation(0, 1));
        } else if (viewId == R.id.album_imageview) {
            if (qrCode != null) {
                mListener.showQRCodeInfo(qrCode);
            }
        }
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteractionGallery(Result result);

        void showQRCodeInfo(QRCode qrCode);
    }


    private Uri getUrifromClipboard() throws IOException {
        ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = clipboard.getPrimaryClip();
        ClipData.Item item = clip.getItemAt(0);
        return item.getUri();
    }
}
