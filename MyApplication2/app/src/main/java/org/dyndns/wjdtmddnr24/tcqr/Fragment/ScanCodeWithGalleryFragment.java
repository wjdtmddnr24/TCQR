package org.dyndns.wjdtmddnr24.tcqr.Fragment;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.flipboard.bottomsheet.BottomSheetLayout;
import com.flipboard.bottomsheet.commons.ImagePickerSheetView;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;

import org.dyndns.wjdtmddnr24.tcqr.R;
import org.dyndns.wjdtmddnr24.tcqr.Util.QRCodeUtils;
import org.dyndns.wjdtmddnr24.tcqr.model.QRCode;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import lombok.Getter;
import lombok.Setter;

public class ScanCodeWithGalleryFragment extends Fragment {

    private static final int REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 10;
    private static final int REQUEST_LOAD_IMAGE = 11;
    private static final int REQUEST_WRITE_PERMISSION = 12;

    @BindView(R.id.album_info)
    TextView albumInfo;
    @BindView(R.id.album_imageview)
    ImageView albumImageview;
    @BindView(R.id.album_image)
    Button albumImage;
    @BindView(R.id.album_clipboard)
    Button albumClipboard;
    @BindView(R.id.album_reset)
    Button albumReset;
    @Getter
    @BindView(R.id.bottomsheet)
    BottomSheetLayout bottomsheet;
    private OnFragmentInteractionListener mListener;
    public static final int REQUEST_READ_EXTERNAL_STORAGE = 1;

    private Unbinder unbinder;
    private ImagePickerSheetView imagePickerSheetView;
    @Setter
    private QRCode qrCode;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_LOAD_IMAGE) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null)
                Glide.with(this).load(selectedImageUri).asBitmap().into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        try {
                            albumImageview.setImageBitmap(resource);
                            Result result = QRCodeUtils.DecodeToResult(resource);
                            mListener.onFragmentInteractionGallery(result);
                        } catch (FormatException | ChecksumException | UnsupportedEncodingException | NotFoundException e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), R.string.error_decode, Toast.LENGTH_SHORT).show();
                            qrCode = null;
                        }
                    }
                });
        }
    }

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
        unbinder = ButterKnife.bind(this, view);
        albumInfo.setText(Html.fromHtml(getContext().getResources().getString(R.string.album_info)));
        return view;
    }

    private ImagePickerSheetView getImagePickerSheetView() {
        return imagePickerSheetView = new ImagePickerSheetView.Builder(getContext()).setMaxItems(15).setShowPickerOption(createPickIntent() != null).setShowCameraOption(false).setImageProvider((imageView, imageUri, size) -> Glide.with(getContext()).load(imageUri).centerCrop().crossFade().into(imageView)).setOnTileSelectedListener(selectedTile -> {
            bottomsheet.dismissSheet();
            if (selectedTile.isPickerTile()) {
                startActivityForResult(createPickIntent(), REQUEST_LOAD_IMAGE);
            } else if (selectedTile.isImageTile()) {
                showSelectedImage(selectedTile.getImageUri());
            }
        }).setTitle(R.string.decode_select_image).create();
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


    @Nullable
    private Intent createPickIntent() {
        Intent picImageIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (picImageIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            return picImageIntent;
        } else {
            return null;
        }
    }

    private void showSelectedImage(Uri selectedImageUri) {
        albumImageview.setImageDrawable(null);
        Glide.with(this).load(selectedImageUri).into(albumImageview);
        Glide.with(this).load(selectedImageUri).asBitmap().into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                try {
                    Result result = QRCodeUtils.DecodeToResult(resource);
                    mListener.onFragmentInteractionGallery(result);
                } catch (FormatException | ChecksumException | UnsupportedEncodingException | NotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), R.string.error_decode, Toast.LENGTH_SHORT).show();
                    qrCode = null;
                }
            }
        });
    }


    @OnClick({R.id.album_image, R.id.album_clipboard, R.id.album_reset, R.id.album_imageview})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.album_image: {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions((Activity) getContext(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
                    return;
                }
                if (imagePickerSheetView == null) {
                    imagePickerSheetView = getImagePickerSheetView();
                }
                bottomsheet.showWithSheetView(imagePickerSheetView);
                break;
            }
            case R.id.album_clipboard:
                try {
                    Uri uri = getUrifromClipboard();
                    Glide.with(this).load(uri).asBitmap().into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            try {
                                albumImageview.setImageBitmap(resource);
                                Result result = QRCodeUtils.DecodeToResult(resource);
                                mListener.onFragmentInteractionGallery(result);
                            } catch (FormatException | ChecksumException | UnsupportedEncodingException | NotFoundException e) {
                                e.printStackTrace();
                                Toast.makeText(getContext(), R.string.error_decode, Toast.LENGTH_SHORT).show();
                                qrCode = null;
                            }
                        }

                        @Override
                        public void onLoadFailed(Exception e, Drawable errorDrawable) {
                            super.onLoadFailed(e, errorDrawable);
                            albumImageview.setImageDrawable(null);
                            Toast.makeText(getContext(), R.string.error_load, Toast.LENGTH_SHORT).show();
                            qrCode = null;
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    albumImageview.setImageDrawable(null);
                    Toast.makeText(getContext(), R.string.error_load, Toast.LENGTH_SHORT).show();
                    qrCode = null;
                }
                break;
            case R.id.album_reset:
                qrCode = null;
                albumImageview.setImageDrawable(null);
                albumImageview.setAnimation(new AlphaAnimation(0, 1));
                break;
            case R.id.album_imageview:
                if (qrCode != null) {
                    mListener.showQRCodeInfo(qrCode);
                }
                break;
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
        Uri pasteUri = item.getUri();
//        return MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), pasteUri);
        return pasteUri;
    }


}
