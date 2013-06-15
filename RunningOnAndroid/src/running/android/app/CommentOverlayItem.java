package running.android.app;

import java.text.SimpleDateFormat;

import running.domain.Comment;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class CommentOverlayItem extends OverlayItem {

	private byte[] photo;
	
	public CommentOverlayItem(byte[] photo, Comment comment) {
		
		super(new GeoPoint(comment.getLatE6(), comment.getLonE6()), 
				"@" + comment.getWriter() + ", " 
				+ new SimpleDateFormat("dd-MM-yyyy, HH:mm").format(comment.getDate()), 
				comment.getText());
		this.photo = photo;
	}
	
	public byte[] getPhoto(){
		return photo;
	}
	
	public GeoPoint getPoint(){
		return super.getPoint();
	}
	
	public String getTitle(){
		return super.getTitle();
	}
	
	public String getSnippet(){
		return super.getSnippet();
	}
}
