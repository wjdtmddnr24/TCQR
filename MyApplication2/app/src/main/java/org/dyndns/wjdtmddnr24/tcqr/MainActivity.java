package org.dyndns.wjdtmddnr24.tcqr;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.dyndns.wjdtmddnr24.tcqr.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private ActivityMainBinding binding;
    private FirebaseAnalytics mFirebaseAnalytics;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Toolbar toolbar = binding.appBarMain.toolbar;
        setSupportActionBar(toolbar);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        DrawerLayout drawer = binding.drawerLayout;
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        binding.navView.setNavigationItemSelectedListener(this);

        binding.appBarMain.contentMain.title0.setOnClickListener(this);
        binding.appBarMain.contentMain.title1.setOnClickListener(this);
        binding.appBarMain.contentMain.title2.setOnClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_credit) {
            new AlertDialog.Builder(this).setTitle(R.string.info_developer).setMessage(R.string.credit).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        Bundle bundle = new Bundle();
        if (id == R.id.nav_recognize_decoder) {
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Read");
            startActivity(new Intent(MainActivity.this, DecodeActivity.class));
        } else if (id == R.id.nav_text_encoder) {
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Create");
            startActivity(new Intent(MainActivity.this, EncodeActivity.class));
        } else if (id == R.id.nav_recent) {
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Recent");
            startActivity(new Intent(MainActivity.this, CreatedActivity.class));
        }
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        binding.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View view) {
        Bundle bundle = new Bundle();
        int viewId = view.getId();
        if (viewId == R.id.title0) {
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Recent");
            startActivity(new Intent(MainActivity.this, CreatedActivity.class));
        } else if (viewId == R.id.title1) {
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Read");
            startActivity(new Intent(MainActivity.this, DecodeActivity.class));
        } else if (viewId == R.id.title2) {
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Create");
            startActivity(new Intent(MainActivity.this, EncodeActivity.class));
        }
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

}
