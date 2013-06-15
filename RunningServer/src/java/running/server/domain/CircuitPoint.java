package running.server.domain;

public class CircuitPoint {
	private int latitudeE6;
	private int longitudeE6;

	public CircuitPoint(int latitudeE6, int longitudeE6) {
		super();
		this.latitudeE6 = latitudeE6;
		this.longitudeE6 = longitudeE6;
	}

	public int getLatitudeE6() {
		return latitudeE6;
	}

	public void setLatitudeE6(int latitudeE6) {
		this.latitudeE6 = latitudeE6;
	}

	public int getLongitudeE6() {
		return longitudeE6;
	}

	public void setLongitudeE6(int longitudeE6) {
		this.longitudeE6 = longitudeE6;
	}
}
