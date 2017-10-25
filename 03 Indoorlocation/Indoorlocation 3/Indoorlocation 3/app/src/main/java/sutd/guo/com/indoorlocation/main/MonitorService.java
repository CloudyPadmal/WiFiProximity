/*
 *  MainActivity - Android App
 *  Copyright (C) 2009 Levente Bagi
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package sutd.guo.com.indoorlocation.main;


import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.animation.AccelerateInterpolator;

import java.net.MalformedURLException;
import java.util.ArrayList;

import sutd.guo.com.indoorlocation.motion.MotionDetector;
import sutd.guo.com.indoorlocation.motion.StepNotifier;
import sutd.guo.com.indoorlocation.preferences.SocialLocSettings;
import sutd.guo.com.indoorlocation.types.Steps;
import sutd.guo.com.indoorlocation.util.HttpUpload;
import sutd.guo.com.indoorlocation.util.MySharedpreference;


/**
 * This is an example of implementing an application service that runs locally
 * in the same process as the application.  The {@link StepServiceController}
 * and {@link StepServiceBinding} classes show how to interact with the
 * service.
 *
 * <p>Notice the use of the {@link NotificationManager} when interesting things
 * happen in the service.  This is generally how background services should
 * interact with the user, rather than doing something more disruptive such as
 * calling startActivity().
 */

public class MonitorService extends Service {
	private static final String TAG = "MonitorService";
	
    private SocialLocSettings mSocialLocSettings;
    private SharedPreferences mState;
    private SharedPreferences.Editor mStateEditor;
    
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private MotionDetector mMotionDetector;
    
    private StepNotifier mStepNotifier;
    
    private PowerManager.WakeLock wakeLock;

    private int mStepsNumber;

    //modify
    private Context context = this;
    private String SaveStep;
    private Steps mSteps;
    private float mHeading;
    private String angle;
    private ArrayList<Long> AngletimeList = new ArrayList();
    private ArrayList<String> AngleList = new ArrayList();
    int max_orientation_history_size=2000;
    private Handler IMUHandler = new Handler();
    private Handler StepHandler =new Handler();
    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class StepBinder extends Binder {
        public MonitorService getService() {
            return MonitorService.this;
        }
    }
    
    @Override
    public void onCreate() {
        Log.i(TAG, "[SERVICE] onCreate");
        super.onCreate();


        // Load settings
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        mSocialLocSettings = new SocialLocSettings(settings);
        mState = getSharedPreferences("state", 0);
        acquireWakeLock();

        // Start detecting
        mMotionDetector = new MotionDetector();
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        //modify
        //mMotionDetector.startDetecting();
        registerDetector();
        

        
        //step
        mStepNotifier = new StepNotifier(mSocialLocSettings);
        mStepNotifier.addListener(mStepListener);
        mMotionDetector.addStepListener(mStepNotifier);
        setSteps(mStepsNumber = mState.getInt("steps", 0));


        // Register our receiver for the ACTION_SCREEN_OFF action. This will make our receiver
        // code be called whenever the phone enters standby mode.
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mReceiver, filter);
        
