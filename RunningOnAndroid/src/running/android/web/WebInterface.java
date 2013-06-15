package running.android.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import running.android.app.MyItemizedOverlay;
import running.android.app.WebService;
import running.android.domain.Circuit;
import running.android.json.JSONAdapterAndroid;
import running.android.util.CircuitManager;
import running.android.util.MutexManager;

import com.google.android.maps.GeoPoint;

import android.app.Activity;
import android.util.Log;

import running.domain.Athlete;
import running.domain.Comment;
import running.domain.Competition;
import running.json.JSONAdapter;

public class WebInterface {
	//private static final String BASE_URL =  "http://192.168.1.35:8080/RunningServer/";
	private static final String BASE_URL = "http://isf.etsit.upm.es/RunningServer/";
	private static final String ATHLETES_URL = BASE_URL + "athletes.do";
	private static final String COMMENTS_URL = BASE_URL + "comments.do";
	private static final String COMPETITIONS_URL = BASE_URL + "competitions.do";
	private static final String CIRCUIT_URL = BASE_URL + "circuit.do";
	private static final String SEND_DISTANCE_URL = BASE_URL
			+ "sendDistance.do";
	private static final String GET_DISTANCES_URL = BASE_URL
			+ "getDistances.do";
	private static final String STOP_SIMULATING_URL = BASE_URL
			+ "stopSimulating.do";
	private static final String LOGIN_URL = BASE_URL + "login.do";
	private static final String CHECK_HAS_STARTED_URL = BASE_URL
			+ "checkHasStarted.do";
	private static final String SET_HAS_STARTED_URL = BASE_URL
			+ "setHasStarted.do";
	private static final String TEST_COMPETITION_URL = BASE_URL
			+ "testCompetition.do";
	private static final String TEST_CIRCUITS_URL = BASE_URL
			+ "testCircuits.do";
	private static final String POST_COMMENT_URL = BASE_URL + "postComment.do";
	private static final String SEND_IMAGE_URL = BASE_URL + "sendImage.do";

	// Parameters
	private final static String IDCOMPETITION = "id";
	private final static String IDCIRCUIT = "idCir";
	private final static String IDATHLETE = "idAth";
	private final static String DISTANCE = "dis";
	private final static String USERNAME = "user";
	private final static String PASSWORD = "pw";
	private final static String COMMENT = "cm";
	private final static String LAST_IDCOMMENT = "lastIdc";
	private final static String LATE6 = "lat";
	private final static String LONE6 = "lon";

