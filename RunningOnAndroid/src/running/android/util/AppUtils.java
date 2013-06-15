package running.android.util;

import running.domain.Athlete;
import running.json.JSONAdapter;

import org.json.JSONException;

import android.content.Context;
import android.util.Log;

public class AppUtils {

	private static final String LOGGED_ATHLETE = "logged_athlete";
	private static final String PREFERENCES_FILE = "main";
	
	/**
	 * Gets the logged athlete from the android shared preferences
	 */
	public static Athlete getLoggedAthlete(Context context) {
		String athleteStr = context.getSharedPreferences(PREFERENCES_FILE, 
				Context.MODE_PRIVATE).getString(LOGGED_ATHLETE, null);
		if (athleteStr == null) {
			return null;
		} else {
			try {
				return JSONAdapter.JSONToAthlete(athleteStr, null);
			} catch (JSONException e) {
				Log.e("", "Error parsing the logged athlete");
			}
		}
		return null;
	}
	
	/**
	 * Deletes the logged athlete from the android shared preferences
	 */
	public static void removeLoggedAthlete(Context context) {
		context.getSharedPreferences(PREFERENCES_FILE, 
				Context.MODE_PRIVATE).edit().remove(LOGGED_ATHLETE).commit();
	}
}