        // Start voice
         reloadSettings();
         stepsChanged(mSteps);

    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "[SERVICE] onDestroy");

        // Unregister our receiver.
        unregisterReceiver(mReceiver);
        unregisterDetector();
        mMotionDetector.stopDetecting();
        stopCompass();
        
        mStateEditor = mState.edit();
        mStateEditor.putInt("steps", mStepsNumber);


        mStateEditor.commit();

        wakeLock.release();
        
        super.onDestroy();

    }

    private void registerDetector() {
    	int sensorRate = SensorManager.SENSOR_DELAY_GAME;
    	//step
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mSensorManager.registerListener(mMotionDetector, mSensor, sensorRate);
            
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mSensorManager.registerListener(mMotionDetector, mSensor, sensorRate);
        
        //direction
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorManager.registerListener(mMotionDetector, mSensor, sensorRate);
        //turning 修改
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(mMotionDetector,mSensor,sensorRate);

        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mSensorManager.registerListener(mMotionDetector,mSensor,sensorRate);
    }

    private void unregisterDetector() {
        mSensorManager.unregisterListener(mMotionDetector);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "[SERVICE] onBind");
        return mBinder;
    }

    /**
     * Receives messages from activity.
     */
    private final IBinder mBinder = new StepBinder();

    public interface ICallback {
        public void stepsChanged(int value);
        //modify for jy
        public void stepsChanged2(Steps steps);
    }

    //start modify
    private static final int STEPS_MSG = 10001;
    private static final int STEPS_MSG2=10002;
    //show in the screen
    private MonitorService.ICallback mMonitorServiceCallback = new MonitorService.ICallback() {
        public void stepsChanged(int value) {
            mHandler.sendMessage(mHandler.obtainMessage(STEPS_MSG, value, 0));

        }

        //modify
        public void stepsChanged2(Steps steps){
            Message message = mHandler.obtainMessage(STEPS_MSG2,0, 0);
            message.obj = steps;
            mHandler.sendMessage(message);
        }
    };

    private ICallback mCallback = mMonitorServiceCallback;

    private Handler mHandler = new Handler() {


        @Override
        public void handleMessage(Message msg) {

            if (msg.what == STEPS_MSG) {
                int mStepValue = msg.arg1;
                Log.d("jiangye3","test_"+"0_" + mStepValue);
                String steps=String.valueOf(mStepValue);
                SharedPreferences.Editor editor = getSharedPreferences("step_head",MODE_PRIVATE).edit();
                editor.putString("step",steps);
                editor.commit();

            }
            else if (msg.what == STEPS_MSG2){
                mSteps = (Steps) msg.obj;
                //now we get the closest heading
                long current_step_timestamp=0;
                if(mSteps!=null)
                {
                    current_step_timestamp=mSteps.getStepTime(mSteps.timeList.size()-1);
                    mHeading=getNearestOrientation(current_step_timestamp);
                    Log.d("jiangye3","test_" + mHeading);
                    SharedPreferences.Editor editor = getSharedPreferences("step_head",MODE_PRIVATE).edit();
                    editor.putString("heading",String.valueOf(mHeading));
                    editor.commit();
                }

            }
            else super.handleMessage(msg);
        }
    };

    //end modify

    public void registerCallback(ICallback cb) {
        mCallback = cb;
    }
    
    public void unregisterCallback(){
    	mCallback = null;
    }
    
    public void initActivityShow(){
    	mStepListener.passValue();

    }
    
    public void reloadSettings() {
        
        if (mMotionDetector != null) { 
            mMotionDetector.setStepViberationSensitivity(
            		mSocialLocSettings.getMinStepsSensitivity(),
            		mSocialLocSettings.getMaxStepsSensitivity()
            );

        }
        
        if (mStepNotifier != null) mStepNotifier.reloadSettings();

    }


