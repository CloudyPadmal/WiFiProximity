package sutd.guo.com.indoorlocation;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.animation.AccelerateInterpolator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import sutd.guo.com.indoorlocation.indoorloacation.Data;
import sutd.guo.com.indoorlocation.indoorloacation.GetSensorData;
import sutd.guo.com.indoorlocation.main.MonitorService;
import sutd.guo.com.indoorlocation.types.Steps;
import sutd.guo.com.indoorlocation.util.AlarmReceiver;
import sutd.guo.com.indoorlocation.util.AlarmUtil;
import sutd.guo.com.indoorlocation.util.FileOperater;
import sutd.guo.com.indoorlocation.util.HttpUpload;
import sutd.guo.com.indoorlocation.util.MySharedpreference;
import sutd.guo.com.indoorlocation.util.WifiScan;
import sutd.guo.com.indoorlocation.util.GpsScan;
/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 *
 * helper methods.
 */
public class MyIntentService extends IntentService {

    private SensorManager mSensorManager;
    private static final String ACTION_IMU="IMU_SCAN";
    private static final String ACTION_STEP="STEP_HEAD";
    private static final String BROADCAST_IMU = "sutd.guo.com.indoorlocation.imuUploadOK";
    private static final String BROADCAST_STEP= "sutd.guo.com.indoorlocation.stepUploadOK";
    GetSensorData mysen = new GetSensorData();


