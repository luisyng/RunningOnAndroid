package running.android.app;

import java.util.List;
import java.util.Map;

import running.domain.Athlete;
import running.domain.Competition;

import android.graphics.Canvas;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.Log;

/**
 * Overlay that draws the athletes over the map
 */
public class AthletesOverlay extends Overlay {

	// Domain
	private Competition competition;
	private List<GeoPoint> circuitPoints;
	private Map<Integer, Bitmap> faces;

	// Application
	private WebService webService;

	// Finals
	private final int ATHLETE_POINT_RADIUS = 5;
	private final int PHOTO_SIZE_X = 50;
	private final int PHOTO_SIZE_Y = 50;

	/**
	 * Constructor
	 * 
	 * @param webService
	 */
	public AthletesOverlay(WebService webService) {
		this.competition = webService.getCompetition();
		this.circuitPoints = webService.getCircuit().getCircuitPoints();
		this.webService = webService;
		this.faces = webService.getFaces();
	}

	/**
	 * Method that android calls to draw the overlay
	 */
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		Log.i("OVERLAY", "Draw the athlete points");
		if (shadow == false) {
			Projection projection = mapView.getProjection();
			// Only paints the athletes if the competition is taking place
			if (competition.getState() == Competition.TAKING_PLACE) {
				webService.getAthletesMutex().startToReadOrModify();
				for (Athlete ath : webService.getAthletes()) {
					// Geopoint
					GeoPoint currentGeoPoint = circuitPoints.get(ath
							.getDistanceFromStart());

					// Projection to pixel
					Point currentPoint = new Point();
					projection.toPixels(currentGeoPoint, currentPoint);

					// Paint it
					Paint paint = new Paint();

					// Paint the face
					if (ath.isShowFace()) {
						Bitmap face = faces.get(ath.getId());
						if (face == null) {
							face = this.faces.get(0);
						}
						canvas.drawBitmap(face, currentPoint.x
								- (PHOTO_SIZE_X / 2), currentPoint.y
								- (PHOTO_SIZE_Y / 2), paint);
					}

					// Paint the point
					else {
						paint.setColor(ath.getColor());
						RectF oval = new RectF(currentPoint.x
								- ATHLETE_POINT_RADIUS, currentPoint.y
								- ATHLETE_POINT_RADIUS, currentPoint.x
								+ ATHLETE_POINT_RADIUS, currentPoint.y
								+ ATHLETE_POINT_RADIUS);
						canvas.drawOval(oval, paint);
					}
				}
				webService.getAthletesMutex().endReadingOrModifying();
			}
		}
		super.draw(canvas, mapView, shadow);
	}
}