package org.dyndns.wjdtmddnr24.tcqr;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityOptionsCompat;
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

    private ActivityCreatedBinding binding;
    private ArrayList<QRCode> qrCodes;
    private RecentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreatedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Toolbar toolbar = binding.toolbar;
        toolbar.setTitle(R.string.title3);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadRecentQRCodes();
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

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
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
                    try {
                        // Decode with inJustDecodeBounds=true to check dimensions
                        final BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(f.getAbsolutePath(), options);

                        // Calculate inSampleSize
                        options.inSampleSize = calculateInSampleSize(options, 512, 512);

                        // Decode bitmap with inSampleSize set
                        options.inJustDecodeBounds = false;
                        Bitmap bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(), options);

                        if (bitmap != null) {
                            QRCode qrCode = new QRCode(QRCodeUtils.DecodeToResult(bitmap));
                            qrCode.setImage(f);
                            qrCode.setFilename(f.getName());
                            qrCodes.add(qrCode);
                            bitmap.recycle(); // Recycle the bitmap to free memory
                        }
                    } catch (IOException | FormatException | NotFoundException | ChecksumException | IllegalArgumentException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (binding == null) return; // view is already destroyed
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
