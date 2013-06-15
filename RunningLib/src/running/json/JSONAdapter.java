package running.json;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import running.domain.Athlete;
import running.domain.Comment;
import running.domain.Competition;

/**
 * Convert from JSON to domain objects and viceversa
 */
public class JSONAdapter {

	// Competition properties
	public final static String ID = "id";
	public final static String NAME = "name";
	public final static String SCHEDULED_DATE_TIME = "date_time";
	public final static String REAL_DATE_TIME = "real_date_time";
	public final static String TIME = "time";
	public final static String LOCATION = "location";
	public final static String STATE = "state";
	public final static String HAS_CIRCUIT = "has_circuit";
	public final static String CATEGORIES = "categories";
	public final static String EVENT = "event";
	public final static String DISTANCE = "distance";
	public final static String IDATHLETE = "idAth";
	public final static String PARTICIPATIONS = "part";
	public final static String NUMBER = "num";

	// Athlete properties
	public final static String FIRST_NAME = "first_name";
	public final static String LAST_NAME = "last_name";
	public final static String IDCATEGORY = "idcategory";
	public final static String ABSOLUTE_POSITION = "absolute_position";
	public final static String CATEGORY_POSITION = "category_position";
	public final static String DISTANCE_FROM_START = "dist_start";
	public final static String HAS_ARRIVED = "has_arrived";
	public final static String USERNAME = "user";

	// Comment properties
	public final static String IDCOMMENT = "idc";
	public final static String TEXT = "tx";
	public final static String REFERENCES = "rf";
	public final static String WRITER = "wt";
	public final static String DATE = "dt";
	public final static String LATE6 = "lat";
	public final static String LONE6 = "lon";

	// Date formats
	public static final String SCHEDULED_DATE_FORMAT = "yyyy-MM-dd, HH:mm";
	public static final String REAL_DATE_FORMAT = "yyyy-MM-dd, HH:mm:ss";

	public static JSONArray competitionListToJSON(List<Competition> competitions)
			throws JSONException {
		JSONArray jsonArray = new JSONArray();
		for (Competition comp : competitions) {
			jsonArray.put(competitionToJSON(comp));
		}
		return jsonArray;
	}

	public static JSONObject competitionToJSON(Competition comp)
			throws JSONException {
		JSONObject jsonObj = new JSONObject();
		jsonObj.put(ID, comp.getId());
		jsonObj.put(NAME, comp.getName());
		jsonObj.put(SCHEDULED_DATE_TIME, comp.gtScheduledDateAndTime());
		jsonObj.put(REAL_DATE_TIME, comp.gtRealDateAndTime());
		jsonObj.put(LOCATION, comp.getLocation());
		jsonObj.put(STATE, comp.getState());
		jsonObj.put(HAS_CIRCUIT, comp.isHasCircuitMap());
		jsonObj.put(CATEGORIES, comp.getIdCategories());
		jsonObj.put(EVENT, comp.getEventName());
		jsonObj.put(DISTANCE, comp.getDistance());
		return jsonObj;
	}

