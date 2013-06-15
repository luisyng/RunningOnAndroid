package running.server.util;

import java.util.ArrayList;
import java.util.List;
import running.server.domain.Circuit;
import running.server.domain.CircuitPoint;
import running.util.DistanceCalculator;

public class CircuitManager {

    /**
     * Creates a circuit from its original circuit points
     * @param originalCircuitPoints
     */
    public static Circuit createCircuit(List<CircuitPoint> points) {
        // List for the circuit points
        List<CircuitPoint> cPoints = new ArrayList<CircuitPoint>();
        CircuitPoint p1 = points.get(0);
        CircuitPoint p2;

        // Min and max to determine the center
        int minLatE6 = p1.getLatitudeE6();
        int maxLatE6 = p1.getLatitudeE6();
        int minLonE6 = p1.getLongitudeE6();
        int maxLonE6 = p1.getLongitudeE6();

        // Save the first point
        cPoints.add(p1);
        for (int i = 0; i < points.size() - 1; i++) {
            // Get the next point
            p2 = points.get(i + 1);

            // Compare and update the max and min
            if (p2.getLatitudeE6() < minLatE6) {
                minLatE6 = p2.getLatitudeE6();
            } else if (p2.getLatitudeE6() > maxLatE6) {
                maxLatE6 = p2.getLatitudeE6();
            }
            if (p2.getLongitudeE6() < minLonE6) {
                minLonE6 = p2.getLongitudeE6();
            } else if (p2.getLongitudeE6() > maxLonE6) {
                maxLonE6 = p2.getLongitudeE6();
            }

            // Calculate the distance between p1 and p2
            double distance = calculateDistance(p1, p2);

            // Increase of latitude and longitude for distance = 1 meter
            double dLat = (p2.getLatitudeE6() - p1.getLatitudeE6()) / distance;
            double dLon = (p2.getLongitudeE6() - p1.getLongitudeE6()) / distance;

            // Add the points to the circuit list
            for (int j = 1; j <= distance; j++) {
                cPoints.add(new CircuitPoint((int) (p1.getLatitudeE6() + j * dLat), (int) (p1.getLongitudeE6() + j * dLon)));
            }
            p1 = cPoints.get(cPoints.size() - 1);
        }
        int centerLatE6 = (int) Math.round((double) (maxLatE6 + minLatE6) / 2.0);
        int centerLonE6 = (int) Math.round((double) (maxLonE6 + minLonE6) / 2.0);
        return new Circuit(cPoints, new CircuitPoint(centerLatE6, centerLonE6),
                new CircuitPoint(maxLatE6, maxLonE6));
    }

    private static double calculateDistance(CircuitPoint p1, CircuitPoint p2) {
        return DistanceCalculator.calculateDistance(p1.getLatitudeE6(), p1.getLongitudeE6(),
                p2.getLatitudeE6(), p2.getLongitudeE6());
    }
}
