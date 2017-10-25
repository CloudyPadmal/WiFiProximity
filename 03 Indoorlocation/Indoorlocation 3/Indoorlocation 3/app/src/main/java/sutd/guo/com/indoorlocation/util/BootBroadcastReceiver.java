package sutd.guo.com.indoorlocation.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import sutd.guo.com.indoorlocation.BackendService;

public class BootBroadcastReceiver extends BroadcastReceiver {

	private Context mContext;

	static final String ACTION = "android.intent.action.BOOT_COMPLETED";


	@Override
	public void onReceive(Context context, Intent intent) {
		mContext = context;
		if (intent.getAction().equals(ACTION)) {

			/*Intent myintent=new Intent(context, BackendService.class);

			context.startService(myintent);*/
			Log.i("myService", "boot up and start Service");
			AlarmUtil.invokeTimerWIFIService(context);
			AlarmUtil.invokeTimerStepService(context);
		}
	}
}
