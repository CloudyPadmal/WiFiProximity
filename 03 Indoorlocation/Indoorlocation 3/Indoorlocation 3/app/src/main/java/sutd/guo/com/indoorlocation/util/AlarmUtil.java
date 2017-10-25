package sutd.guo.com.indoorlocation.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.provider.SyncStateContract;
import android.util.Log;

import java.util.List;

public class AlarmUtil {
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

   /* public static boolean isServiceRunning(Context context, String className) {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceInfos = activityManager.getRunningServices(SyncStateContract.Constants.RUNNING_SERVICE_COUNT);

        if (null == serviceInfos || serviceInfos.size() < 1) {
            return false;
        }
        for (int i = 0; i < serviceInfos.size(); i++) {
            if (serviceInfos.get(i).service.getClassName().contains(className)) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }*/

    public static void invokeTimerWIFIService(Context context) {
        PendingIntent alarmSender = null;
        //Intent startIntent = new Intent(context, ActivityService.class);
        AlarmManager am = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
        Intent startIntent = new Intent(context, AlarmReceiver.class);
        startIntent.setAction("sutd.guo.com.indoorlocation.alarmwifi");
        alarmSender = PendingIntent.getBroadcast(context, 0, startIntent, 0);
        int time = Integer.parseInt(new MySharedpreference().getTimeInfo(context));
        long nextTime=System.currentTimeMillis()+time;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            AlarmManager.AlarmClockInfo alarmClockInfo = null;
            alarmClockInfo = new AlarmManager.AlarmClockInfo(nextTime, alarmSender);
            am.setAlarmClock(alarmClockInfo, alarmSender);
        }else if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            am.setExact(AlarmManager.RTC_WAKEUP, nextTime, alarmSender);
        }else{
            am.set(AlarmManager.RTC_WAKEUP, nextTime, alarmSender);
        }
        Log.i("alarm", "alarm set and the time interval is "+time);
    }

    public static void invokeTimerStepService(Context context) {
        PendingIntent alarmSender = null;
        //Intent startIntent = new Intent(context, ActivityService.class);
        AlarmManager am = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
        Intent startIntent = new Intent(context, AlarmReceiver.class);
        startIntent.setAction("sutd.guo.com.indoorlocation.alarmstep");
        alarmSender = PendingIntent.getBroadcast(context, 0, startIntent, 0);
        int time = Integer.parseInt(new MySharedpreference().getTimeInfo(context));
        long nextTime=System.currentTimeMillis()+time;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            AlarmManager.AlarmClockInfo alarmClockInfo = null;
            alarmClockInfo = new AlarmManager.AlarmClockInfo(nextTime, alarmSender);
            am.setAlarmClock(alarmClockInfo, alarmSender);
        }else if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            am.setExact(AlarmManager.RTC_WAKEUP, nextTime, alarmSender);
        }else{
            am.set(AlarmManager.RTC_WAKEUP, nextTime, alarmSender);
        }
        Log.i("alarm", "alarm set and the time interval is "+time);
    }

    public static void cancelTimerWIFI(Context context){

        PendingIntent alarmSender = null;
        //Intent startIntent = new Intent(context, ActivityService.class);
        AlarmManager am = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
        Intent startIntent = new Intent(context, AlarmReceiver.class);
        startIntent.setAction("sutd.guo.com.indoorlocation.alarmwifi");
        alarmSender = PendingIntent.getBroadcast(context, 0, startIntent, 0);
        am.cancel(alarmSender);
    }

    public static void cancelTimerStep(Context context){

        PendingIntent alarmSender = null;
        //Intent startIntent = new Intent(context, ActivityService.class);
        AlarmManager am = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
        Intent startIntent = new Intent(context, AlarmReceiver.class);
        startIntent.setAction("sutd.guo.com.indoorlocation.alarmstep");
        alarmSender = PendingIntent.getBroadcast(context, 0, startIntent, 0);
        am.cancel(alarmSender);
    }


}
