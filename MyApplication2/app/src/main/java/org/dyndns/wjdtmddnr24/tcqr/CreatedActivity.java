package org.dyndns.wjdtmddnr24.tcqr;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;

import org.dyndns.wjdtmddnr24.tcqr.Util.QRCodeUtils;
import org.dyndns.wjdtmddnr24.tcqr.databinding.ActivityCreatedBinding;
import org.dyndns.wjdtmddnr24.tcqr.databinding.ItemRecentQrBinding;
import org.dyndns.wjdtmddnr24.tcqr.model.QRCode;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class CreatedActivity extends AppCompatActivity {

    private static final int REQUEST_WRITE_PERMISSION = 101;
    private ActivityCreatedBinding binding;
    private ArrayList<QRCode> qrCodes;
    private RecentAdapter adapter;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadRecentQRCodes();
            } else {
                Toast.makeText(CreatedActivity.this, R.string.request_write_permission, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreatedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Toolbar toolbar = binding.toolbar;
        toolbar.setTitle(R.string.title3);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (ContextCompat.checkSelfPermission(CreatedActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(CreatedActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
        } else {
            loadRecentQRCodes();
        }

    }

    private void loadRecentQRCodes() {
        binding.contentCreated.qrRecentRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        qrCodes = new ArrayList<>();
        adapter = new RecentAdapter(qrCodes);
        binding.contentCreated.qrRecentRecyclerview.setAdapter(adapter);
        RecentTask recentTask = new RecentTask();
        recentTask.execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    class RecentAdapter extends RecyclerView.Adapter<RecentAdapter.ViewHolder> {
        private ArrayList<QRCode> qrCodes;

        public RecentAdapter(ArrayList<QRCode> qrCodes) {
            this.qrCodes = qrCodes;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ItemRecentQrBinding binding;

            public ViewHolder(ItemRecentQrBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }

        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ItemRecentQrBinding binding = ItemRecentQrBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Glide.with(CreatedActivity.this).load(qrCodes.get(position).getImage()).into(holder.binding.qrRecentImageview);
            holder.binding.qrRecentContent.setText(qrCodes.get(position).getText());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy년 MM월 DD일 생성");
            holder.binding.qrRecentFilename.setText(qrCodes.get(position).getFilename());
            holder.binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(CreatedActivity.this, QRCodeInfoActivity.class);
                    intent.putExtra("text", qrCodes.get(position).getText());
                    ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(CreatedActivity.this, holder.binding.qrRecentContent, "content");
                    startActivity(intent, optionsCompat.toBundle());
                }
            });
        }

        @Override
        public int getItemCount() {
            return qrCodes.size();
        }

    }

    class RecentTask extends AsyncTask<Void, Void, Void> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            File storageDir = getExternalFilesDir("TCQR/Create");
            if (storageDir != null && storageDir.exists() && storageDir.listFiles() != null) {
                for (File f : storageDir.listFiles()) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    Bitmap bitmap = BitmapFactory.decodeFile(f.getPath());
                    if (bitmap != null) {
                        try {
                            QRCode qrCode = new QRCode(QRCodeUtils.DecodeToResult(bitmap));
                            qrCode.setImage(f);
                            qrCode.setFilename(f.getName());
                            qrCodes.add(qrCode);
                        } catch (IOException | FormatException | NotFoundException | ChecksumException | IllegalArgumentException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (qrCodes.size() != 0) {
                adapter.notifyDataSetChanged();
            }
            if (binding.contentCreated.qrRecentProgress != null && binding.contentCreated.qrRecentProgress.getVisibility() != View.INVISIBLE) {
                binding.contentCreated.qrRecentProgress.setVisibility(View.INVISIBLE);
            }
            if (binding.contentCreated.qrRecentRecyclerview != null && binding.contentCreated.qrRecentRecyclerview.getVisibility() != View.VISIBLE) {
                binding.contentCreated.qrRecentRecyclerview.setVisibility(View.VISIBLE);
            }
        }
    }

}
