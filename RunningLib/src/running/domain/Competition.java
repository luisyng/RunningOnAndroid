package running.domain;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Competition
 */
public class Competition implements Serializable {
	private int id;
	private String name;
	private Date scheduledDate;
	private Date realDate;
	private String location;
	private int state;
	private boolean hasCircuitMap;
	private int[] idCategories;

	private String eventName;
	private int distance;
	
	public static final int NOT_STARTED = 0;
	public static final int TAKING_PLACE = 1;
	public static final int ENDED = 2;
	
	private static final String SCHEDULED_DATE_FORMAT = "yyyy-MM-dd, HH:mm";
	private static final String REAL_DATE_FORMAT = "yyyy-MM-dd, HH:mm:ss";
	private static final String DATE_DATABASE = "yyyy-MM-dd";
	private static final String DATE_SHOW = "dd-MM-yyyy";			
	
	public Competition(int id, String name, Date scheduledDate, Date realDate, String location, 
			int state, boolean hasCircuitMap, int[] idCategories, 
			String eventName, int distance) {
		this.id = id;
		this.name = name;
		this.scheduledDate = scheduledDate;
		this.realDate = realDate;
		this.location = location;
		this.state = state;
		this.hasCircuitMap = hasCircuitMap;
		this.idCategories = idCategories;
		this.eventName = eventName;
		this.distance = distance;
	}

	public String getEventName() {
		return eventName;
	}
	
	public void setEventName(String eventName) {
		this.eventName = eventName;
	}
	
	public int getDistance() {
		return distance;
	}
	
	public void setDistance(int distance) {
		this.distance = distance;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	//This two methods are not real getters for not to be serialized
	public String gtScheduledDateStrToShow() {
		return new SimpleDateFormat(DATE_SHOW).format(this.scheduledDate);
	}
	
	public String gtScheduledDateStrToDB() {
		return new SimpleDateFormat(DATE_DATABASE).format(this.scheduledDate);
	}
	
	public String gtScheduledDateAndTime() {
		return new SimpleDateFormat(SCHEDULED_DATE_FORMAT).format(this.scheduledDate);
	}
	
	public String gtRealDateAndTime() {
		return new SimpleDateFormat(REAL_DATE_FORMAT).format(this.realDate);
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
	
	public int getState() {
		return state;
	}
	
	public String getStateStr() {
		if(this.state == Competition.NOT_STARTED) {
			return "sin empezar";
		} else if(this.state == Competition.TAKING_PLACE) {
			return "en transcurso";
		}
		return "acabada";
		
	}
	
	public void setState(int state) {
		this.state = state;
	}
	
	public boolean isHasCircuitMap() {
		return hasCircuitMap;
	}
	public void setHasCircuitMap(boolean hasCircuitMap) {
		this.hasCircuitMap = hasCircuitMap;
	}
	
	public int[] getIdCategories() {
		return idCategories;
	}
	
	public void setIdCategories(int[] idCategories) {
		this.idCategories = idCategories;
	}
	
	public Date getScheduledDate() {
		return scheduledDate;
	}

	public void setScheduledDate(Date scheduledDate) {
		this.scheduledDate = scheduledDate;
	}

	public Date getRealDate() {
		return realDate;
	}

	public void setRealDate(Date realDate) {
		this.realDate = realDate;
	}
}
