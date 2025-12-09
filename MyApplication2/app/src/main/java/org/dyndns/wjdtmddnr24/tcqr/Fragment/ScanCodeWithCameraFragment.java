package org.dyndns.wjdtmddnr24.tcqr.Fragment;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.google.zxing.qrcode.QRCodeReader;

import org.dyndns.wjdtmddnr24.tcqr.R;
import org.dyndns.wjdtmddnr24.tcqr.databinding.FragmentScanCodeWithCameraBinding;
import org.tukaani.xz.XZInputStream;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

public class ScanCodeWithCameraFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener {
    private FragmentScanCodeWithCameraBinding binding;
    private DecompressTextTask decompressTextTask = null;

    private OnFragmentInteractionListener mListener;

    public ScanCodeWithCameraFragment() {
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult QRresult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (QRresult != null) {
            if (QRresult.getContents() != null && !QRresult.getContents().isEmpty()) {
                mListener.onFragmentInteractionCamera(null);
            } else {
                Toast.makeText(getContext(), "스캔을 취소하였습니다.", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public static ScanCodeWithCameraFragment newInstance() {
        return new ScanCodeWithCameraFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentScanCodeWithCameraBinding.inflate(inflater, container, false);
        binding.text.setOnLongClickListener(this);
        binding.imageView.setOnClickListener(this);
        binding.imageView.setOnLongClickListener(this);
        binding.button.setOnClickListener(this);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.button) {
            IntentIntegrator intentIntegrator = IntentIntegrator.forSupportFragment(this);
            intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
            intentIntegrator.setBarcodeImageEnabled(true);
            intentIntegrator.setBeepEnabled(true);
            intentIntegrator.setPrompt("카메라에 QR코드를 맞추어주세요.");
            intentIntegrator.initiateScan();
        } else if (viewId == R.id.imageView) {
            if (binding.imageView.getDrawable() != null) {
                new AlertDialog.Builder(getContext()).setTitle("기능 선택").setItems(new CharSequence[]{
                        "이미지 저장", "공유", "클립보드로 복사"
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            try {
                                File file = saveQRCode(((BitmapDrawable) binding.imageView.getDrawable()).getBitmap());
                                Toast.makeText(getContext(), file.getAbsolutePath() + " 로 파일을 저장하였습니다.", Toast.LENGTH_SHORT).show();
                                MediaScannerConnection.scanFile(getContext(), new String[]{file.getAbsolutePath()}, null, null);
                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(getContext(), "파일을 저장하는데 문제가 발생하였습니다.", Toast.LENGTH_SHORT).show();
                            }
                        } else if (i == 1) {
                            try {
                                File file = saveQRCode(((BitmapDrawable) binding.imageView.getDrawable()).getBitmap());
                                Uri bmpUri = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".provider", file);
                                final Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
                                shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
                                shareIntent.setType("image/png");
                                startActivity(shareIntent);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        } else if (i == 2) {
                            try {
                                File file = saveQRCode(((BitmapDrawable) binding.imageView.getDrawable()).getBitmap());
                                Uri bmpUri = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".provider", file);
                                ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newUri(getContext().getContentResolver(), "Image", bmpUri);
                                clipboard.setPrimaryClip(clip);
                                Toast.makeText(getContext(), "클립보드로 복사하였습니다.", Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).create().show();
            }
        }

    }

    private File saveQRCode(Bitmap bitmap) throws IOException {
        File storageDir = getContext().getExternalFilesDir("TCQR/Camera/");
        if (storageDir != null && !storageDir.exists()) {
            storageDir.mkdirs();
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmm");
        Date date = new Date();
        String file_name = dateFormat.format(date) + ".jpg";
        File file = new File(storageDir, file_name);
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
        return file;
    }

    @Override
    public boolean onLongClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.imageView) {
            if (binding.imageView.getDrawable() != null) {
                new AlertDialog.Builder(getContext()).setTitle("기능 선택").setItems(new CharSequence[]{
                        "이미지 저장", "공유", "클립보드로 복사"
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            try {
                                File file = saveQRCode(((BitmapDrawable) binding.imageView.getDrawable()).getBitmap());
                                Toast.makeText(getContext(), file.getAbsolutePath() + " 로 파일을 저장하였습니다.", Toast.LENGTH_SHORT).show();
                                MediaScannerConnection.scanFile(getContext(), new String[]{file.getAbsolutePath()}, null, null);
                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(getContext(), "파일을 저장하는데 문제가 발생하였습니다.", Toast.LENGTH_SHORT).show();
                            }
                        } else if (i == 1) {
                            try {
                                File file = saveQRCode(((BitmapDrawable) binding.imageView.getDrawable()).getBitmap());
                                Uri bmpUri = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".provider", file);
                                final Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
                                shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
                                shareIntent.setType("image/png");
                                startActivity(shareIntent);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        } else if (i == 2) {
                            try {
                                File file = saveQRCode(((BitmapDrawable) binding.imageView.getDrawable()).getBitmap());
                                Uri bmpUri = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".provider", file);
                                ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newUri(getContext().getContentResolver(), "Image", bmpUri);
                                clipboard.setPrimaryClip(clip);
                                Toast.makeText(getContext(), "클립보드로 복사하였습니다.", Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).create().show();
            }
        } else if (viewId == R.id.text) {
            if (!TextUtils.isEmpty(binding.text.getText().toString())) {
                new AlertDialog.Builder(getContext()).setTitle("기능 선택").setItems(new CharSequence[]{
                        "클립보드로 복사"
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("TCQRText", binding.text.getText().toString());
                            clipboard.setPrimaryClip(clip);
                            Toast.makeText(getContext(), "클립보드로 복사하였습니다", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).show();
            }
            return true;
        }
        return false;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteractionCamera(Uri uri);
    }

    class DecompressTextTask extends AsyncTask<byte[], Integer, String> {
        private ProgressDialog progressDialog;
        private int size;

        public DecompressTextTask(int size) {
            super();
            this.size = size;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMessage("문자를 압축헤제하는 중입니다");
            progressDialog.setCancelable(false);
            progressDialog.setMax(size / 4096);
            progressDialog.setMessage("잠시만 기다려주세요");
            progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progressDialog.setProgress(values[0]);
        }

        @Override
        protected String doInBackground(byte[]... bytes) {
            String result = null;
            try {
                byte[] buf = new byte[4096];
                ByteArrayInputStream c_input = new ByteArrayInputStream(bytes[0]);
                XZInputStream xz_input = new XZInputStream(c_input);
                StringBuilder sb = new StringBuilder();
                int read;
                int progress = 0;
                while ((read = xz_input.read(buf)) != -1) {
                    sb.append(new String(buf, 0, read));
                    publishProgress(++progress);
                }
                result = sb.toString();
            } catch (IOException e) {
                e.printStackTrace();
                result = null;
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (result != null) {
                binding.text.setText(result);
            }

        }
    }

}
