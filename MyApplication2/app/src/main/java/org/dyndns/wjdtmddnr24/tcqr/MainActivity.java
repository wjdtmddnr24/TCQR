package org.dyndns.wjdtmddnr24.tcqr;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;

import org.dyndns.wjdtmddnr24.tcqr.Fragment.ScanCodeWithCameraFragment;
import org.dyndns.wjdtmddnr24.tcqr.Fragment.ScanCodeWithGalleryFragment;
import org.dyndns.wjdtmddnr24.tcqr.Fragment.ScanCodeWithURLFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import lombok.core.Main;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @BindView(R.id.title1)
    CardView title1;
    @BindView(R.id.title2)
    CardView title2;
    private Unbinder unbinder;
    private FirebaseAnalytics mFirebaseAnalytics;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        unbinder = ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_credit) {
            new AlertDialog.Builder(this).setTitle(R.string.info_developer).setMessage(R.string.credit).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Bundle bundle = new Bundle();
        switch (id) {
            case R.id.nav_recognize_decoder:
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Read");
                startActivity(new Intent(MainActivity.this, DecodeActivity.class));
                break;
            case R.id.nav_text_encoder:
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Create");
                startActivity(new Intent(MainActivity.this, EncodeActivity.class));
                break;
            case R.id.nav_recent:
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Recent");
                startActivity(new Intent(MainActivity.this, CreatedActivity.class));
                break;
        }
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @OnClick({R.id.title0, R.id.title1, R.id.title2})
    public void onClick(View view) {
        Bundle bundle = new Bundle();
        switch (view.getId()) {
            case R.id.title0:
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Recent");
                startActivity(new Intent(MainActivity.this, CreatedActivity.class));
                break;
            case R.id.title1:
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Read");
                startActivity(new Intent(MainActivity.this, DecodeActivity.class));
                break;
            case R.id.title2:
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Create");
                startActivity(new Intent(MainActivity.this, EncodeActivity.class));
                break;
        }
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

}
