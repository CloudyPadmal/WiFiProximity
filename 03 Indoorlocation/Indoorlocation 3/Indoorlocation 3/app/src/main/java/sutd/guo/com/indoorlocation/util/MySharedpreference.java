package sutd.guo.com.indoorlocation.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class MySharedpreference {
    public static int count = 0;

    public final String FILENAME = "ip.txt";
    public final String DEFAULT_IP = "http://202.94.70.33/sportHub/";

    public static SharedPreferences getSharePre(Context a) {
        return a.getSharedPreferences("userColor", Context.MODE_PRIVATE);
    }

    public void saveNameInfo(Context a, String name) {
        SharedPreferences pre = getSharePre(a);
        SharedPreferences.Editor edit = pre.edit();
        edit.putString("name", name);
        edit.commit();
    }

    public String getNameInfo(Context a) {
        SharedPreferences pre = getSharePre(a);
        String color = pre.getString("name", "");
        return color;
    }


    public void saveTimeInfo(Context a, String time) {
        SharedPreferences pre = getSharePre(a);
        SharedPreferences.Editor edit = pre.edit();

        float lowerbound=0.999f;
        float t=Float.parseFloat(time);

        if(!(t<lowerbound)){
            int i= (int) (t*1000);
            String time_change = Integer.toString(i);
            edit.putString("time", time_change);
            edit.commit();
        }else{
            edit.putString("time", "3000");
            edit.commit();
        }
    }

    public String getTimeInfo(Context a) {
        SharedPreferences pre = getSharePre(a);
        String time = pre.getString("time", "3000");
        return time;
    }

    public void saveIpInfo(Context a, String time) {
//        SharedPreferences pre = getSharePre(a);
//        SharedPreferences.Editor edit = pre.edit();
//        edit.putString("ip", time);
//        edit.commit();
        saveIpInfoToFile(a,time);
    }

    public String getIpInfo(Context a) {
        SharedPreferences pre = getSharePre(a);
        String time = DEFAULT_IP;
        if (count == 0) {
            Log.i("ip.txt", time = getIpInfoFromFile(a));
            SharedPreferences.Editor edit = pre.edit();
            edit.putString("ip", time);
            edit.commit();
            count ++;
        } else {
            time = pre.getString("ip",DEFAULT_IP);
        }
        return time;
    }

    private String getIpInfoFromFile(Context a) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String ip = null;
            //File ipFile = new File(a.getFilesDir(), FILENAME);
            File path = new File("/sdcard/sutd/");
            File ipFile = new File(path,FILENAME);
            if (ipFile.exists()) {
            } else {
                saveIpInfoToFile(a,DEFAULT_IP);
            }
            if (!ipFile.isFile()) {
                Log.e("Read IP File", "IP file is not a file!");
                return null;
            }

            FileInputStream in = null;
            BufferedReader reader = null;
            StringBuffer ipBuilder = new StringBuffer();
            try {
                in = new FileInputStream(ipFile);
                reader = new BufferedReader(new InputStreamReader(in));
                String line = "";
                while ((line = reader.readLine()) != null) {
                    ipBuilder.append(line);
                }
            } catch (Exception e) {
                Log.e("Read IP File", "Read IP file failed!");
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e("Read IP File", "Close IP file failed when read!");
                        e.printStackTrace();
                    }
                }
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            ip = ipBuilder.toString();
            return ip;
        } else {
            return null;
        }
    }

    private void saveIpInfoToFile(Context a,String ip) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File path = new File("/sdcard/sutd/");
            File ipFile = new File(path,FILENAME);
            BufferedWriter writer = null;
            OutputStreamWriter out = null;

            if (ip == null) {
                ip = DEFAULT_IP;
            }
        if (ipFile.exists()) {

        } else {
            try {
                if (!path.exists()) {
                    path.mkdirs();
                }
                ipFile.createNewFile();
                Log.i("File",ipFile.canWrite()?  "can":"cannot");
            } catch (IOException e) {
                Log.e("Write IP File","Create IP file failed");
                e.printStackTrace();
            }
        }
            try {
                Log.i("Wirte File", ipFile.getAbsolutePath());
                out = new OutputStreamWriter(new FileOutputStream(ipFile));
                writer = new BufferedWriter(out);
                writer.write(ip);
            } catch (Exception e) {
                Log.e("Write IP File", "Write IP file failed!");
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        Log.e("Write IP File", "Close IP file failed when write!");
                        e.printStackTrace();
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
