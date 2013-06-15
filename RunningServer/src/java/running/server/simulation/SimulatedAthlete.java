package running.server.simulation;

/**
 * Athlete simulated: has a random speed and a random deviation each step
 */
public class SimulatedAthlete {
    private final int CIRCUIT_LENGTH;
    private int id;
    private double distanceFromStart;
    private double speed; // m/s

    public SimulatedAthlete(int id, int circuitLength) {
        this.id = id;
        this.CIRCUIT_LENGTH = circuitLength;
        this.distanceFromStart = 0;
        this.speed = 2.5 + 2 * Math.random(); // = 12.6 km/h < v < 19.8 km/h
    }

    public int nextStep() {
        double nextStep = distanceFromStart + speed - 1 + 2 * Math.random();
        if (nextStep >= CIRCUIT_LENGTH) {
            distanceFromStart = CIRCUIT_LENGTH;
        }
        else {
            
            distanceFromStart = nextStep;
        }
        return (int) Math.round(distanceFromStart);
    }

    public boolean hasArrived() {
        return Math.round(distanceFromStart) == CIRCUIT_LENGTH;
    }

    public int getId() {
        return this.id;
    }
}
