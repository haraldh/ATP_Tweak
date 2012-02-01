package backslash.atp;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import java.io.FileOutputStream;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class widgetActivity extends Activity {
	public static final String LOG_TAG = "ATP_Tweaks";

	private void updateStatus() {
		final TextView tv = (TextView) findViewById(R.id.textView);
		String scheds[] = Util.getSchedulers();
		StringBuilder builder = new StringBuilder();
		builder.append("Available Schedulers: ");
		for (int i = 0; i < scheds.length; i++) {
			builder.append(scheds[i] + " ");
		}
		builder.append("\nActive Scheduler: " + Util.getActiveScheduler());
		tv.setText(builder.toString());
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Context context = getApplicationContext();
		if( ! Util.canGainSu(context) ) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Cannot get Root/Superuser.\nExiting!")
			       .setCancelable(false)
			       .setNeutralButton("OK", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			                dialog.cancel();
			    			System.exit(2);
			           }
			       });
			AlertDialog alert = builder.create();
			alert.show();
			return;
		}
				
		setContentView(R.layout.main);
		checkModules(savedInstanceState);

		final CheckBox checkBox = (CheckBox) findViewById(R.id.checkBoxCFQ);

		OnCheckedChangeListener listener = new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				Context context = getApplicationContext();
				SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
				SharedPreferences.Editor ed = mPrefs.edit();
				if (isChecked) {
					ed.putBoolean("onStartup", true);
					Util.cfq_load(context);
				} else {
					ed.putBoolean("onStartup", false);
					Util.cfq_unload(context);
				}
				ed.apply();
				updateStatus();
			}
		};
		checkBox.setOnCheckedChangeListener(listener);

		SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		Boolean onStartup = mPrefs.getBoolean("onStartup", false);
		SharedPreferences.Editor ed = mPrefs.edit();
		ed.putBoolean("onStartup", onStartup);
		ed.apply();
		checkBox.setChecked(onStartup);

		if (onStartup)
			Util.cfq_load(getApplicationContext());
		else
			Util.cfq_unload(context);

		updateStatus();
	}

	public void checkModules(Bundle savedInstanceState) {
		int versionCode, storedVersionCode;
		Context context = getApplicationContext();
		SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor ed = mPrefs.edit();

		try {
			versionCode = getPackageManager().getPackageInfo(getPackageName(),
					0).versionCode;
		} catch (NameNotFoundException e) {
			versionCode = 1;
		}
		storedVersionCode = mPrefs.getInt("versionCode", 0);

		AssetManager am = getResources().getAssets();
		String assets[] = null;
		try {
			assets = am.list("");
		} catch (IOException ex) {
			;
		}
		for (int i = 0; i < assets.length; ++i) {
			File f = getFileStreamPath(assets[i]);

			if (f.exists() && versionCode <= storedVersionCode)
				continue;

			InputStream from;
			FileOutputStream to;
			try {
				from = am.open(assets[i]);
				to = openFileOutput(assets[i], Context.MODE_PRIVATE);
			} catch (IOException ex) {
				continue;
			}

			try {
				byte[] buffer = new byte[4096];
				int bytesRead;

				try {
					while ((bytesRead = from.read(buffer)) != -1)
						to.write(buffer, 0, bytesRead);
				} catch (IOException e) {
					try {
						to.close();
						to = null;
					} catch (IOException e1) {
						;
					}
					f.delete();
				}
			} finally {
				if (from != null)
					try {
						from.close();
					} catch (IOException e) {
						;
					}
				if (to != null)
					try {
						to.close();
					} catch (IOException e) {
						;
					}
			}
		}

		ed.putInt("versionCode", versionCode);
		ed.commit();
	}
}
