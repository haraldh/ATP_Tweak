package backslash.atp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.preference.PreferenceManager;

public class Modules {
	private final List<String> modules = new ArrayList<String>();
	
	public Modules(AssetManager am, Context context, int versionCode) {
		int storedVersionCode;
		SharedPreferences mPrefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		SharedPreferences.Editor ed = mPrefs.edit();

		storedVersionCode = mPrefs.getInt("versionCode", 0);

		String assets[] = null;
		try {
			assets = am.list("");
		} catch (IOException ex) {
			;
		}
		for (int i = 0; i < assets.length; ++i) {
			if (!assets[i].endsWith(".ko"))
				continue;
			
			File f = context.getFileStreamPath(assets[i]);

			modules.add(assets[i].substring(0, assets[i].length() - 3));

			if (f.exists() && versionCode <= storedVersionCode)
				continue;

			InputStream from;
			FileOutputStream to;
			try {
				from = am.open(assets[i]);
				to = context.openFileOutput(assets[i], Context.MODE_PRIVATE);
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
		Collections.sort(modules);
		ed.putInt("versionCode", versionCode);
		ed.apply();
	}

	public String[] lsmod() {
//		List<String> modules = new ArrayList<String>();
		return (String[]) modules.toArray(new String[0]);
	}
	
	public List<String> lsmodList() {
		return modules;
	}

	public String[] getModules() {
		return (String[]) modules.toArray(new String[0]);
	}


}
