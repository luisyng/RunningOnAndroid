package running.server.simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import running.domain.Athlete;
import running.domain.Competition;
import running.server.db.HsqldbInterface;

/**
 * Simulates a competition
 */
public class Simulator {

    private List<SimulatedAthlete> simAths;
    private Competition competition;

    public Simulator(List<Athlete> athletes, Competition competition) {
        this.simAths = new ArrayList<SimulatedAthlete>();
        for (Athlete ath : athletes) {
            if(ath.getId() > 6) {
                this.simAths.add(new SimulatedAthlete(ath.getId(), competition.getDistance()));
            }
        }
        this.competition = competition;
    }

    /**
     * Starts the simulation
     */
    public void run() {
        while (competition.getState() == Competition.TAKING_PLACE
                && HsqldbInterface.getIsSimulating(competition.getId())) {
            for (SimulatedAthlete simAth : simAths) {
                if (!simAth.hasArrived()) {
                    HsqldbInterface.saveDistanceFromStart(simAth.getId(), competition.getId(), simAth.nextStep());
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Simulator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Creates a Simulator and starts it in a new thread
     */
    public static void simulate(final int idCompetition) {
        new Thread(new Runnable() {
            public void run() {
                Competition comp = HsqldbInterface.getCompetition(idCompetition);
                List<Athlete> aths = HsqldbInterface.getCompetitionAthletes(idCompetition, false);
                //aths.remove(0);
                //aths.remove(1); // Delete Victor
                new Simulator(aths, comp).run();
            }
        }).start();
    }
}
