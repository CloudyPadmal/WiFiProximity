package sutd.guo.com.indoorlocation.motion;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import sutd.guo.com.indoorlocation.main.LogFileFlusher;
import sutd.guo.com.indoorlocation.main.Util;
import sutd.guo.com.indoorlocation.types.SensorType;
import sutd.guo.com.indoorlocation.types.SensorValue;

/**
 * Created by Pradeep on 3/3/2017.
 */

public class MotionDetector implements SensorEventListener {
    static public class DCMMatrix {
        public float[][] matrix = new float[3][3];
    }

    private DCMMatrix dcmMatrix = new DCMMatrix();
    public boolean isRotationValueSetted = false;
    float[] accelerometerValues = new float[3];
    float[] magneticFieldValues = new float[3];

    //variables for periodic method
    Pedometer pedometer;

    //common variables
    private ArrayList<StepListener> mStepListeners = new ArrayList<StepListener>();

    public MotionDetector() {
        pedometer = new Pedometer(mStepListeners);

        pedometer.setName("pedometer_jy");

        setStepViberationSensitivity(50, 50);

        File fileDirectoryCreate = new File(fileDirectory);
        if (!fileDirectoryCreate.exists()) fileDirectoryCreate.mkdir();
        createFiles(fileDirectory);
    }

    public void startDetecting() {
        pedometer.start();
    }

    public void stopDetecting() {
        closeFiles();
        pedometer.interrupt();
    }

    public void setStepViberationSensitivity(int low, int high) {
        pedometer.setVirbationSensitivity(low, high);
    }


    public void addStepListener(StepListener sl) {
        mStepListeners.add(sl);
    }


    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        synchronized (this) {
            SensorType sensorType = SensorType.NONE;
            switch (sensor.getType()) {
                case Sensor.TYPE_ROTATION_VECTOR:
                    sensorType = SensorType.ROTATION_VECTOR;
                    break;
                case Sensor.TYPE_LINEAR_ACCELERATION:
                    sensorType = SensorType.LINEAR_ACCELEROMETER;
                    break;
                case Sensor.TYPE_GYROSCOPE:
                    sensorType = SensorType.GYROSCOPE;
                    break;
                //修改 2017/1/3
                case Sensor.TYPE_ACCELEROMETER:
                    sensorType = SensorType.ACCELEROMETER;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    sensorType = SensorType.MAGNETIC_FIELD;
                default:
                    break;
            }

            sensorChanged(sensorType, event.values, System.currentTimeMillis());
            //Log.d("onSensorChanged", String.valueOf(event.values[0]));
        }

    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //监听传感器改变
    public void sensorChanged(SensorType sensorType, float[] values, long time) {
        if (sensorType == SensorType.ROTATION_VECTOR) {
            rotationVectorChanged(values, time);
            saveValues(values, time, wRot);
        } else if (sensorType == SensorType.LINEAR_ACCELEROMETER) {//linear accelerometer
            linearAccelerometerChanged(values, time);
            saveValues(values, time, wLAcc);
        } else if (sensorType == SensorType.GYROSCOPE) {//gyroscope
            gyroscopeChanged(values, time);
            saveValues(values, time, wGro);

        } else if (sensorType == SensorType.ACCELEROMETER) {
            accelerometerValues = values.clone();
        } else if (sensorType == SensorType.MAGNETIC_FIELD) {
            magneticFieldValues = values.clone();
        }
        calculateOrientation();
    }

    public void rotationVectorChanged(float[] values, long time) {
        float[] quaternion = new float[4];
        SensorManager.getQuaternionFromVector(quaternion, values);
        float[][] matrix = new float[3][3];
        float a = quaternion[0];
        float b = quaternion[1];
        float c = quaternion[2];
        float d = quaternion[3];

        matrix[0][0] = (float) (Math.pow(a, 2) + Math.pow(b, 2) - Math.pow(c, 2) - Math.pow(d, 2));
        matrix[0][1] = 2 * (b * c - a * d);
        matrix[0][2] = 2 * (b * d + a * c);
        matrix[1][0] = 2 * (b * c + a * d);
        matrix[1][1] = (float) (Math.pow(a, 2) - Math.pow(b, 2) + Math.pow(c, 2) - Math.pow(d, 2));
        matrix[1][2] = 2 * (c * d - a * b);
        matrix[2][0] = 2 * (b * d - a * c);
        matrix[2][1] = 2 * (c * d + a * b);
        matrix[2][2] = (float) (Math.pow(a, 2) - Math.pow(b, 2) - Math.pow(c, 2) + Math.pow(d, 2));

        this.dcmMatrix.matrix = matrix.clone();
        isRotationValueSetted = true;
    }

