package backslash.atp;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;

public class ServiceModuleLoader extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * @Override public void onCreate() { super.onCreate();
	 * 
	 * Toast.makeText(this, "Service Created", Toast.LENGTH_LONG).show();
	 * 
	 * }
	 * 
	 * @Override public void onDestroy() { super.onDestroy();
	 * 
	 * Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
	 * 
	 * }
	 */

	@Override
	public void onStart(Intent intent, int startId) {

		super.onStart(intent, startId);
		Context context = getApplicationContext();
		
		SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		Boolean onStartup = mPrefs.getBoolean("onStartup", false);

		if (onStartup) {
/*			
			HashSet<String> default_modules = new HashSet<String>();
			default_modules.add("cfg-iosched");
			Set<String> module_set = mPrefs.getStringSet("modules",
					default_modules);
			Iterator<String> iter = module_set.iterator();
			while (iter.hasNext()) {
				Util.loadModule(context, iter.next());
			}
			String scheduler = mPrefs.getString("scheduler", "cfq");
			Util.setScheduler(scheduler);
			*/
			Util.cfq_load(context);
		}
		stopSelf();
	}
}
