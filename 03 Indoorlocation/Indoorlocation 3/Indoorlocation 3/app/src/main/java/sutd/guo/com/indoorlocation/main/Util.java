package sutd.guo.com.indoorlocation.main;

import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Pradeep on 3/3/2017.
 */

public class Util extends Object {
    public static String logFileDirectory = "/mnt/sdcard/SociallocFile";
    public static ArrayList<String> tagOfCurrentLogFileString = new ArrayList<String>();

    public static void createLogFileFolder(){
        File dirFile = new File(logFileDirectory);

        if(!dirFile.exists()){
            dirFile.mkdir();
        }
    }

    public static void addLogFileExceptTag(String tag){
        tagOfCurrentLogFileString.add(tag);
    }

    public static String getBluetoothAddress(){
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        return adapter.getAddress();
    }

    public static String getImei(Context context){
        String imei = new String();
        TelephonyManager telephonemanager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        try{
            imei = telephonemanager.getDeviceId();
        }
        catch(Exception e){
            e.printStackTrace();
            Toast.makeText(context, "IMEI code read error", Toast.LENGTH_SHORT).show();
        }
        return imei;
    }

    public static boolean isServiceRunning(Context context, String classString){
        ActivityManager myManager=(ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService =
                (ArrayList<ActivityManager.RunningServiceInfo>) myManager.getRunningServices(100);
        for(int i = 0 ; i<runningService.size();i++){
            if(runningService.get(i).service.getClassName().toString().equals
                    (classString)){
                return true;
            }
        }
        return false;
    }

    public static void removeFiles(String pathString){
        removeFiles(pathString, tagOfCurrentLogFileString);
    }

    public static void removeFiles(String pathString, ArrayList<String> exceptTagList){
        File cacheDir = new File(logFileDirectory);

        File[]	fileList;
        fileList = cacheDir.listFiles();
        for(int i = 0;i < fileList.length; i++)   {
            boolean isContainOne = false;
            for(int j=0;j<exceptTagList.size();j++){
                if(fileList[i].getName().contains(exceptTagList.get(j))){
                    isContainOne = true;
                }
            }
            if(!isContainOne){
                fileList[i].delete();
            }
        }
    }

}
