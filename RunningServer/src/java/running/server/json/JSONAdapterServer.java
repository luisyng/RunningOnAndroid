/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package running.server.json;

import java.util.List;
import running.server.domain.CircuitPoint;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * JSONAdapter for classes that are exclusively in the server
 */
public class JSONAdapterServer {

    public static final String LAT_E6 = "a";
    public static final String LON_E6 = "o";
    public static final String IDATHLETE = "idAth";
    public static final String DISTANCE = "dis";
    public static final String CENTER = "center";
    public static final String EDGE = "edge";


    public static JSONObject circuitPointToJSON(CircuitPoint cp)
            throws JSONException {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put(LAT_E6, cp.getLatitudeE6());
        jsonObj.put(LON_E6, cp.getLongitudeE6());
        return jsonObj;
    }

    public static JSONArray circuitPointListToJSON(List<CircuitPoint> circuitPoints)
            throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for (CircuitPoint cp : circuitPoints) {
            jsonArray.put(circuitPointToJSON(cp));
        }
        return jsonArray;
    }
}
