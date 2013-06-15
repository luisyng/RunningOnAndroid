package running.android.app;

import java.util.List;

import running.android.app.R;

import android.content.res.Resources;
import android.graphics.Canvas;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;

public class CircuitOverlay extends Overlay {

	// Domain
	private List<GeoPoint> originalCircuitPoints;
	
	// Application
	private Paint circuitPaint;
	private Bitmap startIcon;
	private Bitmap finishIcon;

	// Final
	private final int CIRCUIT_LINE_RADIUS = 2;

	public CircuitOverlay(List<GeoPoint> circuitPoints, Resources resources) {
		this.originalCircuitPoints = circuitPoints;
		this.circuitPaint = new Paint();
		this.startIcon = BitmapFactory.decodeResource(resources, R.drawable.start_icon);
		this.finishIcon = BitmapFactory.decodeResource(resources, R.drawable.finish_icon);
		circuitPaint.setColor(Color.BLUE);
		circuitPaint.setStrokeWidth(CIRCUIT_LINE_RADIUS);
	}

	/**
	 * Method that android calls to draw the overlay
	 */
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		Log.i("OVERLAY", "Draw the circuit");
		if (shadow == false) {
			Projection projection = mapView.getProjection();

			// Circuit points
			Point p1;
			Point p2 = new Point();
			projection.toPixels(originalCircuitPoints.get(0), p2);
			for(int i = 0; i < originalCircuitPoints.size() - 1; i++) {
				p1 = p2;
				p2 = new Point();
				projection.toPixels(originalCircuitPoints.get(i), p2);
				canvas.drawLine(p1.x, p1.y, p2.x, p2.y, circuitPaint);			
			}
			
			// Drawings
			Point start = new Point();
			projection.toPixels(originalCircuitPoints.get(0),start);
			Point finish = new Point();
			projection.toPixels(originalCircuitPoints.get(originalCircuitPoints.size()-1),finish);
			canvas.drawBitmap(startIcon, start.x, start.y, circuitPaint);
			canvas.drawBitmap(finishIcon, finish.x, finish.y, circuitPaint);
		}
		super.draw(canvas, mapView, shadow);
	}
}