package org.dyndns.wjdtmddnr24.tcqr;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class EncodeActivity extends AppCompatActivity {

    private static final int REQUEST_WRITE_PERMISSION = 100;
    @BindView(R.id.compressmode)
    Switch compressmode;
    @BindView(R.id.edittext)
    EditText edittext;
    @BindView(R.id.textinput)
    TextInputLayout textinput;
    private Unbinder unbinder;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encode);
        unbinder = ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.title2);
        setSupportActionBar(toolbar);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                render();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        edittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                textinput.setHint(getString(R.string.encode_count) + String.valueOf(s.length()));
                if (s.length() > 2000) {
                    if (!compressmode.isChecked()) {
                        compressmode.setChecked(true);
                        Toast.makeText(EncodeActivity.this, R.string.encode_compress_required, Toast.LENGTH_SHORT).show();
                    }
                    compressmode.setEnabled(false);
                } else {
                    if (!compressmode.isEnabled()) {
                        compressmode.setEnabled(true);
                    }
                }
            }
        });
    }

    private void render() {
        String value = edittext.getText().toString();
        if (TextUtils.isEmpty(value)) {
            edittext.setError(getString(R.string.encode_no_input));
            edittext.requestFocus();
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putString("encode_compressed", String.valueOf(compressmode.isChecked()));
        bundle.putString("encode_size", String.valueOf(value.length()));
        mFirebaseAnalytics.logEvent("Encode", bundle);

        Intent intent = new Intent(EncodeActivity.this, RenderActivity.class);
        intent.putExtra("value", value);
        intent.putExtra("compress", compressmode.isChecked());
        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(EncodeActivity.this, edittext, "content");
        startActivity(intent, optionsCompat.toBundle());
    }

    @OnClick({R.id.compressmode, R.id.edittext})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.compressmode:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @OnClick(R.id.compressmode)
    public void onClick() {
    }
}
