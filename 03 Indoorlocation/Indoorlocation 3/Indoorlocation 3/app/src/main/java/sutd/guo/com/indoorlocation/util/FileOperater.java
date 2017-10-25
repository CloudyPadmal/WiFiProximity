package sutd.guo.com.indoorlocation.util;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import sutd.guo.com.indoorlocation.R;

/**
 * Created by Pradeep on 18/7/2016.
 */
public class FileOperater {
    private final static String filepath = "position.txt";
    private Set<String> macSet;
    private static FileOperater fileOperater;
    private static Object syncObject = new Object();
    private FileOperater(Context context) {
        macSet = readFile(context);
    }

    public static FileOperater getInstance(Context context) {

        if(fileOperater == null) {
            synchronized (syncObject){
                if(fileOperater == null) {
                    fileOperater = new FileOperater(context);
                }
            }
        }
        return fileOperater;
    }

    public boolean isMacAddress(List<String> macList) {
        boolean result = false;
        Iterator<String> iterator = macList.iterator();
        while (iterator.hasNext()) {
            if(macSet.contains(iterator.next())) {
                result =true;
                break;
            }
        }
        return result;
    }

    private Set<String> readFile(Context context){
        HashSet<String> macSet = new HashSet<String>();
        BufferedReader macFileReader = null;
        try {
            macFileReader = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(R.raw.ap_locations)));
            String line = macFileReader.readLine();
            int i=0;
            while (line!=null) {
                macSet.add(line);
                line = macFileReader.readLine();
                i++;
            }
            macFileReader.close();
        } catch (FileNotFoundException e) {
            Log.i("Position file","file not found!");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (macFileReader != null) {
                try {
                    macFileReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        macSet.add("18:64:72:56:25:11");
        return macSet;
    }
}
