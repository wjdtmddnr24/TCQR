package org.dyndns.wjdtmddnr24.tcqr.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class SimpleScannerFragment extends Fragment implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;
    private OnFragmentInteractionListener mListener;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final Runnable mResumeRunnable = () -> {
        if (mScannerView != null) {
            mScannerView.resumeCameraPreview(SimpleScannerFragment.this);
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mScannerView = new ZXingScannerView(getActivity());
        return mScannerView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void handleResult(Result result) {
        if (mListener != null) {
            mListener.onFragmentInteractionSimpleCamera(result);
        }
        mHandler.postDelayed(mResumeRunnable, 2000);
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mResumeRunnable);
        mScannerView.stopCamera();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteractionSimpleCamera(Result result);
    }
}