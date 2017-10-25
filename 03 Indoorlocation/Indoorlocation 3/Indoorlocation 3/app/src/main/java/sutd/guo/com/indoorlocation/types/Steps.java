package sutd.guo.com.indoorlocation.types;

import java.util.ArrayList;

/**
 * Created by Pradeep on 3/3/2017.
 */

public class Steps implements Cloneable {
    public ArrayList<Long> timeList = new ArrayList<Long>();
    public ArrayList<Float> stepLengthList = new ArrayList<Float>();

    @Override
    public Object clone() {
        Steps object = null;
        try {
            object = (Steps) super.clone();

            object.timeList = new ArrayList<Long>();
            for(int i=0;i<timeList.size();i++){
                long temp = timeList.get(i).longValue();
                object.timeList.add(temp);
            }

            object.stepLengthList = new ArrayList<Float>();
            for(int i=0;i<stepLengthList.size();i++){
                float temp = stepLengthList.get(i).floatValue();
                object.stepLengthList.add(temp);
            }

        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return object;
    }

    public int size(){
        return timeList.size();
    }

    public long getStepTime(int index){
        if(index>=0 && index<timeList.size()){
            return timeList.get(index);
        }else {
            return 0;
        }
    }

    public float getStepLength(int index){
        if(index>=0 && index<timeList.size()){
            return stepLengthList.get(index);
        }else {
            return 0;
        }
    }

    public void clear() {
        timeList.clear();
        stepLengthList.clear();
    }
}