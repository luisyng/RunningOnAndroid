package running.android.app;

import java.util.ArrayList;
import java.util.List;

import running.domain.Comment;

import android.graphics.drawable.Drawable;

import com.google.android.maps.MapView;

public class MyItemizedOverlay extends
		BalloonItemizedOverlay<CommentOverlayItem> {
	
	private ArrayList<CommentOverlayItem> m_overlays = new ArrayList<CommentOverlayItem>();
	private byte[] bytes = new byte[100];

	public MyItemizedOverlay(Drawable defaultMarker, MapView mapView) {
		super(boundCenter(defaultMarker), mapView);
	}

	public void addOverlay(CommentOverlayItem overlay) {
		m_overlays.add(overlay);
		populate();
	}
	
	public void addComments(List<Comment> comments) {
		for(Comment c: comments) {
			addOverlay(new CommentOverlayItem(bytes, c));
		}
	}

	@Override
	protected CommentOverlayItem createItem(int i) {
		return m_overlays.get(i);
	}

	@Override
	public int size() {
		return m_overlays.size();
	}

	@Override
	protected boolean onBalloonTap(int index) {
		return true;
	}

}
