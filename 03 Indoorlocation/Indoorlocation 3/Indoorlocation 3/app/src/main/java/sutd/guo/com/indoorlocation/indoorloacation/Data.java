package sutd.guo.com.indoorlocation.indoorloacation;

/**
 * Created by Pradeep on 3/3/2017.
 */

public class Data {

    private static Data instance = null;

    public static Data getInstance(){
        if(instance == null){

            instance = new Data();
        }

        return instance;

    }
    private float orientation_x, orientation_y, orientation_z;
    boolean orientation_ready=false,accelerate_ready=false,magnetic_ready=false,barometer_ready=false;

    private float accelerate_x, accelerate_y, accelerate_z;

    //  private float EarthAccelerate_x,EarthAccelerate_y,EarthAccelerate_z;

    private float magnetic_x, magnetic_y, magnetic_z;

    private float barometer_text;


    //orientation

    public float getOrientation_x(){
        return orientation_x;
    }

    public void setOrientation_x(float orientation_x) {
        this.orientation_x = orientation_x;
    }

    public float getOrientation_y(){
        return orientation_y;
    }


    public void setOrientation_y(float orientation_y) {
        this.orientation_y = orientation_y;
    }

    public float getOrientation_z() {
        return orientation_z;
    }

    public void setOrientation_z(float orientation_z) {
        this.orientation_z = orientation_z;
    }

    public void setOrientation_ready(boolean Orientation_ready){this.orientation_ready= Orientation_ready;}

    public boolean getOrientation_ready(){return orientation_ready;}

    //Accelerate
    public float getAccelerate_x(){
        return accelerate_x;
    }

    public void setAccelerate_x(float accelerate_x) {
        this.accelerate_x = accelerate_x;
    }

    public float getAccelerate_y(){
        return accelerate_y;
    }

    public void setAccelerate_y(float accelerate_y) {
        this.accelerate_y = accelerate_y;
    }

    public float getAccelerate_z(){
        return accelerate_z;
    }

    public void setAccelerate_z(float accelerate_z) {
        this.accelerate_z = accelerate_z;
    }

    public void setAccelerate_ready(boolean Accelerate_ready){this.accelerate_ready=Accelerate_ready;}

    public boolean getAccelerate_ready(){return accelerate_ready;}

    //magnetic
    public float getMagnetic_x(){
        return magnetic_x;
    }

    public void setMagnetic_x(float magnetic_x) {
        this.magnetic_x = magnetic_x;
    }

    public float getMagnetic_y(){
        return magnetic_y;
    }

    public void setMagnetic_y(float magnetic_y) {
        this.magnetic_y = magnetic_y;
    }

    public float getMagnetic_z(){
        return magnetic_z;
    }

    public void setMagnetic_z(float magnetic_z) {
        this.magnetic_z = magnetic_z;
    }

    public void setMagnetic_ready(boolean Magnetic_ready){this.magnetic_ready=Magnetic_ready;}

    public boolean getMagnetic_ready(){return magnetic_ready;}



    //barometer
    public  float getBarometer_text(){
        return barometer_text;
    }

    public  void setBarometer_text(float barometer_text){
        this.barometer_text = barometer_text;
    }

    public void setBarometer_ready(boolean Barometer_ready){this.barometer_ready=Barometer_ready;}

    public boolean getBarometer_ready(){return barometer_ready;}


}