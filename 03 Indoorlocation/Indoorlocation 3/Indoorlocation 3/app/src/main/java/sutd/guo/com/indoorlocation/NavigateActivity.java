package sutd.guo.com.indoorlocation;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AccelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import sutd.guo.com.indoorlocation.main.MonitorService;
import sutd.guo.com.indoorlocation.main.Util;
import sutd.guo.com.indoorlocation.types.Steps;
import sutd.guo.com.indoorlocation.util.AlarmUtil;
import sutd.guo.com.indoorlocation.util.MySharedpreference;

public class NavigateActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    SensorManager mSensorManager;
    private Sensor orientation, accelerate, magnetic, pressure;
    private static final String TAG = "IMUActivity";

    private static final String ACTION_WIFI = "WIFI_ONLY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigate);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        TelephonyManager tm = (TelephonyManager) getSystemService((Context.TELEPHONY_SERVICE));
        String name = tm.getDeviceId();

        View naviheader= LayoutInflater.from(this).inflate(R.layout.nav_header_navigate,navigationView,false);

        navigationView.addHeaderView(naviheader);


        TextView deviceIMEI= (TextView) naviheader.findViewById(R.id.deviceId);
        deviceIMEI.setText(name);


        mSensorManager = (SensorManager) super.getSystemService(Context.SENSOR_SERVICE);
        orientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        accelerate = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        pressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        if (orientation == null || accelerate == null || magnetic == null || pressure == null) {
            Toast.makeText(NavigateActivity.this, "error_sensor_not_supported", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.i("startServerice","start");
        new MySharedpreference().saveTimeInfo(this,"5");
        AlarmUtil.cancelTimerWIFI(NavigateActivity.this);
        AlarmUtil.cancelTimerStep(NavigateActivity.this);

        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        AlarmUtil.invokeTimerWIFIService(NavigateActivity.this);
        AlarmUtil.invokeTimerStepService(NavigateActivity.this);
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
        getMenuInflater().inflate(R.menu.navigate, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_start) {

            new MySharedpreference().saveTimeInfo(this,"5");
            AlarmUtil.cancelTimerWIFI(NavigateActivity.this);
            AlarmUtil.cancelTimerStep(NavigateActivity.this);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

           // this.startIMU();
            AlarmUtil.invokeTimerWIFIService(NavigateActivity.this);
            AlarmUtil.invokeTimerStepService(NavigateActivity.this);
            Snackbar.make(findViewById(android.R.id.content), "You have started backend service", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
        } else if (id == R.id.nav_status) {

        } else if (id == R.id.nav_exit) {
           // Intent intentStop = new Intent(this,MyIntentService.class);
            //stopService(intentStop);
            //AlarmUtil.cancelTimerWIFI(this);
            //AlarmUtil.cancelTimerStep(this);
            this.finish();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
