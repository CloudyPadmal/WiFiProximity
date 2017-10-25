package sutd.guo.com.indoorlocation.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.util.Log;

import java.util.Date;

/**
 * Created by jiao on 6/17/16.
 */
public class ServiceStartReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("receive broadcast time", String.valueOf(new Date(System.currentTimeMillis())));
        Log.i("StartReceiver", "receive a broadcast for start service");
        AlarmUtil.cancelTimerWIFI(context);
        AlarmUtil.cancelTimerStep(context);
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        AlarmUtil.invokeTimerWIFIService(context);
        AlarmUtil.invokeTimerStepService(context);
    }
}
