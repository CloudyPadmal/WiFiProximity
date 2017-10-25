package sutd.guo.com.indoorlocation.motion;

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
import sutd.guo.com.indoorlocation.types.SensorValue;
import sutd.guo.com.indoorlocation.types.Steps;

/**
 * Created by Pradeep on 3/3/2017.
 */

public class Pedometer extends Thread {
    public static final String TAG = "Pedometer";
    private long sensorNumOfHead = 1;

    //////////////////////////////////////////////////////parameter
    private final float viberationThresholdLowFrom = 0f;
    private final float viberationThresholdLowTo = 3f;
    private final float viberationThresholdHighFrom = 25f;
    private final float viberationThresholdHighTo = 75f;

    private float mStepLength = 0;

    private boolean isLastMatchFail = true;
    private long validPosition = sensorNumOfHead;

    private final float idleThreshold = 0.1f;
    private final float walkingThreshold = 0.7f;
    private final float idleThresholdValue = 1.5f;
    private final float deleteFirstPeakThreshold = 0.33f;
    private final float deleteFirstPeakWidthThreshold = 2f;

    private final int minTau = 24;
    private final int maxTau = 80;

    private int nextMinTau = minTau;
    private int nextMaxTau = maxTau;

    private final float linearDiffThreshold = 3f;
    //////////////////////////////////////////////////////parameter


    private ArrayList<SensorValue> sensorValueBuffer = new ArrayList<SensorValue>();
    private ArrayList<StepListener> mStepListeners = new ArrayList<StepListener>();

    private float viberationThresholdLow = (viberationThresholdLowFrom + viberationThresholdLowTo)/2;
    private float viberationThresholdHigh = (viberationThresholdHighFrom + viberationThresholdHighTo)/2;

    //log
    private final String fileDirectory = Util.logFileDirectory;
    LogFileFlusher logFileFlusher = new LogFileFlusher();
    public BufferedWriter fileWriterStepSensor, fileWriterStepTau, fileWriterStepIdleValue;
    public BufferedWriter fileWriterSteps;


