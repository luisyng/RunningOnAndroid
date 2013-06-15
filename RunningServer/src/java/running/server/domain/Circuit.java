package running.server.domain;

import java.util.List;

public class Circuit {
    private List<CircuitPoint> circuitPoints;
    private CircuitPoint center;
    private CircuitPoint edge;

    public Circuit(List<CircuitPoint> circuitPoints, CircuitPoint center, CircuitPoint edge) {
        this.circuitPoints = circuitPoints;
        this.center = center;
        this.edge = edge;
    }

    public CircuitPoint getCenter() {
        return center;
    }

    public void setCenter(CircuitPoint center) {
        this.center = center;
    }

    public List<CircuitPoint> getCircuitPoints() {
        return circuitPoints;
    }

    public void setCircuitPoints(List<CircuitPoint> circuitPoints) {
        this.circuitPoints = circuitPoints;
    }

    public CircuitPoint getEdge() {
        return edge;
    }

    public void setEdge(CircuitPoint edge) {
        this.edge = edge;
    }

    public int getLength() {
        return circuitPoints.size() - 1;
    }
}
