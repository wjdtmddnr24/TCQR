package org.dyndns.wjdtmddnr24.tcqr.Fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import org.dyndns.wjdtmddnr24.tcqr.databinding.FragmentScanCodeWithUrlBinding;
import org.dyndns.wjdtmddnr24.tcqr.model.QRCode;

import java.io.UnsupportedEncodingException;

public class ScanCodeWithURLFragment extends Fragment implements View.OnClickListener {

    private FragmentScanCodeWithUrlBinding binding;
    private OnFragmentInteractionListener mListener;
    private QRCode qrCode;

    public void setQrCode(QRCode qrCode) {
        this.qrCode = qrCode;
    }

    public ScanCodeWithURLFragment() {
    }

    public static ScanCodeWithURLFragment newInstance() {
        return new ScanCodeWithURLFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentScanCodeWithUrlBinding.inflate(inflater, container, false);
        binding.urlInfo.setText(Html.fromHtml(getContext().getResources().getString(R.string.url_info), Html.FROM_HTML_MODE_LEGACY));
        binding.albumImageview.setOnClickListener(this);
        binding.urlGo.setOnClickListener(this);
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

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.album_imageview) {
            if (qrCode != null) {
                mListener.showQRCodeInfo(qrCode);
            }
        } else if (viewId == R.id.url_go) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

            if (TextUtils.isEmpty(binding.urlAddress.getText())) {
                binding.urlAddress.setError(getString(R.string.insert_url_here));
                return;
            }
            String url = binding.urlAddress.getText().toString();
            if (url.length() > "http://".length() && !url.substring(0, "http://".length()).equals("http://")) {
                url = "http://" + url;
            }
            Glide.with(this).asBitmap().load(url).into(new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    binding.albumImageview.setImageBitmap(resource);
                    try {
                        Result result = QRCodeUtils.DecodeToResult(resource);
                        mListener.onFragmentInteractionURL(result);
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
                    Toast.makeText(getContext(), R.string.decode_error_load_image, Toast.LENGTH_SHORT).show();
                    qrCode = null;
                }
            });
        }
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteractionURL(Result result);

        void showQRCodeInfo(QRCode qrCode);
    }

}