//重置函数
    public void setSteps(int steps) {
        mStepsNumber = steps;
        mStepListener.passValue();
    }


    
    /**
     * Forwards pace values from PaceNotifier to the activity. 
     */
    private StepNotifier.Listener mStepListener = new StepNotifier.Listener() {
        public void stepsChanged(Steps steps) {
            mStepsNumber += steps.timeList.size();

            //for test
            //modify for jy
            mSteps = (Steps)steps.clone();
            passValue();
            //stop activity still can use
             handleStep();
        }
        public void passValue() {
           if (mCallback != null) {
                mCallback.stepsChanged(mStepsNumber);
                mCallback.stepsChanged2(mSteps);
            }
        }
    };

    /**
     * Forwards distance values from DistanceNotifier to the activity. 
     */


    // BroadcastReceiver for handling ACTION_SCREEN_OFF.
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Check action just to be on the safe side.
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                // Unregisters the listener and registers it again.
                MonitorService.this.unregisterDetector();
                MonitorService.this.registerDetector();
                if (mSocialLocSettings.wakeAggressively()) {
                    wakeLock.release();
                    acquireWakeLock();
                }
            }
        }
    };

    private void acquireWakeLock() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        int wakeFlags;
        if (mSocialLocSettings.wakeAggressively()) {
            wakeFlags = PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP;
        }
        else if (mSocialLocSettings.keepScreenOn()) {
            wakeFlags = PowerManager.SCREEN_DIM_WAKE_LOCK;
        }
        else {
            wakeFlags = PowerManager.PARTIAL_WAKE_LOCK;
        }
        wakeLock = pm.newWakeLock(wakeFlags, TAG);
        wakeLock.acquire();
    }



    public void stepsChanged(Steps steps) {
        if(steps == null)
            return;

        mStepsNumber += steps.timeList.size();
        SaveStep=String.valueOf(mStepsNumber);
        SharedPreferences.Editor editor = getSharedPreferences("step_head",MODE_PRIVATE).edit();
        editor.putString("step",SaveStep);
        editor.commit();
        //for test
        //modify for jy
        mSteps = (Steps)steps.clone();
        long current_step_timestamp=0;
        if(mSteps!=null)
        {
            current_step_timestamp=mSteps.getStepTime(mSteps.timeList.size()-1);
            mHeading=getNearestOrientation(current_step_timestamp);
            Log.d("jiangye2","test"+mHeading);
            SharedPreferences.Editor editor1 = getSharedPreferences("step_head",MODE_PRIVATE).edit();
            editor1.putString("heading",String.valueOf(mHeading));
            editor1.commit();

        }

        //handleStep();
    }


    public float getNearestOrientation(long time) {

        float min_time_diff=Float.POSITIVE_INFINITY;
        float heading=0.0f;
        int max_i=-1;
        //get the nearest orientation orientation based on the input time and the histroy_orientation_data
        for(int i=0;i<AngletimeList.size();i++){
            float difference=Math.abs(time-AngletimeList.get(i));
            if(difference<min_time_diff)
            {
                heading=Float.parseFloat(AngleList.get(i));
                min_time_diff=difference;
                max_i=i;
            }

        }

        return heading;

    }

    // angle
    private final float MAX_ROATE_DEGREE = 1.0f;
    private float mDirection;
    private float mTargetDirection;
    private AccelerateInterpolator mInterpolator;
    private Sensor mOrientationSensor;
    private boolean mStopDrawing = false;
    private void startCompass(){
        mDirection = 0.0f;
        mTargetDirection = 0.0f;
        mInterpolator = new AccelerateInterpolator();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mOrientationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        if (mOrientationSensor != null) {
            mSensorManager.registerListener(mOrientationSensorEventListener, mOrientationSensor,
                    SensorManager.SENSOR_DELAY_GAME);
        }
        mStopDrawing = false;

        IMUHandler.postDelayed(mCompassViewUpdater, 20);
    }
    private void stopCompass(){
        mStopDrawing = true;
        if (mOrientationSensor != null) {
            mSensorManager.unregisterListener(mOrientationSensorEventListener);
        }
        IMUHandler.removeCallbacks(mCompassViewUpdater, 20);
    }
    protected Runnable mCompassViewUpdater = new Runnable() {
        @Override
        public void run() {

            if (mDirection != mTargetDirection) {

                // calculate the short routine
                float to = mTargetDirection;
                if (to - mDirection > 180) {
                    to -= 360;
                } else if (to - mDirection < -180) {
                    to += 360;
                }

                // limit the max speed to MAX_ROTATE_DEGREE
                float distance = to - mDirection;
                if (Math.abs(distance) > MAX_ROATE_DEGREE) {
                    distance = distance > 0 ? MAX_ROATE_DEGREE : (-1.0f * MAX_ROATE_DEGREE);
                }

                // need to slow down if the distance is short
                mDirection = normalizeDegree(mDirection
                        + ((to - mDirection) * mInterpolator.getInterpolation(Math
                        .abs(distance) > MAX_ROATE_DEGREE ? 0.4f : 0.3f)));

            }

            updateDirection();

            IMUHandler.postDelayed(mCompassViewUpdater, 20);

        }
    };
    private SensorEventListener mOrientationSensorEventListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            float direction = event.values[0] * -1.0f;
            //Steps.public long getStepTime(int index)

            mTargetDirection = normalizeDegree(direction);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    private float normalizeDegree(float degree) {
        return (degree + 720) % 360;
    }
    private void updateDirection() {

        float direction = normalizeDegree(mTargetDirection * -1.0f);
        // angle = (int)direction;
        angle=String.valueOf(direction);
        long timeSeconds = System.currentTimeMillis();
        AngletimeList.add(timeSeconds);
        AngleList.add(angle);

        //remove the first come if the size of history data is too big, to avoid too much memory consumption
        while(AngleList.size()>max_orientation_history_size)
            AngleList.remove(0);

        while(AngletimeList.size()>max_orientation_history_size)
            AngletimeList.remove(0);

    }

    //upload the data


    private void handleStep(){
        this.startCompass();
       StepHandler.post(StepRunnable);

    }

    private Runnable StepRunnable= new Runnable() {
        @Override
        public void run() {
            Thread stepThread = new Thread(new STEP());
            stepThread.start();
            Log.d("test","1");
            StepHandler.postDelayed(this,3000);
        }
    };


    private class STEP implements Runnable {
        @Override
        public void run() {
            String name = new MySharedpreference().getNameInfo(context);
            String ip = new MySharedpreference().getIpInfo(context);

            SharedPreferences pref = getSharedPreferences("step_head",MODE_PRIVATE);
            String step_data = pref.getString("step","");
            String turning_data = pref.getString("heading","");


            Log.d("STEP:jiangye, step_data", step_data);


            if (turning_data.length()!=0 && step_data.length()!=0 && Integer.parseInt(step_data)!=0 && Float.parseFloat(turning_data)!=0.0){

                    try {
                        String newTime = new HttpUpload().HttpSTEP(name, step_data, turning_data, ip);
                        if (!newTime.equals("")) {
                            new MySharedpreference().saveTimeInfo(context, newTime);
                        }
                    } catch (MalformedURLException e) {
                        e.printStackTrace();

                    }
            }
        }

    }



}

