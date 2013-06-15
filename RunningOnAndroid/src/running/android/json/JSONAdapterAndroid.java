package running.android.json;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.maps.GeoPoint;

/**
 * JSONAdapter for classes that are exclusively in Android
 */
public class JSONAdapterAndroid {
	public static final String LAT_E6 = "a";
    public static final String LON_E6 = "o";
    
	public static GeoPoint JSONToGeoPoint(String jsonStr) throws JSONException {
		JSONObject jsonObj = new JSONObject(jsonStr);
		return new GeoPoint(jsonObj.getInt(LAT_E6), jsonObj.getInt(LON_E6));
	}
	
	public static List<GeoPoint> JSONToGeoPointList(String jsonStr) throws JSONException {
		List<GeoPoint> geoPoints = new ArrayList<GeoPoint>();
		JSONArray jsonArray = new JSONArray(jsonStr);
		for (int i = 0; i < jsonArray.length(); i++) {
			geoPoints.add(JSONToGeoPoint(jsonArray.getJSONObject(i).toString()));
		}
		return geoPoints;
	}
}
