/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package running.server.simulation;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import running.domain.Comment;
import running.domain.Competition;
import running.server.db.HsqldbInterface;

/**
 *
 * @author luis
 */
public class RacePlayback {

    public static void reset(int idCompetition) {

        // Shutdown
        try {
            HsqldbInterface.shutdown();
        } catch(Exception e) {}

        // Load last script
        loadLastScript();
        
        // Distances to zero
        //HsqldbInterface.distancesToZero(idCompetition);
        // Not started
        //HsqldbInterface.setCompetitionHasNotStarted(idCompetition);
    }

    private static void loadLastScript() {

        // Copy file
        try {
            FileInputStream in = new FileInputStream("/home/isfteleco/last.script");
            FileOutputStream out = new FileOutputStream("/home/isfteleco/db/running.script");
            byte buffer[] = new byte[16];
            int n;
            while ((n = in.read(buffer)) > -1) {
                out.write(buffer, 0, n);  
            }
            out.close();
            in.close();
        } catch (IOException ex) {
            Logger.getLogger(RacePlayback.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void playback(int idCompetition) {
        // Get distances from start

        // Get the distances
        final List<DistanceFromStart> distances =
                HsqldbInterface.getDistancesFromStart(idCompetition);
        // Remove the distances for not to duplicate them
        HsqldbInterface.removeDistancesFromStart(idCompetition);

        // Set has started
        HsqldbInterface.setCompetitionHasStarted(idCompetition);

        // DISTANCES
        new Thread() {

            public void run() {
                long startTime = new Date().getTime();
                for (DistanceFromStart d : distances) {
                    // Time from start of the race
                    long timeFromStart = new Date().getTime() - startTime;

                    // If it's not its time, sleeps
                    long sleepTime = d.getTime() - timeFromStart;
                    if (sleepTime > 0) {
                        try {
                            Thread.sleep(sleepTime);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(RacePlayback.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    // Save distance from start
                    HsqldbInterface.saveDistanceFromStart(d.getIdAthlete(), d.getIdCompetition(), d.getDistance());
                }
            }
        }.start();

        // COMMENTS
        Competition comp = HsqldbInterface.getCompetition(idCompetition);
        final Comment c1 = new Comment(-1, "@luis, @victor y @jaime han salido un poco despacio... "
                + "Espero que no les ganen los robots!",
                "dani", new ArrayList<String>(), comp.getId(), 40452722, -3727811,
                comp.getScheduledDate());
        final Comment c2 = new Comment(-1, "#1 en cabeza! Y #5 y #2 persiguiendo desde cerca! Vamos!!!",
                "sara", new ArrayList<String>(), comp.getId(), 40452231,
                -3729784, comp.getScheduledDate());
        final Comment c3 = new Comment(-1, "Como van?? ya no alcanzo a verlos!!",
                "sara", new ArrayList<String>(), comp.getId(), 40452112, -3729768,
                comp.getScheduledDate());
        final Comment c4 = new Comment(-1, "Por aqui pasa @luis con algo de ventaja... aunque #5 y #2 le tienen a tiro! Carreron!",
                "pablo", new ArrayList<String>(), comp.getId(), 40450965, -3728459,
                comp.getScheduledDate());
        final Comment c5 = new Comment(-1, "vamos @luis!! La victoria es tuya!!!",
                "dani", new ArrayList<String>(), comp.getId(), 40452081, -3727756,
                comp.getScheduledDate());
        final Comment c6 = new Comment(-1, "Venga @jaime!! Arriba ese podium!!",
                "dani", new ArrayList<String>(), comp.getId(), 40452044, -3727745,
                comp.getScheduledDate());
        final Comment c7 = new Comment(-1, "Genial @victor!! Vaya carreron!",
                "dani", new ArrayList<String>(), comp.getId(), 40451991, -3727743,
                comp.getScheduledDate());

        new Thread() {

            public void run() {
                try {
                    Thread.sleep(8000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(RacePlayback.class.getName()).log(Level.SEVERE, null, ex);
                }
                HsqldbInterface.saveComment(c1);
                try {
                    Thread.sleep(44000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(RacePlayback.class.getName()).log(Level.SEVERE, null, ex);
                }
                HsqldbInterface.saveComment(c2);
                try {
                    Thread.sleep(38000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(RacePlayback.class.getName()).log(Level.SEVERE, null, ex);
                }
                HsqldbInterface.saveComment(c3);
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(RacePlayback.class.getName()).log(Level.SEVERE, null, ex);
                }
                HsqldbInterface.saveComment(c4);
                try {
                    Thread.sleep(40000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(RacePlayback.class.getName()).log(Level.SEVERE, null, ex);
                }
                HsqldbInterface.saveComment(c5);
                try {
                    Thread.sleep(7000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(RacePlayback.class.getName()).log(Level.SEVERE, null, ex);
                }
                HsqldbInterface.saveComment(c6);
                try {
                    Thread.sleep(6000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(RacePlayback.class.getName()).log(Level.SEVERE, null, ex);
                }
                HsqldbInterface.saveComment(c7);
            }
        }.start();
    }
}
