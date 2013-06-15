package running.android.domain;

import java.util.List;

import running.util.DistanceCalculator;

import com.google.android.maps.GeoPoint;

public class Circuit {
	
	private final int CHECK_BEFORE = 15;
    private final int CHECK_AFTER = 23;
    // Geopoints from whom the circuit was created
    private List<GeoPoint> originalCircuitPoints;
    // Geopoints separated 1m
	private List<GeoPoint> circuitPoints;
	private GeoPoint center;
    private int latSpanE6;
    private int lonSpanE6;
	
	public Circuit(List<GeoPoint> originalCircuitPoints, List<GeoPoint> geoPoints,
			GeoPoint center, int latSpanE6, int lonSpanE6) {
		super();
		this.originalCircuitPoints = originalCircuitPoints;
		this.circuitPoints = geoPoints;
		this.center = center;
		this.latSpanE6 = latSpanE6;
		this.lonSpanE6 = lonSpanE6;
	}

	public List<GeoPoint> getOriginalCircuitPoints() {
		return originalCircuitPoints;
	}

	public void setOriginalCircuitPoints(List<GeoPoint> originalCircuitPoints) {
		this.originalCircuitPoints = originalCircuitPoints;
	}

	public List<GeoPoint> getCircuitPoints() {
		return circuitPoints;
	}

	public void setCircuitPoints(List<GeoPoint> geoPoints) {
		this.circuitPoints = geoPoints;
	}
	
	public GeoPoint getCircuitPoint (int distanceFromStart) {
		return this.circuitPoints.get(distanceFromStart);
	}
	
	public GeoPoint getCenter() {
		return center;
	}

	public void setCenter(GeoPoint center) {
		this.center = center;
	}

	public int getLatSpanE6() {
		return latSpanE6;
	}

	public void setLatSpanE6(int latSpanE6) {
		this.latSpanE6 = latSpanE6;
	}

	public int getLonSpanE6() {
		return lonSpanE6;
	}

	public void setLonSpanE6(int lonSpanE6) {
		this.lonSpanE6 = lonSpanE6;
	}

	/**
	 * Gets the most probable point of the circuit
	 * given the previous one and his current position
	 */
	public GeoPoint getNearestCircuitPoint(int lastDistanceFromStart, GeoPoint currentPoint) {
		return getCircuitPoint(getNearestDistanceFromStart(lastDistanceFromStart, currentPoint));
	}
	
	/**
	 * Gets the most probable point of the circuit (as distance from start)
	 * given the previous one and his current position
	 */
	public int getNearestDistanceFromStart (int lastDistanceFromStart, GeoPoint currentPoint){
        int start = Math.max(0, (lastDistanceFromStart - CHECK_BEFORE));
        int end = Math.min(circuitPoints.size(), (lastDistanceFromStart + CHECK_AFTER));
        double distance = calculateDistance(currentPoint, circuitPoints.get(start));
        int nearestDis = start;
        for (int i = start+1; i < end; i++){
            double newDistance = calculateDistance(currentPoint, circuitPoints.get(i));
            if (newDistance < distance){
                distance = newDistance;
                nearestDis = i;
            }
        }
        return nearestDis;
    }
	
	private double calculateDistance(GeoPoint p1, GeoPoint p2) {
		 return DistanceCalculator.calculateDistance(p1.getLatitudeE6(), p1.getLongitudeE6(),
                 p2.getLatitudeE6(), p2.getLongitudeE6());
	}
	
	/**
	 * Gets the length of the circuit in metres
	 */
	public int getLength() {
		return circuitPoints.size() - 1;
	}
}
