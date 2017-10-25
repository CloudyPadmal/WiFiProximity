package sutd.guo.com.indoorlocation.indoorloacation;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * Created by Pradeep on 3/3/2017.
 */

public class GetSensorData extends Activity implements SensorEventListener {


    private SensorManager mySensorManager;

    private float orientation_x, orientation_y, orientation_z;

    private float accelerate_x, accelerate_y, accelerate_z;

    private float barometer_text;

    private Sensor orientation, accelerate, magnetic, pressure,gravity;
    private float[] gravityValues = null;
    private float[] magneticValues = null;
    private float[] earthaccelerate = null;



    public void getsensorData(SensorManager mSensorManager) {
        mySensorManager = mSensorManager;


        orientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        accelerate = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        gravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        pressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);

        mySensorManager.registerListener(this, orientation, SensorManager.SENSOR_DELAY_UI);
        mySensorManager.registerListener(this, accelerate, SensorManager.SENSOR_DELAY_UI);
        mySensorManager.registerListener(this, magnetic, SensorManager.SENSOR_DELAY_UI);
        mySensorManager.registerListener(this, gravity, SensorManager.SENSOR_DELAY_UI);
        mySensorManager.registerListener(this, pressure, SensorManager.SENSOR_DELAY_UI);


    }

    protected void myOnStop() {
        mySensorManager.unregisterListener(this);
    }

    @Override
    protected void onPause() {
        mySensorManager.unregisterListener(this);
        super.onPause();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Data data = Data.getInstance();
        float[] values = event.values;
        int sensorType = event.sensor.getType();
        boolean orientation_ready=true;
        boolean accelerate_ready =true;
        boolean magnetic_ready   =true;
        boolean barometer_ready  =true;
        switch (sensorType) {
            case Sensor.TYPE_ORIENTATION:
                data.setOrientation_x(values[1]);
                //Log.e("JY+orientation_x", String.valueOf(data.getOrientation_x()));
                data.setOrientation_y(values[2]);
                data.setOrientation_z(values[0]);
                data.setOrientation_ready(orientation_ready);
                break;
            case Sensor.TYPE_ACCELEROMETER:
                data.setAccelerate_x(values[0]);
                data.setAccelerate_y(values[1]);
                data.setAccelerate_z(values[2]);
                data.setAccelerate_ready(accelerate_ready);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                data.setMagnetic_x(values[0]);
                data.setMagnetic_y(values[1]);
                data.setMagnetic_z(values[2]);
                data.setMagnetic_ready(magnetic_ready);
                break;
            case Sensor.TYPE_PRESSURE:
                data.setBarometer_text(values[0]);
                data.setBarometer_ready(barometer_ready);
                break;
            default:
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }







}
