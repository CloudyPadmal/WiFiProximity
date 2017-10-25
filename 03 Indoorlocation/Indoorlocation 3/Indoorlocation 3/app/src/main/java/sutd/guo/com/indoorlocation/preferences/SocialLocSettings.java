package sutd.guo.com.indoorlocation.preferences;

import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by Pradeep on 3/3/2017.
 */

public class SocialLocSettings {

    SharedPreferences mSettings;

    public SocialLocSettings(SharedPreferences settings) {
        mSettings = settings;
    }

    public int getMinStepsSensitivity() {
        Log.d("Settings", "min_steps_sensitivity " + mSettings.contains("min_steps_sensitivity"));
        Log.d("Settings", "min_steps_sensitivity " + Integer.valueOf(mSettings.getInt("min_steps_sensitivity",1)));
        return Integer.valueOf(mSettings.getInt("min_steps_sensitivity", 50));
    }

    public int getMaxStepsSensitivity() {
        //Log.d("Settings", "max_steps_sensitivity " + mSettings.contains("max_steps_sensitivity"));
        //Log.d("Settings", "max_steps_sensitivity " + Integer.valueOf(mSettings.getInt("max_steps_sensitivity", 1)));
        return Integer.valueOf(mSettings.getInt("max_steps_sensitivity", 50));
    }

    public boolean wakeAggressively() {
        return mSettings.getString("operation_level", "run_in_background").equals("wake_up");
    }
    public boolean keepScreenOn() {
        return mSettings.getString("operation_level", "run_in_background").equals("keep_screen_on");
    }
}
