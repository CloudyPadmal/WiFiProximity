package sutd.guo.com.indoorlocation.util;

/**
 * Created by Pradeep on 5/8/2016.
 */
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Pradeep on 4/8/2016.
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "CrashHandler";
    private static final boolean DEBUG = true;

    private static final String PATH = Environment.getExternalStorageDirectory().getPath() + "/sdcard/CrashSUTD/log/";
    private static final String FILE_NAME = "crash";
    private static final String FILE_NAME_SUFFIX = ".trace";

    private static CrashHandler crashHandler = new CrashHandler();
    private Thread.UncaughtExceptionHandler mDefaultCrasherHandler;
    private Context context;

    private CrashHandler() {

    }

    public static CrashHandler getInstance() {
        return crashHandler;
    }

    public void init(Context context) {
        mDefaultCrasherHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
        context = context.getApplicationContext();
    }

    private void dumpExceptionToSDCard(Throwable ex) throws IOException {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            if (DEBUG) {
                Log.w(TAG, "sdcard unmounted, skip dump exception");
                return;
            }
        }

        File dir = new File(PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        long current = System.currentTimeMillis();
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(current));
        File file = new File(PATH + FILE_NAME + time + FILE_NAME_SUFFIX);

        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
            pw.println(time);
            dumpPhoneInfo(pw);
            pw.println();
            Log.i(TAG, "Output exception ti file");
            ex.printStackTrace(pw);

            pw.close();
        } catch (Exception e) {
            Log.e(TAG, "dump crash info failed");
            e.printStackTrace();
        }
    }

    private void dumpPhoneInfo(PrintWriter pw) throws PackageManager.NameNotFoundException {
//        PackageManager pm = context.getPackageManager();
//        PackageInfo pi = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
//        pw.print("App Version: ");
//        pw.print(pi.versionName);
//        pw.print("_");
//        pw.println(pi.versionCode);
//
//        pw.print("OS Version: ");
//        pw.print(Build.VERSION.RELEASE);
//        pw.print("_");
//        pw.println(Build.VERSION.SDK_INT);
//
//        pw.print("Vendor: ");
//        pw.println(Build.MANUFACTURER);
//
//        pw.print("Model: ");
//        pw.println(Build.MODEL);
//
//        pw.print("CPU ABI: ");
//        pw.println(Build.CPU_ABI);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        try {
            Log.i(TAG,"uncaughtException function");
            dumpExceptionToSDCard(ex);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i(TAG,"Before output exception!");
        ex.printStackTrace();
        Log.i(TAG,"After output exception!");
        if (mDefaultCrasherHandler != null) {
            Log.i(TAG,"Restart Alarm!");
            new MySharedpreference().saveTimeInfo(context,"5");
            AlarmUtil.cancelTimerWIFI(context);
            /*Intent intents=new Intent(this, BackendService.class);
            this.startService(intents);*/

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            AlarmUtil.invokeTimerWIFIService(context);
            mDefaultCrasherHandler.uncaughtException(thread, ex);
        } else {
            //Process.killProcess(Process.myPid());
            Log.i(TAG,"Restart Alarm!");
            new MySharedpreference().saveTimeInfo(context,"5");
            AlarmUtil.cancelTimerWIFI(context);
            /*Intent intents=new Intent(this, BackendService.class);
            this.startService(intents);*/

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            AlarmUtil.invokeTimerWIFIService(context);
        }

    }
}

