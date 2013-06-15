/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package running.server.simulation;

/**
 *
 * @author luis
 */
public class DistanceFromStart {
    private int idAthlete;
    private int idCompetition;
    private int distance;
    private long time;

    public DistanceFromStart(int idAthlete, int idCompetition, int distance, long time) {
        this.idAthlete = idAthlete;
        this.idCompetition = idCompetition;
        this.distance = distance;
        this.time = time;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getIdAthlete() {
        return idAthlete;
    }

    public void setIdAthlete(int idAthlete) {
        this.idAthlete = idAthlete;
    }

    public int getIdCompetition() {
        return idCompetition;
    }

    public void setIdCompetition(int idCompetition) {
        this.idCompetition = idCompetition;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
