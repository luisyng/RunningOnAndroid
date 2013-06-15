package running.domain;

import java.util.Date;
import java.util.List;

public class Comment {
	private int id;
	private String text;
	private String writer;
	List<String> userNames;
	private int idCompetition;
	private int latE6;
	private int lonE6;
	private Date date;
	
	public Comment(int id, String text, String writer, List<String> userNames, 
			int idCompetition, int latE6, int lonE6, Date date) {
		super();
		this.id = id;
		this.text = text;
		this.writer = writer;
		this.userNames = userNames;
		this.idCompetition = idCompetition;
		this.latE6 = latE6;
		this.lonE6 = lonE6;
		this.date = date;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}

	public String getWriter() {
		return writer;
	}

	public void setWriter(String writer) {
		this.writer = writer;
	}

	public List<String> getUserNames() {
		return userNames;
	}

	public void setUserNames(List<String> userNames) {
		this.userNames = userNames;
	}

	public int getIdCompetition() {
		return idCompetition;
	}

	public void setIdCompetition(int idCompetition) {
		this.idCompetition = idCompetition;
	}

	public int getLatE6() {
		return latE6;
	}

	public void setLatE6(int latE6) {
		this.latE6 = latE6;
	}

	public int getLonE6() {
		return lonE6;
	}

	public void setLonE6(int lonE6) {
		this.lonE6 = lonE6;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
}
