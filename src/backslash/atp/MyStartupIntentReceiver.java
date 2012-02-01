package backslash.atp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyStartupIntentReceiver extends BroadcastReceiver {

	@Override

	public void onReceive(Context context, Intent intent) {
		Intent serviceIntent = new Intent();
		serviceIntent.setAction("backslash.atp.ServiceModuleLoader");
		context.startService(serviceIntent);
	}
}