	/**
	 * Connects to an url and returns the text of the HTTPResponse
	 * 
	 * @param urlStr
	 * @param params
	 * @return
	 */
	private static String connectToServer(String urlStr, String[] params) {
		try {

			// Encode the params
			for (int i = 0; i < params.length; i++) {
				params[i] = URLEncoder.encode(params[i], "UTF-8");
			}

			// Add the params
			if (params.length > 0) {
				urlStr += "?";
				for (int i = 0; i < params.length; i = i + 2) {
					if (i > 0) {
						urlStr += "&";
					}
					urlStr += params[i] + "=" + params[i + 1];
				}
			}
			Log.d("WEB", "URL=" + urlStr);

			// Get the URL
			URL url = new URL(urlStr);

			// Connect via HTTP
			HttpURLConnection httpConnection = (HttpURLConnection) url
					.openConnection();
			httpConnection.setRequestProperty("Content-type",
					"application/json;charset=UTF8");
			if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				Log.d("WEB", "HTTP connection OK");
				// Buffer to save the lines of the server response
				StringBuffer content = new StringBuffer();

				// Input stream and buffer
				InputStream in = httpConnection.getInputStream();

				BufferedReader bf = new BufferedReader(new InputStreamReader(
						in, "UTF8"));

				// Read the different lines
				String line;
				while ((line = bf.readLine()) != null) {
					content.append(line);
				}
				return new String(content.toString().getBytes(), "UTF8");

				// return content.toString();
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "[]"; // Default: Empty JSON array
	}

	/**
	 * Adds all the competitions to the list
	 */
	public static void addCompetitions(Activity activity,
			List<Competition> competitions) {
		// Clear the competitions
		competitions.clear();
		// Connect to the server
		String jsonStr = connectToServer(COMPETITIONS_URL, new String[0]);
		// To competition list
		try {
			for (Competition comp : JSONAdapter.JSONToCompetitionList(jsonStr)) {
				competitions.add(comp);
			}
			Log.d("JSON", "JSON from web parsed correctly.");
		} catch (JSONException e) {
			Log.e("JSON", "Error parsing the JSON");
			// Returns an empty arrayList
		}
	}

	/**
	 * Gets the circuit of a competition
	 */
	public static Circuit getCircuit(int idCompetition) {
		String[] params = { IDCOMPETITION, "" + idCompetition };
		String jsonStr = connectToServer(CIRCUIT_URL, params);
		try {
			List<GeoPoint> circuitPoints = JSONAdapterAndroid
					.JSONToGeoPointList(jsonStr);
			return CircuitManager.createCircuit(circuitPoints);
		} catch (JSONException e) {
			Log.e("JSON", "Error parsing the JSON");
			return null;
		}
	}

	/**
	 * Adds all athletes to the list
	 */
	public static void addAthletes(List<Athlete> athletes,
			Competition competition) {
		String[] params = { IDCOMPETITION, "" + competition.getId() };
		String jsonStr = connectToServer(ATHLETES_URL, params);
		athletes.clear();
		List<Athlete> mAthletes = new ArrayList<Athlete>();
		try {
			mAthletes = JSONAdapter.JSONToAthleteList(jsonStr, competition);
		} catch (JSONException e) {
			Log.e("JSON", "Error parsing the JSON");
		}
		for (Athlete athlete : mAthletes) {
			athletes.add(athlete);
		}
	}

	/**
	 * Adds all comments to the list
	 */
	public static void addComments(List<Comment> comments,
			Competition competition, int lastIdComment, MutexManager mutex,
			MyItemizedOverlay overlay) {
		String[] params = { IDCOMPETITION, "" + competition.getId(),
				LAST_IDCOMMENT, "" + lastIdComment };
		String jsonStr = connectToServer(COMMENTS_URL, params);
		List<Comment> mComments = new ArrayList<Comment>();
		try {
			mComments = JSONAdapter.JSONToCommentList(jsonStr);
		} catch (JSONException e) {
			Log.e("JSON", "Error parsing the JSON");
		}
		if (overlay != null) {
			overlay.addComments(mComments);
		}
		mutex.startToReadOrModify();
		for (int i = mComments.size() - 1; i >= 0; i--) {
			comments.add(0, mComments.get(i));
			Log.i("COMM", comments.get(0).getText()); // TODO
		}
		mutex.endReadingOrModifying();

	}

	/**
	 * Sends the current distance from start of the athlete
	 */
	public static void sendParticipationPoint(int idAthlete, int idCompetition,
			int distance) {
		String[] params = { IDATHLETE, "" + idAthlete, IDCOMPETITION,
				"" + idCompetition, DISTANCE, "" + distance };
		connectToServer(SEND_DISTANCE_URL, params);
	}

	/**
	 * Updates all the athletes distances and positions
	 */
	public static void refreshAthletesDistancesFromStart(
			Competition competition, List<Athlete> athletes,
			WebService webService, MutexManager mutex) {
		String[] params = { IDCOMPETITION, "" + competition.getId() };
		String jsonStr = connectToServer(GET_DISTANCES_URL, params);
		mutex.startToReadOrModify();
		try {
			JSONAdapter.JSONToParticipations(jsonStr, athletes, competition);
			Log.i("WEBSERVICE", "JSON parsed correctly");
		} catch (JSONException e) {
			Log.e("JSON", "Error parsing the JSON");
		}
		mutex.endReadingOrModifying();
	}

	/**
	 * Tries to log in
	 * 
	 * @param username
	 * @param password
	 * @return the athlete if username and password are correct
	 */
	public static Athlete logIn(String username, String password) {
		String[] params = { USERNAME, username, PASSWORD, password };
		String jsonStr = connectToServer(LOGIN_URL, params);
		// Not correctly logged in
		if (jsonStr == "0") {
			return null;
		}
		try {
			return JSONAdapter.JSONToAthlete(jsonStr, null);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Checks if the competition has started
	 */
	public static boolean checkHasStarted(Competition competition) {
		String[] params = { IDCOMPETITION, "" + competition.getId() };
		String jsonStr = connectToServer(CHECK_HAS_STARTED_URL, params);
		Log.d("WEB", jsonStr);
		if (jsonStr.equals("0")) {
			return false;
		}
		try {
			competition.setRealDate(new SimpleDateFormat(
					JSONAdapter.REAL_DATE_FORMAT).parse(jsonStr));
			return true;
		} catch (ParseException e) {
			return false;
		}
	}

	/**
	 * Sets the competition has started and starts the simulation
	 */
	public static void setHasStarted(int idCompetition) {
		String[] params = { IDCOMPETITION, "" + idCompetition };
		connectToServer(SET_HAS_STARTED_URL, params);
		Log.d("WEB", "Set has started, idcompetition = " + idCompetition);
	}

	/**
	 * Get test circuit names
	 */
	public static String[] getCircuitNames() {
		String[] params = {};
		String jsonStr = connectToServer(TEST_CIRCUITS_URL, params);
		Log.d("WEB", "Get circuit names");
		try {
			return JSONAdapter.JSONToCircuitNames(jsonStr);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Creates a test competition
	 */
	public static void createTestCompetition(int idCircuit) {
		String[] params = { IDCIRCUIT, "" + idCircuit };
		connectToServer(TEST_COMPETITION_URL, params);
		Log.d("WEB", "Create test competition ");
	}

	/**
	 * Stops the simulation
	 */
	public static void stopSimulation(int idCompetition) {
		String[] params = { IDCOMPETITION, "" + idCompetition };
		connectToServer(STOP_SIMULATING_URL, params);
		Log.d("WEB", "Stop the simulation");
	}

	/**
	 * Posts the comment
	 */
	public static void postComment(Comment c) {
		StringBuilder textModif = new StringBuilder();
		for (int i = 0; i < c.getText().length(); i++) {
			char ch = c.getText().charAt(i);
			if (ch == '#') {
				textModif.append("*");
			} else {
				textModif.append(ch);
			}
		}
		String[] params = { IDCOMPETITION, "" + c.getIdCompetition(), USERNAME,
				"" + c.getWriter(), COMMENT, "" + textModif.toString(), LATE6,
				"" + c.getLatE6(), LONE6, "" + c.getLonE6() };
		connectToServer(POST_COMMENT_URL, params);
		Log.d("WEB", "Post a comment");
	}

	public static void sendImage(byte[] imageData) {

		try {
			// Construct data
			String data = URLEncoder.encode("key1", "UTF-8") + "="
					+ URLEncoder.encode("value1", "UTF-8");
			data += "&" + URLEncoder.encode("key2", "UTF-8") + "="
					+ URLEncoder.encode("value2", "UTF-8");

			// Send data
			URL url = new URL(SEND_IMAGE_URL);
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
			OutputStreamWriter wr = new OutputStreamWriter(conn
					.getOutputStream());
			wr.write(data);
			wr.flush();

			// Get the response
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn
					.getInputStream()));
			String line;
			while ((line = rd.readLine()) != null) {
				// Process line...
			}
			wr.close();
			rd.close();
		} catch (Exception e) {
		}

		/*
		 * try { // Send data URL url = new URL(SEND_IMAGE_URL); URLConnection
		 * conn = url.openConnection(); conn.setDoOutput(true); OutputStream os
		 * = conn.getOutputStream(); os.write(imageData); os.close(); } catch
		 * (Exception e) { }
		 */

	}
}
