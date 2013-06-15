package running.server.servlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import running.domain.Athlete;
import running.domain.Competition;
import running.json.JSONAdapter;
import running.server.db.HsqldbInterface;
import running.server.domain.CircuitPoint;
import running.server.json.JSONAdapterServer;
import running.server.simulation.Simulator;
import running.server.util.TestCompetitionCreator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import running.domain.Comment;
import running.server.simulation.RacePlayback;
import running.server.util.CommentProcessor;

/**
 * Servlet to process the http requests
 */
public class Servlet extends HttpServlet {

    // ACTIONS
    private static final String COMPETITIONS = "competitions";
    private static final String ATHLETES = "athletes";
    private static final String CIRCUIT = "circuit";
    private static final String SEND_DISTANCE = "sendDistance";
    private static final String GET_DISTANCES = "getDistances";
    private static final String LOGIN = "login";
    private static final String CHECK_HAS_STARTED = "checkHasStarted";
    private static final String SET_HAS_STARTED = "setHasStarted";
    private static final String STOP_SIMULATING = "stopSimulating";
    private static final String TEST_COMPETITION = "testCompetition";
    private static final String TEST_CIRCUITS = "testCircuits";
    private static final String POST_COMMENT = "postComment";
    private static final String COMMENTS = "comments";
    private static final String SHUTDOWN = "shutdown";
    private static final String SEND_IMAGE = "sendImage";
    private static final String RESET = "reset";
    private static final String PLAYBACK = "playback";

    // REQUEST PARAMTETERS
    private final static String IDCOMPETITION = "id";
    private final static String IDATHLETE = "idAth";
    private final static String IDCIRCUIT = "idCir";
    private final static String DISTANCE = "dis";
    private final static String USERNAME = "user";
    private final static String PASSWORD = "pw";
    private final static String COMMENT = "cm";
    private final static String LAST_IDCOMMENT = "lastIdc";
    private final static String LATE6 = "lat";
    private final static String LONE6 = "lon";

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Look for the request action
        String action = request.getServletPath();

        // Writer for the response
        PrintWriter out = response.getWriter();

        // Parsing of the athlete
        action = action.substring(action.lastIndexOf('/') + 1,
                action.indexOf('.'));
        response.setCharacterEncoding("UTF8");
        response.setContentType("application/json; charset=UTF-8");