	public static Competition JSONToCompetition(String jsonStr)
			throws JSONException {
		JSONObject jsonObj = new JSONObject(jsonStr);

		// Categories
		JSONArray idCategories = jsonObj.getJSONArray(CATEGORIES);
		int[] idCategoriesArr = new int[idCategories.length()];
		for (int i = 0; i < idCategories.length(); i++) {
			idCategoriesArr[i] = idCategories.getInt(i);
		}

		// Dates
		Date scheduledDate = null;
		Date realDate = null;
		try {
			scheduledDate = new SimpleDateFormat(SCHEDULED_DATE_FORMAT)
					.parse(jsonObj.getString(SCHEDULED_DATE_TIME));
			realDate = new SimpleDateFormat(REAL_DATE_FORMAT).parse(jsonObj
					.getString(REAL_DATE_TIME));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		// Competition
		return new Competition(jsonObj.getInt(ID), jsonObj.getString(NAME),
				scheduledDate, realDate, jsonObj.getString(LOCATION), jsonObj
						.getInt(STATE), jsonObj.getBoolean(HAS_CIRCUIT),
				idCategoriesArr, jsonObj.getString(EVENT), jsonObj
						.getInt(DISTANCE));
	}

	public static List<Competition> JSONToCompetitionList(String jsonStr)
			throws JSONException {
		List<Competition> competitions = new ArrayList<Competition>();
		JSONArray jsonArray = new JSONArray(jsonStr);
		for (int i = 0; i < jsonArray.length(); i++) {
			competitions.add(JSONToCompetition(jsonArray.getJSONObject(i)
					.toString()));
		}
		return competitions;
	}

	public static JSONObject athleteToJSON(Athlete athlete)
			throws JSONException {
		JSONObject jsonObj = new JSONObject();
		jsonObj.put(ID, athlete.getId());
		jsonObj.put(FIRST_NAME, athlete.getFirstName());
		jsonObj.put(LAST_NAME, athlete.getLastName());
		jsonObj.put(IDCATEGORY, athlete.getIdCategory());
		jsonObj.put(ABSOLUTE_POSITION, athlete.getAbsolutePosition());
		jsonObj.put(CATEGORY_POSITION, athlete.getCategoryPosition());
		jsonObj.put(DISTANCE_FROM_START, athlete.getDistanceFromStart());
		jsonObj.put(TIME, athlete.getTime());
		jsonObj.put(HAS_ARRIVED, athlete.isHasArrived());
		jsonObj.put(NUMBER, athlete.getNumber());
		jsonObj.put(USERNAME, athlete.getUserName());
		return jsonObj;
	}

	public static JSONArray athleteListToJSON(List<Athlete> athletes)
			throws JSONException {
		JSONArray jsonArray = new JSONArray();
		for (Athlete athlete : athletes) {
			jsonArray.put(athleteToJSON(athlete));
		}
		return jsonArray;
	}

	public static Athlete JSONToAthlete(String jsonStr, Competition competition)
			throws JSONException {
		JSONObject jsonObj = new JSONObject(jsonStr);
		if (competition == null) {
			return new Athlete(jsonObj.getInt(ID), jsonObj
					.getString(FIRST_NAME), jsonObj.getString(LAST_NAME),
					jsonObj.getInt(IDCATEGORY), 0, 0, 0, 0, false, 0, jsonObj.getString(USERNAME));
		}
		boolean hasArrived = competition.getDistance() == jsonObj
				.getInt(DISTANCE_FROM_START);
		return new Athlete(jsonObj.getInt(ID), jsonObj.getString(FIRST_NAME),
				jsonObj.getString(LAST_NAME), jsonObj.getInt(IDCATEGORY),
				jsonObj.getInt(ABSOLUTE_POSITION), jsonObj
						.getInt(CATEGORY_POSITION), jsonObj
						.getInt(DISTANCE_FROM_START), jsonObj.getInt(TIME),
				hasArrived, jsonObj.getInt(NUMBER), jsonObj.getString(USERNAME));
	}

	public static List<Athlete> JSONToAthleteList(String jsonStr,
			Competition competition) throws JSONException {
		List<Athlete> athletes = new ArrayList<Athlete>();
		JSONArray jsonArray = new JSONArray(jsonStr);
		for (int i = 0; i < jsonArray.length(); i++) {
			athletes.add(JSONToAthlete(jsonArray.getJSONObject(i).toString(),
					competition));
		}
		return athletes;
	}

	public static JSONObject participationsToJSON(List<Athlete> athletes,
			Competition competition) throws JSONException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(STATE, competition.getState());
		JSONArray jsonArr = new JSONArray();
		for (Athlete ath : athletes) {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put(IDATHLETE, ath.getId());
			jsonObj.put(DISTANCE_FROM_START, ath.getDistanceFromStart());
			jsonObj.put(ABSOLUTE_POSITION, ath.getAbsolutePosition());
			jsonObj.put(CATEGORY_POSITION, ath.getCategoryPosition());
			jsonObj.put(TIME, ath.getTime());
			jsonArr.put(jsonObj);
		}
		jsonObject.put(PARTICIPATIONS, jsonArr);
		return jsonObject;
	}

