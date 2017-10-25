package sutd.guo.com.indoorlocation.util;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.InputStream;
import java.text.DateFormat;
import java.util.Date;

import sutd.guo.com.indoorlocation.R;

/**
 * Created by cqq on 3/10/16.
 */
public class MyNotificationReceiver extends BroadcastReceiver{
    private static final String BROADCAST_WIFI = "sutd.guo.com.indoorlocation.WifiUploadOK";
    private static final int NOTIFICATION_ID=121;
    private NotificationCompat.Builder locationBuilder;


    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(BROADCAST_WIFI)){

            showNotification(context);

        }

    }

    private void showNotification(Context ctx){
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
            locationBuilder = new NotificationCompat.Builder(ctx)
                    .setAutoCancel(false)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Indoor Location Service")
                    .setContentText("upload at " + currentDateTimeString)
                    .setOnlyAlertOnce(true);
            NotificationManager myNotifiManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            myNotifiManager.notify(NOTIFICATION_ID, locationBuilder.build());
       // }
    }
}
