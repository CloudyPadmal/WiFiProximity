package sutd.guo.com.indoorlocation.types;

/**
 * Created by Pradeep on 3/3/2017.
 */

public class SensorValue{
    SensorType sensorType = SensorType.NONE;
    public float[] values = new float[4];
    public float xValue = 0, yValue = 0, zValue = 0;
    public float xValue_c = 0, yValue_c = 0, zValue_c = 0;
    public float[] accelerometerValues = new float[3];
    public float[] magneticFieldValues = new float[3];
    public long time = 0;
}