    public void linearAccelerometerChanged(float[] values, long time) {
        SensorValue sensorValue = new SensorValue();
        sensorValue.values = values.clone();
        sensorValue.time = time;

        if (isRotationValueSetted) {
            sensorValue.zValue = dcmMatrix.matrix[2][0] * sensorValue.values[0] +
                    dcmMatrix.matrix[2][1] * sensorValue.values[1] +
                    dcmMatrix.matrix[2][2] * sensorValue.values[2];
            sensorValue.xValue = dcmMatrix.matrix[0][0] * sensorValue.values[0] +
                    dcmMatrix.matrix[0][1] * sensorValue.values[1] +
                    dcmMatrix.matrix[0][2] * sensorValue.values[2];
            sensorValue.yValue = dcmMatrix.matrix[1][0] * sensorValue.values[0] +
                    dcmMatrix.matrix[1][1] * sensorValue.values[1] +
                    dcmMatrix.matrix[1][2] * sensorValue.values[2];
            pedometer.addSensorValue(sensorValue);

            //modify
            pedometer.onSensorChanged();
        }
    }



    public void gyroscopeChanged(float[] values, long time) {
        SensorValue sensorValue = new SensorValue();
        sensorValue.values = values.clone();
        sensorValue.time = time;

        if (isRotationValueSetted) {
            sensorValue.zValue = dcmMatrix.matrix[2][0] * sensorValue.values[0] +
                    dcmMatrix.matrix[2][1] * sensorValue.values[1] +
                    dcmMatrix.matrix[2][2] * sensorValue.values[2];
            sensorValue.xValue = dcmMatrix.matrix[0][0] * sensorValue.values[0] +
                    dcmMatrix.matrix[0][1] * sensorValue.values[1] +
                    dcmMatrix.matrix[0][2] * sensorValue.values[2];
            sensorValue.yValue = dcmMatrix.matrix[1][0] * sensorValue.values[0] +
                    dcmMatrix.matrix[1][1] * sensorValue.values[1] +
                    dcmMatrix.matrix[1][2] * sensorValue.values[2];
            //	turningMeter.addSensorValue(sensorValue);

            //modify
            //	turningMeter.onSensorChanged();
        }
    }



    private void calculateOrientation() {
        float[] values = new float[3];
        float[] R = new float[9];
        SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticFieldValues);
        SensorManager.getOrientation(R, values);

        // 要经过一次数据格式的转换，转换为度
        values[0] = (float) Math.toDegrees(values[0]);
        //Log.i("angle", values[0] + " " + values[1] + " " + values[2]);
        //values[1] = (float) Math.toDegrees(values[1]);
        //values[2] = (float) Math.toDegrees(values[2]);
    }

    public String fileDirectory = Util.logFileDirectory;
    public LogFileFlusher logFIleFlusher = new LogFileFlusher();
    public BufferedWriter wLAcc, wRot, wGro;

    boolean createFiles(String directory) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH_mm_ss_SSS_dd", Locale.getDefault());
        String timeString = sdf.format(new java.util.Date());

        String fileName;
        File file;
        try {
            fileName = directory + "/lacc_raw_" + timeString + ".dat";
            file = new File(fileName);
            wLAcc = new BufferedWriter(new FileWriter(file));
            logFIleFlusher.add(wLAcc);

            fileName = directory + "/rot_raw_" + timeString + ".dat";
            file = new File(fileName);
            wRot = new BufferedWriter(new FileWriter(file));
            logFIleFlusher.add(wRot);

            fileName = directory + "/gro_raw_" + timeString + ".dat";
            file = new File(fileName);
            wGro = new BufferedWriter(new FileWriter(file));
            logFIleFlusher.add(wGro);

            Util.addLogFileExceptTag(timeString);
            logFIleFlusher.start();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    void closeFiles() {
        try {
            logFIleFlusher.stop();
            wLAcc.close();
            wRot.close();
            wGro.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void saveValues(float[] values, long time, BufferedWriter bufferedWriter) {

        try {
            float avg = (values[0] + values[1] + values[2]) / 3;
            String fileString = String.format("%f\t%f\t%f\t%f\t%d\n", values[0], values[1], values[2], avg, time);
            bufferedWriter.write(fileString);
            //bufferedWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}