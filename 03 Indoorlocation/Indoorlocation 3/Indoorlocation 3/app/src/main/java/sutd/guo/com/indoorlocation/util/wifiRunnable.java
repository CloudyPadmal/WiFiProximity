package sutd.guo.com.indoorlocation.util;

import android.content.Context;
import android.net.wifi.WifiManager;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cqq on 3/3/16.
 */
public class wifiRunnable implements Runnable {

    private WifiManager wm;
    private Context context;

    public wifiRunnable(WifiManager w,Context c){
        this.wm=w;
        this.context=c;
    }
    String tem_wifi = new String();

    @Override
    public void run() {
        tem_wifi = new WifiScan().writedown(wm, context);

        String name=new MySharedpreference().getNameInfo(context);
        String ip=new MySharedpreference().getIpInfo(context);
        if (tem_wifi.length() != 0) {

            try {
                String time=new HttpUpload().HttpWifi(name,tem_wifi,ip);
                FileOperater fileOperater = FileOperater.getInstance(context);
                List<String> macList = new ArrayList<String>();
                String[] records = tem_wifi.split(" ");
                for (int i=0; i<records.length; i++) {
                    macList.add(records[i]);
                    i++;
                }
                if (!fileOperater.isMacAddress(macList)) {
                    new MySharedpreference().saveTimeInfo(context,"25");
                } else {
                    new MySharedpreference().saveTimeInfo(context,time);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }
}
