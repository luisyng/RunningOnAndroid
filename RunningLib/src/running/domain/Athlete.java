package running.domain;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Athlete
 */
public class Athlete implements Cloneable {
	private int id;
	private String firstName;
	private String lastName;
	private int idCategory;
	private boolean following;
	private boolean showFace;
	private int color;
	private int absolutePosition;
	private int categoryPosition;
	private int distanceFromStart;
	private long time;
	private boolean hasArrived;
	private int number;
	private static final int DEFAULT_COLOR = -18944;
	private String userName;

	public Athlete(int id, String firstName, String lastName, int idCategory,
			int absolutePosition, int categoryPosition, int distanceFromStart,
			long time, boolean hasArrived, int number, String userName) {
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.idCategory = idCategory;
		this.absolutePosition = absolutePosition;
		this.categoryPosition = categoryPosition;
		this.distanceFromStart = distanceFromStart;
		this.time = time;
		this.following = false;
		this.showFace = false;
		this.hasArrived = hasArrived;
		this.color = DEFAULT_COLOR;
		this.number = number;
		this.userName = userName;
	}

	public Athlete() {
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public int getIdCategory() {
		return idCategory;
	}

	public void setIdCategory(int idCategory) {
		this.idCategory = idCategory;
	}

	public boolean isFollowing() {
		return following;
	}

	public boolean isShowFace() {
		return showFace;
	}

	public void setShowFace(boolean showFace) {
		this.showFace = showFace;
	}

	public void setFollowing(boolean following) {
		this.following = following;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public void setDefaultColor() {
		this.color = DEFAULT_COLOR;
	}

	public int getAbsolutePosition() {
		return absolutePosition;
	}

	public void setAbsolutePosition(int absolutePosition) {
		this.absolutePosition = absolutePosition;
	}

	public int getCategoryPosition() {
		return categoryPosition;
	}

	public void setCategoryPosition(int categoryPosition) {
		this.categoryPosition = categoryPosition;
	}

	public Category gtCategory() {
		return Category.getCategory(idCategory);
	}

	public int getDistanceFromStart() {
		return distanceFromStart;
	}

	public void setDistanceFromStart(int distanceFromStart) {
		this.distanceFromStart = distanceFromStart;
	}

	public long getTime() {
		return time;
	}

	public String getTimeStr() {
		return new SimpleDateFormat("m:ss").format(new Date(time));
	}

	public String getSpeedKMH() {
		double speed = 3.6 * (double) distanceFromStart / (time / 1000.0);
		NumberFormat nf = new DecimalFormat("#.#");
		return nf.format(speed) + " km/h";
	}

	public String getTimeToFirst(int distanceFirst, long milisFromEnd) {
		double speed = (double) distanceFromStart / (time / 1000.0);
		long timeToFirst = (long) (1000.0 * (double) (distanceFirst - this.distanceFromStart) / speed);
		timeToFirst += milisFromEnd;
		return new SimpleDateFormat("m:ss").format(new Date(timeToFirst));
	}

	public void setTime(long time) {
		this.time = time;
	}

	public boolean isHasArrived() {
		return hasArrived;
	}

	public void setHasArrived(boolean hasArrived) {
		this.hasArrived = hasArrived;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Athlete clone() throws CloneNotSupportedException {
		return (Athlete) super.clone();
	}
}
