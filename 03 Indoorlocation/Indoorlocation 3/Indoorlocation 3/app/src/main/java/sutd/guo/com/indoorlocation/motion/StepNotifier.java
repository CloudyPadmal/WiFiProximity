package sutd.guo.com.indoorlocation.motion;

import java.util.ArrayList;

import sutd.guo.com.indoorlocation.preferences.SocialLocSettings;
import sutd.guo.com.indoorlocation.types.Steps;

/**
 * Created by Pradeep on 3/3/2017.
 */

public class StepNotifier implements StepListener{

    Steps steps = new Steps();

    SocialLocSettings mSettings;

    public StepNotifier(SocialLocSettings settings) {
        mSettings = settings;
        notifyListener();
    }


    public void onStep(Steps steps) {
        this.steps = (Steps)steps.clone();
        notifyListener();
    }

    public void reloadSettings() {

    }



    //-----------------------------------------------------
    // Listener

    public interface Listener {
        public void stepsChanged(Steps steps);
        public void passValue();
    }
    private ArrayList<Listener> mListeners = new ArrayList<Listener>();

    public void addListener(Listener l) {
        mListeners.add(l);
    }
    public void notifyListener() {
        for (Listener listener : mListeners) {
            listener.stepsChanged(steps);
        }
    }
}