    private static final String BROADCAST_WIFI = "sutd.guo.com.indoorlocation.WifiUploadOK";
    private static final String ACTION_WIFI = "WIFI_ONLY";
    private static final String ACTION_GPS = "GPS_SCAN";
    private static final long SCAN_PERIOD = 1500;
    private Context context;
    private String tem_wifi = "";
    private WifiManager wm;
    private String name;
    private Location location = null;
    private LocationManager locationManager = null;
    private Thread.UncaughtExceptionHandler UEhandler=new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            Log.e("myintentservice", "uncaughtException: ", ex);
            try {
                createLogOnDevice(true,"indoorlocationCrashLog.txt",ex.toString());
                AlarmUtil.cancelTimerWIFI(MyIntentService.this);
                PendingIntent alarmSender = null;
                AlarmManager am = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
                Intent startIntent = new Intent(context, AlarmReceiver.class);
                startIntent.setAction("sutd.guo.com.indoorlocation.alarmwifi");
                alarmSender = PendingIntent.getBroadcast(context, 0, startIntent, 0);
                long nextTime=System.currentTimeMillis()+10*1000;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    AlarmManager.AlarmClockInfo alarmClockInfo = null;
                    alarmClockInfo = new AlarmManager.AlarmClockInfo(nextTime, alarmSender);
                    am.setAlarmClock(alarmClockInfo, alarmSender);
                }else if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
                    am.setExact(AlarmManager.RTC_WAKEUP, nextTime, alarmSender);
                }else{
                    am.set(AlarmManager.RTC_WAKEUP, nextTime, alarmSender);
                }
                stopSelf();
                System.exit(2);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    public MyIntentService() {
        super("MyIntentService");
    }

    @Override
    public void onCreate() {

        // TODO delete if gps is not used
        locationManager = (LocationManager) MyIntentService.this
                .getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                500, 8, locationListener);
        Log.i("myintentservice", "onCreate()");
        Thread.setDefaultUncaughtExceptionHandler(UEhandler);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        locationManager.removeUpdates(locationListener);
        //this.stopCompass();
        Log.i("myintentservice", "onDestory()");
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        context=this;

        //set the alarm for next task,  set here or set in AlarmReceiver.java
        //AlarmUtil.invokeTimerWIFIService(context);

        if(new MySharedpreference().getNameInfo(context).equals("")){
            TelephonyManager tm = (TelephonyManager) getSystemService((Context.TELEPHONY_SERVICE));
            name = tm.getDeviceId();
            new MySharedpreference().saveNameInfo(context,name);
        }
        //do the task
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_IMU.equals(action)){
                mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
                handleActionIMU();
            }
            if (ACTION_WIFI.equals(action)) {
                wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                if (!wm.isWifiEnabled()) {
                    wm.setWifiEnabled(true);
                }
                handleActionWIFI(); //handlewifi upload

                Log.i("myintentservice", "intentservie wifi");
            // gps for school test
            }else if (ACTION_GPS.equals(action)) {

                handleActionGPS();

                Log.i("myintentservice", "intentservie gps");
            }
            //release wakelock
            AlarmReceiver.completeWakefulIntent(intent);
        }
    }

    /**
     * Handle action WIFI_ONLY
     */
    private void handleActionWIFI() {
        Thread wifiThread=new Thread( new Wifi());
        wifiThread.start();

    }

    /*
    *   Handle action IMU
    */
    private void handleActionIMU(){
        mysen.getsensorData(mSensorManager);
        Thread imuThread = new Thread(new IMU());
        imuThread.start();

    }


    /**
     * Handle action GPS
     */
    private void handleActionGPS() {
        location=null;

        String name = new MySharedpreference().getNameInfo(context);
        String ip = new MySharedpreference().getIpInfo(context);
        try {
            Thread.sleep(SCAN_PERIOD);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        location = locationManager.getLastKnownLocation(getProvider());
        Myupload myupload= new Myupload();
        myupload.setIp(ip);
        myupload.setName(name);
        myupload.setLocation(location);
        Thread thread=new Thread(myupload);
        thread.start();
    }

    private String getProvider() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(true);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(false);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        return locationManager.getBestProvider(criteria, true);
    }

    //the wifi thread
    class Wifi implements Runnable {

        @Override
        public void run() {

            tem_wifi = new WifiScan().writedown(wm, context);
            String name = new MySharedpreference().getNameInfo(context);
            String ip = new MySharedpreference().getIpInfo(context);
            if (tem_wifi.length() != 0) {

                try {
                    String newTime="3";
                    //if the time is valid
                    if(!newTime.equals("")){
                            newTime=new HttpUpload().HttpWifi(name, tem_wifi, ip);
                            if (newTime == "" || newTime == null || newTime.trim().length()==0) {
                                newTime = "3";
                            } else {
                                //update the notification TODO delete if do not want see notification
                                Intent intent=new Intent();
                                intent.setAction(BROADCAST_WIFI);
                                sendBroadcast(intent);
                            }
                            new MySharedpreference().saveTimeInfo(context,newTime);
//                        }
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    // the IMU thread

    private class IMU implements Runnable {

        @Override
        public void run() {

            String name = new MySharedpreference().getNameInfo(context);
            String ip = new MySharedpreference().getIpInfo(context);
            Data data = Data.getInstance();

            String orientation_data = String.valueOf(data.getOrientation_x())+ " " +String.valueOf(data.getOrientation_y())+" "+
                    String.valueOf(data.getOrientation_z());

            String accelerate_data = String.valueOf(data.getAccelerate_x())+ " "+String.valueOf(data.getAccelerate_y())+" "+
                    String.valueOf(data.getAccelerate_z());

            String magnetic_data = String.valueOf(data.getMagnetic_x())+" "+String.valueOf(data.getMagnetic_y())+" "+
                    String.valueOf(data.getMagnetic_z());

            String barometer_data= String.valueOf(data.getBarometer_text());


           if(data.getOrientation_ready() & data.getAccelerate_ready() &
                    data.getMagnetic_ready() & data.getBarometer_ready()){
                try {

                    String newTime=new HttpUpload().HttpIMU(name,orientation_data,accelerate_data,magnetic_data,barometer_data,ip);
                    if(!newTime.equals("")){
                        new MySharedpreference().saveTimeInfo(context,newTime);

                        Intent intent=new Intent();
                        intent.setAction(BROADCAST_IMU);
                        sendBroadcast(intent);
                    }
                }catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }

        }

    }


    public class Myupload implements Runnable{

        Location location;
        String name;
        String ip;
        public void setLocation(Location location){
            this.location=location;

        }

        public void setName(String name){
            this.name=name;
        }

        public void setIp(String ip){
            this.ip=ip;
        }
        @Override
        public void run() {

            if (location != null) {

            } else {

            }

        }
    }

    private LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location l) {
            if (l != null) {
                location = l;
            }
        }

        public void onProviderDisabled(String provider) {
            location = null;
        }

        public void onProviderEnabled(String provider) {

            Location l = locationManager.getLastKnownLocation(provider);
            if (l != null) {
                location = l;
            }
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

    };

    public static BufferedWriter out;
    private void createLogOnDevice(Boolean append,String filename,String message) throws IOException {
                /*
                 * Function to initially create the log file and it also writes the time of creation to file.
                 */
        File Root = Environment.getExternalStorageDirectory();
        if(Root.canWrite()){
            File LogFile = new File(Root, filename);
            FileWriter LogWriter = new FileWriter(LogFile, append);
            out = new BufferedWriter(LogWriter);
            Date date = new Date();
            out.write("Logged at" + String.valueOf(date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds() + "\n"));
            out.write(message);
            out.close();

        }
    }


}

