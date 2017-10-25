package sutd.guo.com.indoorlocation.main;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Pradeep on 3/3/2017.
 */

public class LogFileFlusher {
    final String TAG = "LogFileFlusher";

    ArrayList<BufferedWriter> bufferedWriterList = new ArrayList<BufferedWriter>();
    private int interval = 5000;
    private int index = 0;
    public void start(){
        Log.i(TAG,"start");
        Random random = new Random();
        interval = 5000/bufferedWriterList.size() + (random.nextInt()%10)*20;
        timer.schedule(timerTask, interval, interval);
    }

    public void stop(){
        Log.i(TAG,"stop");
        flushAll();
        timer.cancel();
    }

    public void flushAll(){
        for (BufferedWriter bufferedWriter : bufferedWriterList) {
            try {
                if(bufferedWriter != null)
                    bufferedWriter.flush();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void add(BufferedWriter bufferedWriter){
        bufferedWriterList.add(bufferedWriter);
    }

    public void remove(BufferedWriter bufferedWriter){
        if(bufferedWriterList.contains(bufferedWriter)){
            bufferedWriterList.remove(bufferedWriter);
        }
    }
    //timer
    private final Timer timer = new Timer();
    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            try {
                bufferedWriterList.get(index).flush();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            index++;
            index %= bufferedWriterList.size();
        }
    };
}
