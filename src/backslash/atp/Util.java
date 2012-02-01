package backslash.atp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

public class Util {

    private static final String TAG = "ATP_Tweak";
    private static final String scriptFileName = "commands.sh";
    
	public static String suExec(Context context, String script) {
        StringBuilder output = new StringBuilder();		
		String progArray[] = { "su", "-c", "source " + context.getFileStreamPath(scriptFileName) };
		FileOutputStream f;
		try {
			f = context.openFileOutput(scriptFileName, Context.MODE_PRIVATE );
			f.write(script.getBytes());
			f.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
	        return output.toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
	        return output.toString();
		}
		
		Process p;
		try {
			p = Runtime.getRuntime().exec(progArray);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
	        return output.toString();
		}
		try {
			p.waitFor();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
	        return output.toString();
		}
		
		BufferedReader in =
                new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        try {
			while ((line = in.readLine()) != null) {
			    output.append(line);
			    output.append("\n");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
	        return output.toString();
		}

        return output.toString();
	}


	/**
	 * This will launch the Android market looking for SuperUser
	 * 
	 * @param activity
	 *            pass in your Activity
	 */
	public static void offerSuperUser(Activity activity) {
		Intent i = new Intent(Intent.ACTION_VIEW,
				Uri.parse("market://details?id=com.noshufou.android.su"));
		activity.startActivity(i);
	}

	/**
	 * This will launch the Android market looking for SuperUser, but will
	 * return the intent fired and starts the activity with
	 * startActivityForResult
	 * 
	 * @param activity
	 *            pass in your Activity
	 * @param requestCode
	 *            pass in the request code
	 * @return intent fired
	 */
	public static Intent offerSuperUser(Activity activity, int requestCode) {
		Intent i = new Intent(Intent.ACTION_VIEW,
				Uri.parse("market://details?id=com.noshufou.android.su"));
		activity.startActivityForResult(i, requestCode);
		return i;
	}	
	
    public static final Boolean canGainSu(Context context) {

        String suTestScript = "echo ";
        String suTestScriptValid = "SuPermsOkay";

        String output = suExec(context,
                suTestScript + suTestScriptValid);

        if (output.trim().equals(suTestScriptValid)) {
            Log.d(TAG, "Superuser command auth confirmed");
            return true;

        } else {
            Log.d(TAG, "Superuser command auth refused");
            return false;

        }

    }	
	
	public static void loadModule(Context context, String module) {
		suExec(context, "insmod " + context.getFileStreamPath(module + ".ko").getPath());
	}

	public static void unloadModule(Context context, String module) {
		suExec(context, "rmmod " + module.replace("-", "_"));		
	}

	public static void setScheduler(Context context, String scheduler) {
		suExec(context, "echo " + scheduler + " > /sys/block/mmcblk0/queue/scheduler");

	}

	public static void cfq_load(Context context) {
		loadModule(context, "cfq-iosched");
		setScheduler(context, "cfq");
	}

	public static void cfq_unload(Context context) {
		setScheduler(context, "noop");
	}

	public static String readScheduler() {
		File f = new File("/sys/block/mmcblk0/queue/scheduler");
		try {
			FileReader freader = new FileReader(f);
			BufferedReader bufRead = new BufferedReader(freader);
			String scheduler = bufRead.readLine();
			bufRead.close();
			freader.close();
			return scheduler;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "[noop]";
	}

	public static String[] getSchedulers() {
		String scheds = readScheduler();
		String schedset[]= scheds.split(" ");
		for (int i=0; i < schedset.length; i++) {
			if (schedset[i].startsWith("["))
				schedset[i] = schedset[i].substring(1, schedset[i].length()-1);
		}
		return schedset; 
	}

	public static String getActiveScheduler() {
		String scheds = readScheduler();
		String schedset[]= scheds.split(" ");
		for (int i=0; i < schedset.length; i++) {
			if (schedset[i].startsWith("[")) {
				schedset[i] = schedset[i].substring(1, schedset[i].length()-1);
				return schedset[i];
			}
		}
		return "noop"; 
	}
	
	public static boolean is_cfq_loaded() {
		String scheduler = readScheduler();
		if (scheduler.contains("[cfq]"))
			return true;
		return false;
	}
}
