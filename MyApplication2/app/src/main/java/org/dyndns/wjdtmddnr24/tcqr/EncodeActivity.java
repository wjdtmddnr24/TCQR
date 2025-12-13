package org.dyndns.wjdtmddnr24.tcqr;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityOptionsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.dyndns.wjdtmddnr24.tcqr.databinding.ActivityEncodeBinding;

public class EncodeActivity extends AppCompatActivity {

    private static final int REQUEST_WRITE_PERMISSION = 100;
    private ActivityEncodeBinding binding;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEncodeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Toolbar toolbar = binding.toolbar;
        toolbar.setTitle(R.string.title2);
        setSupportActionBar(toolbar);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        FloatingActionButton fab = binding.fab;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                render();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.contentEncode.edittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                binding.contentEncode.textinput.setHint(getString(R.string.encode_count) + String.valueOf(s.length()));
                if (s.length() > 2000) {
                    if (!binding.contentEncode.compressmode.isChecked()) {
                        binding.contentEncode.compressmode.setChecked(true);
                        Toast.makeText(EncodeActivity.this, R.string.encode_compress_required, Toast.LENGTH_SHORT).show();
                    }
                    binding.contentEncode.compressmode.setEnabled(false);
                } else {
                    if (!binding.contentEncode.compressmode.isEnabled()) {
                        binding.contentEncode.compressmode.setEnabled(true);
                    }
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void render() {
        String value = binding.contentEncode.edittext.getText().toString();
        if (TextUtils.isEmpty(value)) {
            binding.contentEncode.edittext.setError(getString(R.string.encode_no_input));
            binding.contentEncode.edittext.requestFocus();
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putString("encode_compressed", String.valueOf(binding.contentEncode.compressmode.isChecked()));
        bundle.putString("encode_size", String.valueOf(value.length()));
        mFirebaseAnalytics.logEvent("Encode", bundle);

        Intent intent = new Intent(EncodeActivity.this, RenderActivity.class);
        intent.putExtra("value", value);
        intent.putExtra("compress", binding.contentEncode.compressmode.isChecked());
        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(EncodeActivity.this, binding.contentEncode.edittext, "content");
        startActivity(intent, optionsCompat.toBundle());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

}
