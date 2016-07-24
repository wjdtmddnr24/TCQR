package org.dyndns.wjdtmddnr24.tcqr.Fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.dyndns.wjdtmddnr24.tcqr.R;

public class ScanCodeWithCameraFragment extends Fragment implements View.OnClickListener {
    private Button button;
    private TextView textView;

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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button:
                IntentIntegrator intentIntegrator = IntentIntegrator.forSupportFragment(this);
                intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                intentIntegrator.setPrompt("카메라에 QR코드를 맞추어주세요.");
                intentIntegrator.initiateScan();
                break;
        }

    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteractionCamera(Uri uri);
    }
}
