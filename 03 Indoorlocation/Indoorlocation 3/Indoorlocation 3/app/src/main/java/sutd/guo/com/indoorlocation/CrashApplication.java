package sutd.guo.com.indoorlocation;

import android.app.Application;

import sutd.guo.com.indoorlocation.util.CrashHandler;

/**
 * Created by Pradeep on 5/8/2016.
 */
public class CrashApplication extends Application {
    private static CrashApplication crashInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        crashInstance = this;
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(this);
    }

    public static CrashApplication getInstance() {
        return crashInstance;
    }
}
