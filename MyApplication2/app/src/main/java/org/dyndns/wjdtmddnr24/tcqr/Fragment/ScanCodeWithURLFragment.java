package org.dyndns.wjdtmddnr24.tcqr.Fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

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

import java.util.concurrent.ExecutionException;


public class ScanCodeWithURLFragment extends Fragment implements View.OnClickListener {

    private OnFragmentInteractionListener mListener;
    private TextView textView;
    private Button button;
    private EditText editText;
    private ImageView imageView;
    private Handler handler;

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
        editText = (EditText) view.findViewById(R.id.edittext);
        imageView = (ImageView) view.findViewById(R.id.imageView);
        button = (Button) view.findViewById(R.id.button);
        button.setOnClickListener(this);
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteractionURL(uri);
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
                if (!url.substring(0, "http".length()).equals("http")) {
                    url = "http://" + url;
                }
                new DecodeImageURLTask(url).execute();
                break;
        }
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
                bitmap = Glide.with(getContext()).load(url).asBitmap().into(-1, -1).get();

                int width = bitmap.getWidth(), height = bitmap.getHeight();
                int[] pixels = new int[width * height];
                bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
                RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
                BinaryBitmap bBitmap = new BinaryBitmap(new HybridBinarizer(source));
                Result result = new QRCodeReader().decode(bBitmap);
                ret = result.toString() + result.getBarcodeFormat().name();
            } catch (InterruptedException | ExecutionException | FormatException | ChecksumException | NotFoundException e) {
                e.printStackTrace();
                ret = "";
            }
            return ret;
        }

        @Override
        protected void onPostExecute(String result) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (result != null && !result.isEmpty()) {
                textView.setText(result);
            } else {
                textView.setText(" ");
                imageView.setImageBitmap(null);
            }

        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteractionURL(Uri uri);
    }
}
