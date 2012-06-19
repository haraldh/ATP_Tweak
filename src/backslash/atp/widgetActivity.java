package backslash.atp;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;

public class widgetActivity extends Activity {
	public static final String LOG_TAG = "ATP_Tweaks";
	final int toggleids[] = { R.id.toggleButtonPowerSaving,
			R.id.toggleButtonBalanced, R.id.toggleButtonPerformance,
			R.id.toggleButtonTurbo1, R.id.toggleButtonTurbo2 };

	private ListView listview = null;
	private CheckBox checkBox = null;
	private Modules modules = null;

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

	private void updateSpinner() {
		String scheds[] = Util.getSchedulers();
		Spinner s = (Spinner) findViewById(R.id.spinnerScheduler);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, scheds);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		s.setAdapter(adapter);

		SharedPreferences mPrefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		String scheduler = mPrefs.getString("scheduler", "cfq");

		for (int i = 0; i < scheds.length; i++) {
			if (scheduler.equals(scheds[i]))
				s.setSelection(i);
		}

	}

	private void updateModules() {
		String[] modloaded = modules.getModules();
		List<String> lsmod = modules.lsmodList();
		for (int i = 0; i < modloaded.length; i++) {
			if (lsmod.contains(modloaded[i]))
				listview.setItemChecked(i, true);
			else
				listview.setItemChecked(i, false);
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		checkSuPerm();

		setContentView(R.layout.main);

		int versionCode;
		try {
			versionCode = getPackageManager().getPackageInfo(getPackageName(),
					0).versionCode;
		} catch (NameNotFoundException e) {
			versionCode = 1;
		}
		AssetManager am = getResources().getAssets();
		modules = new Modules(am, this, versionCode);
		String[] modloaded = modules.getModules();

		setupModulesListView(modloaded);

		setupSchedulerCheckBox();

		setupCPUToggleButtons();

		SharedPreferences mPrefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		Boolean onStartup = mPrefs.getBoolean("onStartup", false);
		SharedPreferences.Editor ed = mPrefs.edit();
		ed.putBoolean("onStartup", onStartup);
		ed.apply();
		checkBox.setChecked(onStartup);

		String scheduler = mPrefs.getString("scheduler", "noop");
		ed.putString("scheduler", scheduler);
		ed.apply();

		Util.setScheduler(this, scheduler);

		Spinner s = (Spinner) findViewById(R.id.spinnerScheduler);
		s.setOnItemSelectedListener(new MyOnItemSelectedListener());
		updateSpinner();
		updateStatus();
	}

	private void setupCPUToggleButtons() {
		ToggleButton.OnClickListener togglelistener = new ToggleButton.OnClickListener() {
			public void onClick(View v) {
				int id = v.getId();
				ToggleButton togglebutton = (ToggleButton) findViewById(id);

				for (int i = 0; i < toggleids.length; i++) {
					if (toggleids[i] == id) {
						togglebutton.setChecked(true);
						String script = "source /system/etc/cpu" + (i + 1)
								+ ".sh\n";
						/*
						 * Toast.makeText(getBaseContext(), script,
						 * Toast.LENGTH_LONG).show();
						 */
						Context context = getApplicationContext();

						Util.suExec(context, script);
					} else {
						ToggleButton tb = (ToggleButton) findViewById(toggleids[i]);
						tb.setChecked(false);
					}
				}
			}
		};

		for (int i : toggleids) {
			ToggleButton togglebutton = (ToggleButton) findViewById(i);
			togglebutton.setOnClickListener(togglelistener);
		}
	}

	private void setupSchedulerCheckBox() {
		checkBox = (CheckBox) findViewById(R.id.checkBoxCFQ);

		OnCheckedChangeListener listener = new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				Context context = getApplicationContext();
				SharedPreferences mPrefs = PreferenceManager
						.getDefaultSharedPreferences(context);
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
	}

	private void setupModulesListView(String[] modloaded) {
		listview = (ListView) findViewById(R.id.listViewModules);
		listview.setAdapter(new ArrayAdapter<String>(widgetActivity.this,
				android.R.layout.simple_list_item_multiple_choice,
				android.R.id.text1, modloaded) {

			public boolean areAllItemsEnabled() {
				return false;
			}

			public boolean isEnabled(int position) {
				// return false if position == position you want to disable
				String module = (String) getItem(position);
				if (module.equals(Util.getActiveScheduler() + "-iosched"))
					return false;
				return true;
			}
		});

		listview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

		updateModules();

		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View arg1, int pos,
					long arg3) {
				Context context = getApplicationContext();
				ListView lv = (ListView) parent;
				SparseBooleanArray cp = lv.getCheckedItemPositions();
				StringBuilder b = new StringBuilder();
				for (int k = 0; k < cp.size(); k++) {
					int i = cp.keyAt(k);
					if (i < 0)
						continue;
					String module = (String) lv.getItemAtPosition(i);
					boolean checked = cp.valueAt(k);
					if (checked) {
						b.append("insmod "
								+ context.getFileStreamPath(module + ".ko")
										.getPath() + ";");
					} else {
						b.append("rmmod " + module.replace("-", "_") + ";");
					}
				}

				Util.suExec(context, b.toString());
				updateStatus();
				updateSpinner();
				updateModules();
			}
		});
	}

	private Boolean checkSuPerm() {
		return true;
	}

	private Boolean checkSuPerms() {
		if (!Util.canGainSu(this)) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Cannot get Root/Superuser.\nExiting!")
					.setCancelable(false)
					.setNeutralButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
									System.exit(2);
								}
							});
			AlertDialog alert = builder.create();
			alert.show();
			return false;
		}
		return true;
	}

	public class MyOnItemSelectedListener implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			Context context = getApplicationContext();
			String scheduler = parent.getItemAtPosition(pos).toString();
			Util.setScheduler(context, scheduler);
			SharedPreferences mPrefs = PreferenceManager
					.getDefaultSharedPreferences(context);
			SharedPreferences.Editor ed = mPrefs.edit();
			ed.putString("scheduler", scheduler);
			ed.apply();
			updateStatus();
		}

		public void onNothingSelected(AdapterView parent) {
			// Do nothing.
		}
	}

}