	public static void JSONToParticipations(String jsonStr,
			List<Athlete> athletes, Competition competition)
			throws JSONException {
		// Parse the JSON
		JSONObject jsonObject = new JSONObject(jsonStr);

		// State
		competition.setState(jsonObject.getInt(STATE));

		// Participations: we take the order of the JSON, so it's simpler
		// to add them to a new array and then copy the athletes
		JSONArray jsonArray = jsonObject.getJSONArray(PARTICIPATIONS);
		Athlete[] mAthletes = new Athlete[jsonArray.length()];
		for (int i = 0; i < jsonArray.length(); i++) {
			for (int j = 0; i < athletes.size(); j++) {
				Athlete ath = athletes.get(j);
				JSONObject jsonObj = jsonArray.getJSONObject(i);
				if (jsonObj.getInt(IDATHLETE) == ath.getId()) {
					mAthletes[i] = athletes.get(j);
					ath.setDistanceFromStart(jsonObj
							.getInt(DISTANCE_FROM_START));
					ath.setAbsolutePosition(jsonObj.getInt(ABSOLUTE_POSITION));
					ath.setCategoryPosition(jsonObj.getInt(CATEGORY_POSITION));
					ath.setTime(jsonObj.getLong(TIME));
					if (competition != null
							&& competition.getDistance() == ath
									.getDistanceFromStart()) {
						ath.setHasArrived(true);
					}
					break;
				}
			}
		}
		athletes.clear();
		for (Athlete ath : mAthletes) {
			athletes.add(ath);
		}
	}

	public static JSONArray circuitNamesListToJSON(String[] circuitNames)
			throws JSONException {
		JSONArray jsonArr = new JSONArray();
		for (String cirName : circuitNames) {
			jsonArr.put(cirName);
		}
		return jsonArr;
	}

	public static String[] JSONToCircuitNames(String jsonStr)
			throws JSONException {
		JSONArray jsonArray = new JSONArray(jsonStr);
		String[] circuitNames = new String[jsonArray.length()];
		for (int i = 0; i < jsonArray.length(); i++) {
			circuitNames[i] = (String) jsonArray.get(i);
		}
		return circuitNames;
	}

	public static JSONObject commentToJSON(Comment c)
			throws JSONException {
		JSONObject jsonObj = new JSONObject();
		jsonObj.put(IDCOMMENT, c.getId());
		jsonObj.put(TEXT, c.getText());
		jsonObj.put(WRITER, c.getWriter());
		jsonObj.put(ID, c.getIdCompetition());
		jsonObj.put(LATE6, c.getLatE6());
		jsonObj.put(LONE6, c.getLonE6());
		jsonObj.put(DATE, c.getDate().getTime());
		JSONArray jsonArr = new JSONArray();
		for(String ref: c.getUserNames()) {
			jsonArr.put(ref);
		}
		jsonObj.put(REFERENCES, jsonArr);
		return jsonObj;
	}

	public static JSONArray commentListToJSON(List<Comment> comments)
			throws JSONException {
		JSONArray jsonArray = new JSONArray();
		for (Comment c : comments) {
			jsonArray.put(commentToJSON(c));
		}
		return jsonArray;
	}

	public static Comment JSONToComment(String jsonStr)
			throws JSONException {
		JSONObject jsonObj = new JSONObject(jsonStr);
	
		// Get the references
		List<String> refs = new ArrayList<String>();
		JSONArray jsonArr = jsonObj.getJSONArray(REFERENCES);
		for(int i = 0; i < jsonArr.length(); i++) {
			refs.add((String) jsonArr.getString(i));
		}
		Date date = new Date(jsonObj.getLong(DATE));
		return new Comment(jsonObj.getInt(IDCOMMENT), jsonObj.getString(TEXT), 
				jsonObj.getString(WRITER), refs, jsonObj.getInt(ID), 
				jsonObj.getInt(LATE6), jsonObj.getInt(LONE6), date);
	}

	public static List<Comment> JSONToCommentList(String jsonStr) 
	throws JSONException {
		List<Comment> comments = new ArrayList<Comment>();
		JSONArray jsonArray = new JSONArray(jsonStr);
		for (int i = 0; i < jsonArray.length(); i++) {
			comments.add(JSONToComment(jsonArray.getJSONObject(i).toString()));
		}
		return comments;
	}

}
