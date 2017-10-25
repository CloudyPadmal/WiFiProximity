package sutd.guo.com.indoorlocation.util;

import android.content.Context;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class WifiScan {
    private static List<ScanResult> results;
    public synchronized String writedown(WifiManager wm, Context context) {
        StringBuffer wifi = new StringBuffer();
        if (wm.startScan()) {
            results = wm.getScanResults();
            String num = String.valueOf(results.size());
            wifi.append(num+" ");
            if (results.size() != 0) {
                for (ScanResult result : results) {
                    wifi.append(result.BSSID+" "+result.level+" ");
                    Log.i("frequency",String.valueOf(result.frequency));
                }
            }
            results.clear();
        }
        return wifi.toString();
    }
}