        // Gets the competitions
        if (action.equals(COMPETITIONS)) {
            List<Competition> competitions = HsqldbInterface.getCompetitions();
            JSONArray jsonArray;
            try {
                jsonArray = JSONAdapter.competitionListToJSON(competitions);
                out.println(jsonArray);
            } catch (JSONException ex) {
                Logger.getLogger(Servlet.class.getName()).log(Level.SEVERE, null, ex);
            }

            // Gets the athletes
        } else if (action.equals(ATHLETES)) {
            int idCompetition = 0;
            try {
                idCompetition = Integer.parseInt(request.getParameter(IDCOMPETITION));
            } catch (IllegalArgumentException e) {
                out.println(0);
                e.printStackTrace();
                return;
            }
            List<Athlete> athletes = HsqldbInterface.getCompetitionAthletes(idCompetition, false);
            JSONArray jsonArray;
            try {
                jsonArray = JSONAdapter.athleteListToJSON(athletes);
                out.println(jsonArray);
            } catch (JSONException ex) {
                Logger.getLogger(Servlet.class.getName()).log(Level.SEVERE, null, ex);
            }

            // Gets the circuit
        } else if (action.equals(CIRCUIT)) {
            int idCompetition = 0;
            try {
                idCompetition = Integer.parseInt(request.getParameter(IDCOMPETITION));
            } catch (IllegalArgumentException e) {
                out.println(0);
                e.printStackTrace();
                return;
            }
            List<CircuitPoint> circuitPoints = HsqldbInterface.getCircuitPoints(idCompetition);
            JSONArray jsonArr;
            try {
                jsonArr = JSONAdapterServer.circuitPointListToJSON(circuitPoints);
                out.println(jsonArr);
            } catch (JSONException ex) {
                Logger.getLogger(Servlet.class.getName()).log(Level.SEVERE, null, ex);
            }

            // Saves an athlete distance from start
        } else if (action.equals(SEND_DISTANCE)) {
            int idAthlete = 0;
            int idCompetition = 0;
            int distance = 0;
            try {
                idAthlete = Integer.parseInt(request.getParameter(IDATHLETE));
                idCompetition = Integer.parseInt(request.getParameter(IDCOMPETITION));
                distance = Integer.parseInt(request.getParameter(DISTANCE));
            } catch (IllegalArgumentException e) {
                out.println(0);
                e.printStackTrace();
                return;
            }
            HsqldbInterface.saveDistanceFromStart(idAthlete, idCompetition, distance);

            // Gets the last athlete distances and positions
        } else if (action.equals(GET_DISTANCES)) {
            int idCompetition = 0;
            try {
                idCompetition = Integer.parseInt(request.getParameter(IDCOMPETITION));
            } catch (IllegalArgumentException e) {
                out.println(0);
                e.printStackTrace();
                return;
            }

            try {
                JSONObject jsonObj = JSONAdapter.participationsToJSON(
                        HsqldbInterface.getCompetitionAthletes(idCompetition, true),
                        HsqldbInterface.getCompetition(idCompetition));
                out.println(jsonObj);
            } catch (JSONException ex) {
                out.println(0);
                ex.printStackTrace();
                return;
            }

            // Receives an attempt of login
        } else if (action.equals(LOGIN)) {
            String username = request.getParameter(USERNAME);
            String password = request.getParameter(PASSWORD);
            Athlete ath = HsqldbInterface.logIn(username, password);
            if (ath == null) {
                out.println(0);
            } else {
                try {
                    out.println(JSONAdapter.athleteToJSON(ath));
                } catch (JSONException ex) {
                    Logger.getLogger(Servlet.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            // Asking if the competition has started
        } else if (action.equals(CHECK_HAS_STARTED)) {
            int idCompetition = 0;
            try {
                idCompetition = Integer.parseInt(request.getParameter(IDCOMPETITION));
            } catch (IllegalArgumentException e) {
                out.println(-1);
                e.printStackTrace();
                return;
            }
            Competition competition = HsqldbInterface.getCompetition(idCompetition);
            if (competition == null) {
                out.println(0);
                return;
            }
            if (competition.getState() != Competition.NOT_STARTED) {
                out.println(competition.gtRealDateAndTime());
            } else {
                out.println(0);
            }

            // Sets the competition has started and starts the simulation
        } else if (action.equals(SET_HAS_STARTED)) {
            int idCompetition = 0;
            try {
                idCompetition = Integer.parseInt(request.getParameter(IDCOMPETITION));
            } catch (IllegalArgumentException e) {
                out.println(-1);
                e.printStackTrace();
                return;
            }
            HsqldbInterface.setCompetitionHasStarted(idCompetition);
            HsqldbInterface.setIsSimulating(idCompetition, true);
            // Starts the simulator (internally in other thread
            Simulator.simulate(idCompetition);
            out.println(0);

            // Gets the circuit test names
        } else if (action.equals(TEST_CIRCUITS)) {
            String[] circuitNames = TestCompetitionCreator.getCircuitNames();
            JSONArray jsonArr;
            try {
                jsonArr = JSONAdapter.circuitNamesListToJSON(circuitNames);
                out.println(jsonArr.toString());
            } catch (JSONException ex) {
                Logger.getLogger(Servlet.class.getName()).log(Level.SEVERE, null, ex);
                out.println(0);
            }

            // Creates a test competition
        } else if (action.equals(TEST_COMPETITION)) {
            int idCircuit = 0;
            try {
                idCircuit = Integer.parseInt(request.getParameter(IDCIRCUIT));
            } catch (IllegalArgumentException e) {
                out.println(0);
                e.printStackTrace();
                return;
            }
            TestCompetitionCreator.deletePreviousAndCreateNewCompetition(idCircuit);
            out.println(0);

            // Stops the simulation
        } else if (action.equals(STOP_SIMULATING)) {
            int idCompetition = 0;
            try {
                idCompetition = Integer.parseInt(request.getParameter(IDCOMPETITION));
            } catch (IllegalArgumentException e) {
                out.println(-1);
                e.printStackTrace();
                return;
            }
            HsqldbInterface.setIsSimulating(idCompetition, false);
            out.println(0);

            // Saves a comment posted
        } else if (action.equals(POST_COMMENT)) {
            String comment = request.getParameter(COMMENT);
            String writer = request.getParameter(USERNAME);
            int latE6 = 0;
            int lonE6 = 0;
            int idCompetition = 0;
            try {
                latE6 = Integer.parseInt(request.getParameter(LATE6));
                lonE6 = Integer.parseInt(request.getParameter(LONE6));
                idCompetition = Integer.parseInt(request.getParameter(IDCOMPETITION));
            } catch (IllegalArgumentException e) {
                out.println(-1);
                e.printStackTrace();
                return;
            }
            Comment c = new Comment(-1, comment, writer, null,
                    idCompetition, latE6, lonE6, new Date());
            CommentProcessor.addReferences(c);
            HsqldbInterface.saveComment(c);
        } // Gets the athletes
        else if (action.equals(COMMENTS)) {
            int idCompetition = 0;
            int lastIdComment = 0;
            try {
                idCompetition = Integer.parseInt(request.getParameter(IDCOMPETITION));
                lastIdComment = Integer.parseInt(request.getParameter(LAST_IDCOMMENT));
            } catch (IllegalArgumentException e) {
                out.println(0);
                e.printStackTrace();
                return;
            }
            List<Comment> comments = HsqldbInterface.getComments(idCompetition,
                    lastIdComment);
            JSONArray jsonArray;
            try {
                jsonArray = JSONAdapter.commentListToJSON(comments);
                out.println(jsonArray.toString());
            } catch (JSONException ex) {
                Logger.getLogger(Servlet.class.getName()).log(Level.SEVERE, null, ex);
            }
        } // Send image
        else if (action.equals(SEND_IMAGE)) {
            System.out.println("SEND IMAGE");
            InputStream is = request.getInputStream();

            byte[] photo = new byte[request.getContentLength()];
            System.out.println("bya: " + photo[0] + photo[1]);
            is.read(photo);
            System.out.println("byd: " + photo[0] + photo[1]);
            File file = new File("/home/luis", "img.jpg");
            OutputStream os = new FileOutputStream(file);
            os.write(photo);
            os.close();
            // Reset a competition
        } else if (action.equals(RESET)) {
            int idCompetition = 0;
            try {
                idCompetition = Integer.parseInt(request.getParameter(IDCOMPETITION));
            } catch (IllegalArgumentException e) {
                out.println(-1);
                e.printStackTrace();
                return;
            }
            RacePlayback.reset(idCompetition);
            out.println(0);
        // Reset a competition
        } else if (action.equals(PLAYBACK)) {
            int idCompetition = 0;
            try {
                idCompetition = Integer.parseInt(request.getParameter(IDCOMPETITION));
            } catch (IllegalArgumentException e) {
                out.println(-1);
                e.printStackTrace();
                return;
            }
            RacePlayback.playback(idCompetition);
            out.println(0);
        } else if (action.equals(SHUTDOWN)) {
            HsqldbInterface.shutdown();
        } else {
            out.println("Not still implemented or wrong address!");
        }
    }
}
