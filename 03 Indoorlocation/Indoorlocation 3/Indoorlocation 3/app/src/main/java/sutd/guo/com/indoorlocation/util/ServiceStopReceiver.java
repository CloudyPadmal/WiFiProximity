package sutd.guo.com.indoorlocation.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.INotificationSideChannel;
import android.util.Log;

import sutd.guo.com.indoorlocation.MyIntentService;
import sutd.guo.com.indoorlocation.main.MonitorService;

/**
 * Created by jiao on 6/17/16.
 */
public class ServiceStopReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("StopReceiver","receive a broadcast for stop service");
        Intent intentStop = new Intent(context,MyIntentService.class);
        context.stopService(intentStop);
        Intent intentStopStep=new Intent(context, MonitorService.class);
        context.stopService(intentStopStep);
        AlarmUtil.cancelTimerWIFI(context);
        AlarmUtil.cancelTimerStep(context);
    }
}
