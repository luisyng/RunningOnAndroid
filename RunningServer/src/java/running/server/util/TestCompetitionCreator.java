package running.server.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import running.domain.Athlete;
import running.domain.Comment;
import running.domain.Competition;
import running.server.db.HsqldbInterface;
import running.server.domain.Circuit;
import running.server.domain.CircuitPoint;

/**
 * Creates a new competition for testing propose
 */
public class TestCompetitionCreator {

    private static String[] circuitNames = {"Teleco", "Maudes", "Circuito olimpico",
    "Nuevos ministerios", "Paraninfo"};

    public static String[] getCircuitNames() {
        return circuitNames;
    }

    private static List<CircuitPoint> getCircuitPoints(int idCircuit) {
        List<CircuitPoint> circuit = new ArrayList<CircuitPoint>();
        if (idCircuit == 0) {
            circuit.add(new CircuitPoint(40452774, -3725862)); // Circuit Teleco
            circuit.add(new CircuitPoint(40452606, -3726796));
            circuit.add(new CircuitPoint(40453125, -3726871));
            circuit.add(new CircuitPoint(40453292, -3725874));

        } else if (idCircuit == 1) {
            circuit.add(new CircuitPoint(40445421, -3702635)); // Circuit Maudes
            circuit.add(new CircuitPoint(40445364, -3701937));
            circuit.add(new CircuitPoint(40446650, -3701809));
            circuit.add(new CircuitPoint(40446740, -3703718));
            circuit.add(new CircuitPoint(40445426, -3703295));
        } else if (idCircuit == 2) {
            circuit.add(new CircuitPoint(39981664, 116392619)); // Circuit olímpico Beijing
            circuit.add(new CircuitPoint(39981507, 116392877));
            circuit.add(new CircuitPoint(39981544, 116393317));
            circuit.add(new CircuitPoint(39981824, 116393553));
            circuit.add(new CircuitPoint(39982691, 116393494));
            circuit.add(new CircuitPoint(39982893, 116393333));
            circuit.add(new CircuitPoint(39982975, 116392989));
            circuit.add(new CircuitPoint(39982909, 116392721));
            circuit.add(new CircuitPoint(39982667, 116392523));
            circuit.add(new CircuitPoint(39981877, 116392544));
        } else if (idCircuit == 3) { // Castellana
            circuit.add(new CircuitPoint(40450975, -3697534));
            circuit.add(new CircuitPoint(40450991, -3698253));
            circuit.add(new CircuitPoint(40450240, -3698296));
            circuit.add(new CircuitPoint(40450264, -3699004));
            circuit.add(new CircuitPoint(40449562, -3699058));
            circuit.add(new CircuitPoint(40449554, -3698382));
            circuit.add(new CircuitPoint(40449325, -3698382));
            circuit.add(new CircuitPoint(40449260, -3696869));
            circuit.add(new CircuitPoint(40450893, -3696740));
        }
        else if (idCircuit == 4) { // Paraninfo
            circuit.add(new CircuitPoint(40452508,-3727971));
            circuit.add(new CircuitPoint(40452855,-3728191));
            circuit.add(new CircuitPoint(40453027,-3728540));
            circuit.add(new CircuitPoint(40453047,-3728862));
            circuit.add(new CircuitPoint(40452969,-3729167));
            circuit.add(new CircuitPoint(40452855,-3729360));
            circuit.add(new CircuitPoint(40452680,-3729516));
            circuit.add(new CircuitPoint(40452500,-3729580));
            circuit.add(new CircuitPoint(40452329,-3729564));
            circuit.add(new CircuitPoint(40450806,-3729280));
            circuit.add(new CircuitPoint(40450758,-3729210));
            circuit.add(new CircuitPoint(40450703,-3729065));
            circuit.add(new CircuitPoint(40450836,-3727818));
            circuit.add(new CircuitPoint(40450938,-3727702));
            circuit.add(new CircuitPoint(40452250,-3727928));
        }
        return circuit;
    }

    public static void deletePreviousAndCreateNewCompetition(int idCircuit) {
        List<Competition> comps = HsqldbInterface.getCompetitions();
        int id = comps.get(comps.size() - 1).getId() + 1;
        for (Competition comp : comps) {
            HsqldbInterface.setIsSimulating(comp.getId(), false);
            // HsqldbInterface.deleteCompetition(comp.getId());
        }
        createNewCompetition(id, idCircuit);
    }

    private static void createNewCompetition(int id, int idCircuit) {

        Competition comp = new Competition(id, circuitNames[idCircuit] + " " + id,
                new Date(), new Date(), "Madrid", Competition.NOT_STARTED, true, new int[0],
                "Carrera", 0);

        //Save the competition
        HsqldbInterface.saveCompetition(comp);

        // Save the categories
        int[] idCats = {1, 2, 3, 4, 5, 6, 7, 8};
        HsqldbInterface.saveCompetitionCategories(id, idCats);

        // Save the athletes
        List<Athlete> athletes = HsqldbInterface.getAllAthletes();
        HsqldbInterface.saveCompetitionAthletes(id, athletes);

        // Save the circuit
        List<CircuitPoint> points = getCircuitPoints(idCircuit);
        Circuit circuit = CircuitManager.createCircuit(points);
        HsqldbInterface.saveCircuit(points, id, circuit.getLength());

        // Save some example comments
        Comment c1 = new Comment(-1, "Bienvenidos a " + comp.getName() + ". "
                + "Hoy tengo el placer de poder narrarles la carrera.",
                "comentarista", new ArrayList<String>(), comp.getId(), circuit.getCenter().getLatitudeE6(),
                circuit.getCenter().getLongitudeE6(), new Date());
        Comment c2 = new Comment(-1, "Hace dia estupendo en " + comp.getLocation()
                + " para correr y contamos con la presencia de " + athletes.size()
                + " atletas.",
                "comentarista", new ArrayList<String>(), comp.getId(), circuit.getCenter().getLatitudeE6(),
                circuit.getCenter().getLongitudeE6(), new Date());
        CommentProcessor.addReferences(c1);
        CommentProcessor.addReferences(c2);
        HsqldbInterface.saveComment(c1);
        HsqldbInterface.saveComment(c2);
    }
}
