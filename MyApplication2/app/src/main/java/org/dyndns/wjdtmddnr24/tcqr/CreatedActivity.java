package org.dyndns.wjdtmddnr24.tcqr;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;

import org.dyndns.wjdtmddnr24.tcqr.Util.QRCodeUtils;
import org.dyndns.wjdtmddnr24.tcqr.model.QRCode;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class CreatedActivity extends AppCompatActivity {

    @BindView(R.id.qr_recent_recyclerview)
    RecyclerView qrRecentRecyclerview;
    @BindView(R.id.qr_recent_progress)
    ProgressBar qrRecentProgress;
    private Unbinder unbinder;
    private ArrayList<QRCode> qrCodes;
    private RecentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_created);
        unbinder = ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("최근 기록");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        qrRecentRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        qrCodes = new ArrayList<>();
        adapter = new RecentAdapter(qrCodes);
        qrRecentRecyclerview.setAdapter(adapter);
        RecentTask recentTask = new RecentTask();
        recentTask.execute();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    class RecentAdapter extends RecyclerView.Adapter<RecentAdapter.ViewHolder> {
        private ArrayList<QRCode> qrCodes;

        public RecentAdapter(ArrayList<QRCode> qrCodes) {
            this.qrCodes = qrCodes;
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.qr_recent_imageview)
            ImageView qrRecentImageview;
            @BindView(R.id.qr_recent_content)
            TextView qrRecentContent;
            @BindView(R.id.qr_recent_date)
            TextView qrRecentDate;
            @BindView(R.id.qr_recent_cardview)
            CardView qrRecentCardview;

            public ViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }

        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recent_qr, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Glide.with(CreatedActivity.this).load(qrCodes.get(position).getImage()).into(holder.qrRecentImageview);
            holder.qrRecentContent.setText(qrCodes.get(position).getText());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy년 MM월 DD일 생성");
            holder.qrRecentDate.setText(simpleDateFormat.format(new Date(qrCodes.get(position).getImage().lastModified())));
            holder.qrRecentCardview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(CreatedActivity.this, QRCodeInfoActivity.class);
                    intent.putExtra("text", qrCodes.get(position).getText());
                    ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(CreatedActivity.this, holder.qrRecentContent, "content");
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
            String path = Environment.getExternalStorageDirectory().toString() + "/TCQR/Create";
            File file = new File(path);
            for (File f : file.listFiles()) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                Bitmap bitmap = BitmapFactory.decodeFile(f.getPath());
                if (bitmap != null) {
                    try {
                        QRCode qrCode = new QRCode(QRCodeUtils.DecodeToResult(bitmap));
                        qrCode.setImage(f);
                        qrCodes.add(qrCode);
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
            if (qrCodes.size() != 0) {
                adapter.notifyDataSetChanged();
            }
            if (qrRecentProgress.getVisibility() != View.INVISIBLE) {
                qrRecentProgress.setVisibility(View.INVISIBLE);
            }
            if (qrRecentRecyclerview.getVisibility() != View.VISIBLE) {
                qrRecentRecyclerview.setVisibility(View.VISIBLE);
            }
        }
    }

}
