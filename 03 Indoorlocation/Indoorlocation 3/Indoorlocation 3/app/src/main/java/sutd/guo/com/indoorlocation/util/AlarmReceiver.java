package sutd.guo.com.indoorlocation.util;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import sutd.guo.com.indoorlocation.MyIntentService;
import sutd.guo.com.indoorlocation.main.MonitorService;


/**
 * Created by guowei on 12/11/15.
 */
public class AlarmReceiver extends WakefulBroadcastReceiver {

    private static final String TAG = "AlarmReceiver";

    private static final String ACTION_WIFI = "WIFI_ONLY";
    private static final String ACTION_GPS = "GPS_SCAN";
    private static final String ACTION_IMU="IMU_SCAN";
    private static final String ACTION_STEP="STEP_HEAD";


    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent.getAction().equals("sutd.guo.com.indoorlocation.alarmwifi")){
            Log.i("Alarm receive time", String.valueOf(System.currentTimeMillis()));
            AlarmUtil.invokeTimerWIFIService(context);

            Intent wifiIntent = new Intent(context, MyIntentService.class);
            wifiIntent.setAction(ACTION_WIFI);
            //context.startService(wifiIntent);
            startWakefulService(context, wifiIntent);

            Intent imuIntent = new Intent(context,MyIntentService.class);
            imuIntent.setAction(ACTION_IMU);
            startWakefulService(context,imuIntent);


            Intent stepIntent = new Intent(context,MyIntentService.class);
            stepIntent.setAction(ACTION_STEP);
            startWakefulService(context,stepIntent);


            //gps is for school test, TODO delete if not used
            Intent gpsIntent = new Intent(context, MyIntentService.class);
            gpsIntent.setAction(ACTION_GPS);
            startWakefulService(context, gpsIntent);
        }

        if (intent.getAction().equals("sutd.guo.com.indoorlocation.alarmstep")){
            AlarmUtil.invokeTimerStepService(context);
            Intent stepIntentCount = new Intent(context, MonitorService.class);
            //stepIntentCount.setAction(ACTION_STEP);
            startWakefulService(context,stepIntentCount);
        }
    }
}