    public Pedometer(ArrayList<StepListener> stepListeners) {
        this.mStepListeners = stepListeners;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH_mm_ss_SSS_dd", Locale.getDefault());
            String timeString = sdf.format(new java.util.Date());

            String fileName = fileDirectory + "/" + timeString + "_stepSensor" + ".dat";
            File file=new File(fileName);
            fileWriterStepSensor = new BufferedWriter(new FileWriter(file) );
            logFileFlusher.add(fileWriterStepSensor);

            fileName = fileDirectory + "/" + timeString + "_stepTau" + ".dat";
            file=new File(fileName);
            fileWriterStepTau = new BufferedWriter(new FileWriter(file) );
            logFileFlusher.add(fileWriterStepTau);

            fileName = fileDirectory + "/" + timeString + "_stepIdle" + ".dat";
            file=new File(fileName);
            fileWriterStepIdleValue = new BufferedWriter(new FileWriter(file) );
            logFileFlusher.add(fileWriterStepIdleValue);

            fileName = fileDirectory + "/" + timeString + "_steps" + ".dat";
            file=new File(fileName);
            fileWriterSteps = new BufferedWriter(new FileWriter(file) );
            logFileFlusher.add(fileWriterSteps);

            Util.addLogFileExceptTag(timeString);
            logFileFlusher.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setVirbationSensitivity(int low,int high) {//low,high:[1:100]
        viberationThresholdLow = (viberationThresholdLowFrom + (low/100f)*(viberationThresholdLowTo-viberationThresholdLowFrom) );
        viberationThresholdHigh = (viberationThresholdHighFrom + (high/100f)*(viberationThresholdHighTo-viberationThresholdHighFrom) ); // 1.97  2.96  4.44  6.66  10.00  15.00  22.50  33.75  50.62
    }

    public void addStepListener(StepListener stepListener){
        this.mStepListeners.add(stepListener);
    }

    @Override
    protected void finalize() throws Throwable {
        logFileFlusher.stop();

        fileWriterStepSensor.close();
        fileWriterStepTau.close();
        fileWriterStepIdleValue.close();

        fileWriterSteps.close();
        super.finalize();
    }


    private void notifyListeners(Steps steps){
        for (StepListener stepListener : mStepListeners) {
            stepListener.onStep(steps);
        }

        //log turnings
        for (int i=0;i<steps.size();i++) {
            String textString = String.format("%d\t%f\n", steps.getStepTime(i),
                    steps.getStepLength(i));
            try {
                fileWriterSteps.write(textString);
                //fileWriterSteps.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void addSensorValue(SensorValue sensorValue){
        synchronized (this) {
            sensorValueBuffer.add(sensorValue);
        }

        //log
        String textString = String.format("%f\t%f\t%f\t%d\n",sensorValue.xValue,sensorValue.yValue,sensorValue.zValue,sensorValue.time);
        try {
            fileWriterStepSensor.write(textString);
            //fileWriterStepSensor.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteSensorValue(int start, int length){
        synchronized (this) {
            while(length>0){
                sensorValueBuffer.remove(start);
                length--;
                sensorNumOfHead++;
            }
        }
        if(validPosition < sensorNumOfHead){
            validPosition = sensorNumOfHead;
        }
    }

    private float mean(int start){
        return mean(start, 2*nextMaxTau);
    }

    private float mean(int start, int length){
        return Pedometer.mean(sensorValueBuffer, start, length);
    }

    public static float mean(ArrayList<SensorValue> sensorValueBuffer, int start, int length){
        int end = start + length;
        if(start > end || end > sensorValueBuffer.size())return -1f;

        float meanValue = 0f;
        for(int i=start;i<end;i++){
            meanValue += sensorValueBuffer.get(i).zValue;
        }
        meanValue /= (end-start);

        return meanValue;
    }

    private float standardDeviation(int start, int length){
        return Pedometer.standardDeviation(sensorValueBuffer, start, length);
    }

    public static float standardDeviation(ArrayList<SensorValue> sensorValueBuffer, int start, int length){
        int end = start + length;
        if(start > end || end > sensorValueBuffer.size())return -1f;

        float standardDeviationValue = 0;
        float meanValue = 0f;
        for(int i=start;i<end;i++){
            meanValue += sensorValueBuffer.get(i).zValue;
        }
        meanValue /= (end-start);

        for(int i=start;i<end;i++){
            standardDeviationValue += Math.pow((sensorValueBuffer.get(i).zValue - meanValue),2);
        }
        standardDeviationValue /= (end-start);

        // TODO Auto-generated catch block
        //return (float)Math.sqrt(standardDeviationValue);

        return (float) Math.sqrt(standardDeviationValue);
    }

    private float absoluteStandardDeviation(int start, int length){
        return Pedometer.absoluteStandardDeviation(sensorValueBuffer, start, length);
    }

    public static float absoluteStandardDeviation(ArrayList<SensorValue> sensorValueBuffer, int start, int length){
        int end = start + length;
        if(start > end || end > sensorValueBuffer.size())return -1f;

        float standardDeviationValue = 0;
        float meanValue = 0f, sensorMeanValue = 0f;
        for(int i=start;i<end;i++){
            sensorMeanValue = sensorValueBuffer.get(i).zValue;
            meanValue += Math.abs(sensorMeanValue);
        }
        meanValue /= (end-start);

        for(int i=start;i<end;i++){
            sensorMeanValue = sensorValueBuffer.get(i).zValue;
            sensorMeanValue = Math.abs(sensorMeanValue);
            standardDeviationValue += Math.pow((sensorMeanValue - meanValue),2);
        }
        standardDeviationValue /= (end-start);

        // TODO Auto-generated catch block
        //return (float)Math.sqrt(standardDeviationValue);
        return (float) Math.sqrt(standardDeviationValue);
    }

    public float Gama(int m,int tau){
        float sum = 0;
        float m1 = mean(m, tau);
        float m2 = mean(m+tau, tau);

        for(int k=0;k<tau;k++){
            float sensorMeanValue = sensorValueBuffer.get(m+k).zValue;
            float v1 = sensorMeanValue - m1;

            sensorMeanValue = sensorValueBuffer.get(m+k+tau).zValue;
            float v2 = sensorMeanValue - m2;

            sum += v1*v2;
        }

        return sum/(tau*standardDeviation(m, tau)*standardDeviation(m+tau, tau));
    }

    public class PosaiResult{
        float posaiM = 0;
        int tauFound = 0;
        float linearDiff = 0;
        float magnitudeUp = 0;
        float magnitudeDown = 0;
        int extremeNum = 0;

        long startPointTimeStamp = 0;
        long middleTimeStamp = 0;
        long endPointTimeStamp = 0;
    }

    public PosaiResult Posai(int m){
        float maxValue = -1;
        int tauFound = 0;
        float linearDiff = 0;
        float magnitudeUp = 0;
        float magnitudeDown = 0;
        int extremeNum = 0;

        for(int tau=nextMinTau;tau<=nextMaxTau;tau++){
            float value = Gama(m, tau);
            if(value>maxValue){
                maxValue = value;
                tauFound = tau;
            }
        }

        for(int i=0;i<tauFound;i++){
            linearDiff += Math.abs(sensorValueBuffer.get(m+i).zValue - sensorValueBuffer.get(m+tauFound+i).zValue);
        }
        linearDiff /= tauFound;

        float   lastValue = sensorValueBuffer.get(m).zValue;
        float   lastDirection = 0;
        //float   lastExtreme[] = {0,0};
        ArrayList<Float> extremes = new ArrayList<Float>();

        for(int i=1;i<tauFound;i++){
            float value = sensorValueBuffer.get(m+i).zValue;
            float direction = (value < lastValue ? 1 : (value > lastValue ? -1 : 0));
            if(direction == -lastDirection && Math.abs(direction)==1){
                // Direction changed
                int extType = (direction > 0 ? 0 : 1); // minimum or maximum? 0 extreme big; 1 extreme small;
                if(extType == 0 && lastValue > viberationThresholdLow){
                    extremes.add(lastValue);
                }
                else if(extType == 1 && lastValue < -viberationThresholdLow){
                    extremes.add(lastValue);
                }
            }
            lastDirection = direction;
            lastValue = value;
        }

        float maxExtreme = 0, minExtreme = 0;//for magnitudeUp
        for(int i=0;i<extremes.size();){//delete small extremes
            if(extremes.get(i) > maxExtreme){
                maxExtreme = extremes.get(i);
            }else if(extremes.get(i) < minExtreme){
                minExtreme = extremes.get(i);
            }

            if(Math.abs(extremes.get(i)) < viberationThresholdLow){
                extremes.remove(i);
            }
            else {
                i++;
            }
        }

        int extremeBigNum = 0,extremeSmallNum = 0;
        float sumExtremeBig = 0,sumExtremeSmall = 0;
        if(extremes.size() > 0){
            extremeNum++;
            if(extremes.get(0)>0){
                extremeBigNum++;
                sumExtremeBig += extremes.get(0);
            }else {
                extremeSmallNum++;
                sumExtremeSmall += extremes.get(0);
            }
        }
        for(int i=1;i<extremes.size();i++){
            if(extremes.get(i)>0){
                extremeBigNum++;
                sumExtremeBig += extremes.get(i);
            }else {
                extremeSmallNum++;
                sumExtremeSmall += extremes.get(i);
            }

            if(extremes.get(i)*extremes.get(i-1)<0){
                extremeNum++;
            }
        }

        float extremeBig = 0,extremeSmall = 0;
        if(extremeBigNum == 0){
            extremeBig = Math.max(sensorValueBuffer.get(m).zValue,
                    Math.max(sensorValueBuffer.get(m+tauFound-1).zValue, maxExtreme));
        }else {
            extremeBig = sumExtremeBig/extremeBigNum;
        }
        if(extremeSmallNum ==0){
            extremeSmall = Math.min(sensorValueBuffer.get(m).zValue,
                    Math.min(sensorValueBuffer.get(m+tauFound-1).zValue, minExtreme));
        }
        else {
            extremeSmall = sumExtremeSmall/extremeSmallNum;
        }
        magnitudeUp = Math.abs(extremeBig);
        magnitudeDown = Math.abs(extremeSmall);

        PosaiResult posaiResult = new PosaiResult();
        posaiResult.posaiM = maxValue;
        posaiResult.tauFound = tauFound;
        posaiResult.linearDiff = linearDiff;
        posaiResult.magnitudeUp = magnitudeUp;
        posaiResult.magnitudeDown = magnitudeDown;
        posaiResult.extremeNum = extremeNum;

        posaiResult.startPointTimeStamp = sensorValueBuffer.get(m).time;
        posaiResult.middleTimeStamp = sensorValueBuffer.get(m+tauFound-1).time;
        posaiResult.endPointTimeStamp = sensorValueBuffer.get(m+tauFound+tauFound-1).time;
        return posaiResult;
    }

    public float SigmaAlpha(int start, int length){
        return absoluteStandardDeviation(start, length);
    }

    public float SigmaAlpha(int m){
        return  SigmaAlpha(m, 2*nextMaxTau);
    }

    public int deleteNoiseNext(int start, int length){
        int end = start + length;

        //find extremes
        float   lastValue = sensorValueBuffer.get(start).zValue;
        float   lastDirection = 0;
        ArrayList<Float> extremes = new ArrayList<Float>();
        ArrayList<Integer> extremesPos = new ArrayList<Integer>();

        for(int i=start+1;i<end;i++){
            float value = sensorValueBuffer.get(i).zValue;
            float direction = (value < lastValue ? 1 : (value > lastValue ? -1 : 0));
            if(direction == -lastDirection && Math.abs(direction)==1){
                // Direction changed
                int extType = (direction > 0 ? 0 : 1); // minimum or maximum? 0 extreme big; 1 extreme small;
                if(extType == 0 && lastValue > viberationThresholdLow){
                    extremes.add(lastValue);
                    extremesPos.add(i);
                }
                else if(extType == 1 && lastValue < -viberationThresholdLow){
                    extremes.add(lastValue);
                    extremesPos.add(i);
                }
            }
            lastDirection = direction;
            lastValue = value;
        }

        //delete some peaks
        for(int i=0;i<extremes.size();i++){
            if(Math.abs(extremes.get(i) - lastIdleMeanValue) < viberationThresholdLow){
                extremes.remove(i);
                extremesPos.remove(i);
                i--;
            }
        }

        int deleteNum = 0;
        if(extremes.size() >= 4){
            float meanExceptFirst = 0;
            float meanIntervalExceptFirst = 0;
            for(int i=1;i<extremes.size();i++){
                meanExceptFirst += Math.abs(extremes.get(i)-lastIdleMeanValue);
            }
            for(int i=2;i<4;i++){
                meanIntervalExceptFirst += extremesPos.get(i) - extremesPos.get(i-1);
            }

            meanExceptFirst /= extremes.size();
            if( (extremesPos.get(1)-extremesPos.get(0))/meanIntervalExceptFirst >= deleteFirstPeakWidthThreshold ){
                deleteNum = Math.max(0, (extremesPos.get(0)+extremesPos.get(1))/2 - start );
            }else if ( Math.abs(extremes.get(0)-lastIdleMeanValue)/meanExceptFirst <= deleteFirstPeakThreshold) {
                deleteNum = Math.max(0, (extremesPos.get(1)+extremesPos.get(2))/2 - start );
            }
        }

        //delete first peak if needed
        if(deleteNum > 0){
            deleteSensorValue(start, deleteNum);
        }

        //System.out.println("next delete, start: " + sensorNumOfHead + ",length: " + length + ",deleted: " + deleteNum + "\n");
        return deleteNum;
    }

    public int deleteNoise(int start, int length) {
        if(length <= 0)return 0;

        int end = start + length;

        int indexFirstPositive = start;
        int indexFirstBigEnough = -1;
        for(int i=start;i<end;i++){
            if(Math.abs(sensorValueBuffer.get(i).zValue) >= viberationThresholdLow){
                indexFirstBigEnough = i;
                break;
            }
        }

        if(indexFirstBigEnough == -1){
            deleteSensorValue(start, length);
            return length;
        }
        else {
            for(int i=indexFirstBigEnough;i>start;i--){
                if( Math.abs(sensorValueBuffer.get(i).zValue - lastIdleMeanValue) < 0.5/* &&
						Math.abs(sensorValueBuffer.get(i).zValue - sensorValueBuffer.get(i-1).zValue) < 0.4*/){
                    indexFirstPositive = i;
                    break;
                }
            }
        }

        int deleteNum = Math.max(0, indexFirstPositive - start);
        if(deleteNum > 0)
        {
            deleteSensorValue(start, deleteNum);
        }

        //System.out.println("start: " + sensorNumOfHead + ",length: " + length + ",deleted: " + deleteNum + ",first big enough: " + indexFirstBigEnough);
        return deleteNum;
    }

    boolean isLastIdleOccur = false;
    float lastIdleMeanValue = 0;
    boolean isLastDeleteNoiseOccur = false;
    boolean isAfterNextDeleteNoise = true;

    private boolean IdleNext(int start){
        return IdleNext(start, 2*nextMaxTau);
    }

    private boolean IdleNext(int start, int lentgh){
        int end = start + lentgh;

        float idleValue = SigmaAlpha(start, lentgh);
        //log
        String textString = String.format("%d\t%f\n",sensorNumOfHead, idleValue);
        try {
            fileWriterStepIdleValue.write(textString);
            //fileWriterStepIdleValue.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(idleValue < idleThreshold){
            return true;
        }



        int bigNumber = 0;
        for(int i=start+1;i<end;i++){
            float value = sensorValueBuffer.get(i).zValue;
            if(Math.abs(value) > idleThresholdValue){
                bigNumber++;
            }
        }

        if(bigNumber==0){
            return true;
        }else {
            return false;
        }
    }

    boolean isDone = false;
    public void done() {
        isDone = true;
    }

    //modify
    public void onSensorChanged(){


        int bufferSize = 0;

        synchronized (this) {
            bufferSize = sensorValueBuffer.size();
        }

        if(bufferSize >= 2*nextMaxTau) {

//            Log.d("onSC_bufferSize", String.valueOf(bufferSize));

            if( !IdleNext(0) ){//not idle
                if(isLastIdleOccur){
                    deleteNoise(0,2*nextMaxTau);
                    isLastDeleteNoiseOccur = true;
                    isLastIdleOccur = false;
                }else if(isLastDeleteNoiseOccur){
                    deleteNoiseNext(0, 2*nextMaxTau);
                    isLastDeleteNoiseOccur = false;
                    isAfterNextDeleteNoise = true;
                }else {
                    PosaiResult posaiResult = Posai(0);
                    if(posaiResult.posaiM > walkingThreshold &&
                            posaiResult.linearDiff < linearDiffThreshold &&
                            (posaiResult.magnitudeUp >= viberationThresholdLow && posaiResult.magnitudeUp <= viberationThresholdHigh) &&
                            (posaiResult.magnitudeDown >= viberationThresholdLow && posaiResult.magnitudeDown <= viberationThresholdHigh)){//walking

                        //log
                        String textString1 = String.format("%d\t%d\t%d\t%f\t%f\t%f\t%f\t%d\t%d\t%d\n",sensorNumOfHead, posaiResult.tauFound, 5,
                                posaiResult.posaiM, posaiResult.linearDiff, posaiResult.magnitudeUp, posaiResult.magnitudeDown,
                                posaiResult.extremeNum,nextMinTau,nextMaxTau);
                        try {
                            fileWriterStepTau.write(textString1);
                            //fileWriterStepTau.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        //notify listeners
                        Steps steps = new Steps();
                        if(isLastMatchFail && sensorNumOfHead >= validPosition){
                            if(posaiResult.extremeNum >= 3 && posaiResult.tauFound >= 2*minTau){
                                steps.timeList.add( (posaiResult.startPointTimeStamp + posaiResult.middleTimeStamp)/2 );
                                steps.stepLengthList.add(mStepLength);
                                steps.timeList.add( posaiResult.middleTimeStamp );
                                steps.stepLengthList.add(mStepLength);
                            }else {
                                steps.timeList.add( posaiResult.middleTimeStamp );
                                steps.stepLengthList.add(mStepLength);
                            }

                            isLastMatchFail = false;
                        }
                        if(posaiResult.extremeNum >= 3 && posaiResult.tauFound >= 2*minTau){
                            steps.timeList.add( (posaiResult.endPointTimeStamp + posaiResult.middleTimeStamp)/2);
                            steps.stepLengthList.add(mStepLength);
                            steps.timeList.add( posaiResult.endPointTimeStamp );
                            steps.stepLengthList.add(mStepLength);
                        }else {
                            steps.timeList.add( posaiResult.endPointTimeStamp );
                            steps.stepLengthList.add(mStepLength);
                        }

                        notifyListeners(steps);

                        if(isAfterNextDeleteNoise){
                            isAfterNextDeleteNoise = false;
                        }

                        //prepare next period
                        nextMinTau = posaiResult.tauFound - 10;
                        nextMaxTau = posaiResult.tauFound + 10;
                        if(nextMinTau < minTau)nextMinTau = minTau;
                        if(nextMaxTau > maxTau)nextMaxTau = maxTau;

                        validPosition = sensorNumOfHead + nextMinTau + posaiResult.tauFound;
                        deleteSensorValue(0, posaiResult.tauFound);
                    }
                    else {//not find a period,moving but not walking
                        deleteSensorValue(0, 1);
                        nextMinTau = minTau;
                        nextMaxTau = maxTau;
                        isLastMatchFail = true;
                        //System.out.println("move but not walking\n");
                    }
                }
            }
            else {//idle
                lastIdleMeanValue = mean(0);
                isLastMatchFail = true;
                isLastIdleOccur = true;
                deleteSensorValue(0, 2*nextMaxTau);
                nextMinTau = minTau;
                nextMaxTau = maxTau;
            }
        }
    }
    @Override
    public void run() {
        super.run();

        int bufferSize = 0;
        while(true){
            try {
                Thread.sleep(20);
            }catch (Exception e){

            }

            synchronized (this) {
                bufferSize = sensorValueBuffer.size();
            }

            if(bufferSize >= 2*nextMaxTau) {

                if( !IdleNext(0) ){//not idle
                    if(isLastIdleOccur){
                        deleteNoise(0,2*nextMaxTau);
                        isLastDeleteNoiseOccur = true;
                        isLastIdleOccur = false;
                    }else if(isLastDeleteNoiseOccur){
                        deleteNoiseNext(0, 2*nextMaxTau);
                        isLastDeleteNoiseOccur = false;
                        isAfterNextDeleteNoise = true;
                    }else {
                        PosaiResult posaiResult = Posai(0);
                        if(posaiResult.posaiM > walkingThreshold &&
                                posaiResult.linearDiff < linearDiffThreshold &&
                                (posaiResult.magnitudeUp >= viberationThresholdLow && posaiResult.magnitudeUp <= viberationThresholdHigh) &&
                                (posaiResult.magnitudeDown >= viberationThresholdLow && posaiResult.magnitudeDown <= viberationThresholdHigh)){//walking

                            //log
                            String textString1 = String.format("%d\t%d\t%d\t%f\t%f\t%f\t%f\t%d\t%d\t%d\n",sensorNumOfHead, posaiResult.tauFound, 5,
                                    posaiResult.posaiM, posaiResult.linearDiff, posaiResult.magnitudeUp, posaiResult.magnitudeDown,
                                    posaiResult.extremeNum,nextMinTau,nextMaxTau);
                            try {
                                fileWriterStepTau.write(textString1);
                                //fileWriterStepTau.flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            //notify listeners
                            Steps steps = new Steps();
                            if(isLastMatchFail && sensorNumOfHead >= validPosition){
                                if(posaiResult.extremeNum >= 3 && posaiResult.tauFound >= 2*minTau){
                                    steps.timeList.add( (posaiResult.startPointTimeStamp + posaiResult.middleTimeStamp)/2 );
                                    steps.stepLengthList.add(mStepLength);
                                    steps.timeList.add( posaiResult.middleTimeStamp );
                                    steps.stepLengthList.add(mStepLength);
                                }else {
                                    steps.timeList.add( posaiResult.middleTimeStamp );
                                    steps.stepLengthList.add(mStepLength);
                                }

                                isLastMatchFail = false;
                            }
                            if(posaiResult.extremeNum >= 3 && posaiResult.tauFound >= 2*minTau){
                                steps.timeList.add( (posaiResult.endPointTimeStamp + posaiResult.middleTimeStamp)/2);
                                steps.stepLengthList.add(mStepLength);
                                steps.timeList.add( posaiResult.endPointTimeStamp );
                                steps.stepLengthList.add(mStepLength);
                            }else {
                                steps.timeList.add( posaiResult.endPointTimeStamp );
                                steps.stepLengthList.add(mStepLength);
                            }

                            notifyListeners(steps);

                            if(isAfterNextDeleteNoise){
                                isAfterNextDeleteNoise = false;
                            }

                            //prepare next period
                            nextMinTau = posaiResult.tauFound - 10;
                            nextMaxTau = posaiResult.tauFound + 10;
                            if(nextMinTau < minTau)nextMinTau = minTau;
                            if(nextMaxTau > maxTau)nextMaxTau = maxTau;

                            validPosition = sensorNumOfHead + nextMinTau + posaiResult.tauFound;
                            deleteSensorValue(0, posaiResult.tauFound);
                        }
                        else {//not find a period,moving but not walking
                            deleteSensorValue(0, 1);
                            nextMinTau = minTau;
                            nextMaxTau = maxTau;
                            isLastMatchFail = true;
                            //System.out.println("move but not walking\n");
                        }
                    }
                }
                else {//idle
                    lastIdleMeanValue = mean(0);
                    isLastMatchFail = true;
                    isLastIdleOccur = true;
                    deleteSensorValue(0, 2*nextMaxTau);
                    nextMinTau = minTau;
                    nextMaxTau = maxTau;
                }
            }
        }
    }

}
