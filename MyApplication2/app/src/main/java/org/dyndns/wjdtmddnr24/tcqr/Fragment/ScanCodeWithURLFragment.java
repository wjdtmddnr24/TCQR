package org.dyndns.wjdtmddnr24.tcqr.Fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;

import org.dyndns.wjdtmddnr24.tcqr.R;
import org.dyndns.wjdtmddnr24.tcqr.Util.QRCodeUtils;
import org.dyndns.wjdtmddnr24.tcqr.model.QRCode;

import java.io.UnsupportedEncodingException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import lombok.Setter;


public class ScanCodeWithURLFragment extends Fragment {

    @BindView(R.id.url_info)
    TextView urlInfo;
    @BindView(R.id.album_imageview)
    ImageView albumImageview;
    @BindView(R.id.url_address)
    EditText urlAddress;
    @BindView(R.id.url_go)
    Button urlGo;
    private OnFragmentInteractionListener mListener;
    @Setter
    private QRCode qrCode;
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_scan_code_with_url, container, false);
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

    String foler_name = "/TCQR/Download/";

    @OnClick({R.id.album_imageview, R.id.url_go})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.album_imageview:
                if (qrCode != null) {
                    mListener.showQRCodeInfo(qrCode);
                }
                break;
            case R.id.url_go:
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                if (TextUtils.isEmpty(urlAddress.getText())) {
                    urlAddress.setError(getString(R.string.insert_url_here));
                    return;
                }
                String url = urlAddress.getText().toString();
                if (url.length() > "http://".length() && !url.substring(0, "http://".length()).equals("http://")) {
                    url = "http://" + url;
                }
                Glide.with(this).load(url).asBitmap().into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        albumImageview.setImageBitmap(resource);
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
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        super.onLoadFailed(e, errorDrawable);
                        Toast.makeText(getContext(), R.string.decode_error_load_image, Toast.LENGTH_SHORT).show();
                        qrCode = null;
                    }
                });
                break;
        }
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteractionURL(Result result);

        void showQRCodeInfo(QRCode qrCode);
    }

}
