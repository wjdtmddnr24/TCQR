package org.dyndns.wjdtmddnr24.tcqr;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.dyndns.wjdtmddnr24.tcqr.databinding.ActivityQrcodeInfoBinding;

public class QRCodeInfoActivity extends AppCompatActivity implements DialogInterface.OnClickListener {

    private ActivityQrcodeInfoBinding binding;
    private String text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQrcodeInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Toolbar toolbar = binding.toolbar;
        toolbar.setTitle(R.string.qr_info_view_text);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        text = intent.getStringExtra("text");
        binding.contentQrcodeInfo.qrText.setText(text);
        binding.contentQrcodeInfo.qrText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new AlertDialog.Builder(QRCodeInfoActivity.this).setTitle("기능 선택").setItems(new CharSequence[]{
                        getString(R.string.copy_into_clipboard)
                }, QRCodeInfoActivity.this).create().show();
                return true;
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("TCQR content", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, R.string.clipboard, Toast.LENGTH_SHORT).show();
    }
}
