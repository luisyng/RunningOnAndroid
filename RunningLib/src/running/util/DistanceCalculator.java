package running.util;

public class DistanceCalculator {
	 public static final double EARTH_RADIUS = 6371000;
	
	 /**
	  * Calculates the distance between two geopoints
	  * @param latE61 latitude of point1 * 1000000
	  * @param lonE61 longitude of point1 * 1000000
	  * @param latE62 latitude of point2 * 1000000
	  * @param lonE62 longitude of point2 * 1000000
	  * @return distance in metres
	  */
	 public static double calculateDistance(int latE61, int lonE61, int latE62, int lonE62) {
        double lat1 = latE61 / 1E6;
        double lat2 = latE62 / 1E6;
        double lon1 = lonE61 / 1E6;
        double lon2 = lonE62 / 1E6;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c;
    }
}
