package sutd.guo.com.indoorlocation;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;

import java.net.MalformedURLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import sutd.guo.com.indoorlocation.util.GpsScan;
import sutd.guo.com.indoorlocation.util.HttpUpload;
import sutd.guo.com.indoorlocation.util.MySharedpreference;
import sutd.guo.com.indoorlocation.util.WifiScan;
import sutd.guo.com.indoorlocation.util.wifiRunnable;

public class BackendService extends Service {
    private static final String BROADCAST_WIFI = "sutd.guo.com.indoorlocation.WifiUploadOK";
    String tem_wifi = new String();
    private WifiManager wm;
    String name;
    String time;
    private TelephonyManager tm;
    private Handler mHandler;
    private GpsScan gpsScan;
    Location location;
    String ip;
    private Handler uploadHandler = new Handler();
    private volatile PowerManager.WakeLock wakeLock;
    private NotificationCompat.Builder localBuilder ;
    private MyBroadcastReceiver myReceiver;
    private NotificationManager myNotifiManager;
    @Override
    public void onCreate() {
        super.onCreate();

        myNotifiManager=(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        localBuilder= new NotificationCompat.Builder(this);
        localBuilder.setAutoCancel(false);
        localBuilder.setSmallIcon(R.mipmap.ic_launcher);
        localBuilder.setTicker("Foreground Service Start");
        localBuilder.setContentTitle("Indoor Location Service");
        localBuilder.setContentText("no upload");
        startForeground(1, localBuilder.getNotification());
        myNotifiManager.notify(1,localBuilder.build());
        myReceiver = new MyBroadcastReceiver();
        IntentFilter itFilter = new IntentFilter();
        itFilter.addAction(BROADCAST_WIFI);
        registerReceiver(myReceiver, itFilter);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    class MyBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(BROADCAST_WIFI)){
                String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
                localBuilder.setContentText("upload at "+currentDateTimeString);
                myNotifiManager.notify(1,localBuilder.build());
            }

        }
    }

}
