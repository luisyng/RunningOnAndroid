/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package running.server.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import running.domain.Athlete;
import running.domain.Comment;
import running.domain.Competition;
import running.server.domain.CircuitPoint;
import running.server.simulation.DistanceFromStart;
import running.server.util.ResultsCalculator;

/**
 *
 * @author luis
 */
public class HsqldbInterface {

    private static Connection con;
    private static Statement stmt;
    private static String SCHEDULED_DATE_FORMAT = "yyyy-MM-dd, HH:mm";

    /**
     * Connects to the database
     */
    public static void connectToDatabase() {
        try {
            Class.forName("org.hsqldb.jdbcDriver");
            con = DriverManager.getConnection("jdbc:hsqldb:file:/home/isfteleco/db/running", "sa", "");
            //con = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/running", "sa", "");
            stmt = con.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets all the competitions
     */
    public static List<Competition> getCompetitions() {
        connectToDatabase();

        String query = "SELECT * FROM competitions ORDER BY idcompetition ASC";
        List<Competition> competitions = new ArrayList<Competition>();
        try {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                competitions.add(getCompetitionFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return competitions;
    }

    /**
     * Deletes a competition
     */
    public static void deleteCompetition(int idCompetition) {
        connectToDatabase();

        try {
            stmt.executeQuery("DELETE FROM competitions WHERE idcompetition = " + idCompetition);
            stmt.executeQuery("DELETE FROM competitions_athletes WHERE idcompetition = " + idCompetition);
            stmt.executeQuery("DELETE FROM competitions_categories WHERE idcompetition = " + idCompetition);
            stmt.executeQuery("DELETE FROM distances_from_start WHERE idcompetition = " + idCompetition);
            stmt.executeQuery("DELETE FROM circuit_points WHERE idcompetition = " + idCompetition);
            stmt.executeQuery("DELETE FROM comments WHERE idcompetition = " + idCompetition);
            stmt.executeQuery("DELETE FROM comments_references");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Gets a competition
     */
    public static Competition getCompetition(int idCompetition) {
        connectToDatabase();

        String query = "SELECT * FROM competitions WHERE idCompetition = " + idCompetition;
        try {
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) {
                return getCompetitionFromResultSet(rs);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private static Competition getCompetitionFromResultSet(ResultSet rs) throws SQLException {
        Date scheduledDate = null;
        try {
            scheduledDate = new SimpleDateFormat(SCHEDULED_DATE_FORMAT).parse(
                    rs.getString(3) + ", " + rs.getString(4));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int id = rs.getInt(1);
        return new Competition(id, rs.getString(2),
                scheduledDate, new Date(rs.getLong(5)), rs.getString(6), rs.getInt(7), rs.getBoolean(8),
                getIdCategories(id), rs.getString(9), rs.getInt(10));
        // Db: IDCOMPETITION,NAME,DATE,TIME,LOCATION,STATE,HAS_CIRCUIT,EVENT_NAME,DISTANCE
        // Constructor: id,name,date,location,state,hasCircuitMap,idCategories,eventName,distance
    }

    private static int[] getIdCategories(int idCompetition) {
        int[] idCategories = new int[0];
        String query = "SELECT idCategory FROM competitions_categories WHERE idCompetition = " + idCompetition;
        try {
            ResultSet rs = stmt.executeQuery(query);
            // Try to find another way of doing this
            List<Integer> list = new ArrayList<Integer>();
            int i = 0;
            while (rs.next()) {
                list.add(rs.getInt(1));
            }
            idCategories = new int[list.size()];
            for (i = 0; i < list.size(); i++) {
                idCategories[i] = list.get(i);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return idCategories;
    }

    /**
     * Gets the athletes of a competition
     */
    public static List<Athlete> getCompetitionAthletes(int idCompetition, boolean setDistances) {
        connectToDatabase();

        List<Athlete> athletes = new ArrayList<Athlete>();
        String query = "SELECT * FROM athletes AS a, competitions_athletes AS ca WHERE a.idathlete = ca.idathlete AND ca.idcompetition = " + idCompetition + "  ORDER BY DISTANCE_FROM_START DESC, TIME ASC, IDATHLETE ASC";
        try {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                athletes.add(new Athlete(rs.getInt(1), rs.getString(2),
                        rs.getString(3), rs.getInt(4), rs.getInt(11),
                        rs.getInt(12), rs.getInt(10), rs.getInt(13),
                        false, rs.getInt(14), rs.getString(5)));
                // Db: IDATHLETE,FIRST_NAME,LAST_NAME,IDCATEGORY
                // Constructor: id,firstName,lastName,idCategory,absolutePosition,categoryPosition, 
                //distanceFromStart,time,hasArrived
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        if (setDistances) {
            ResultsCalculator.setPositions(athletes);
        }
        return athletes;
    }

    /**
     * Gets all the athletes
     */
    public static List<Athlete> getAllAthletes() {
        connectToDatabase();

        List<Athlete> athletes = new ArrayList<Athlete>();

        String query = "SELECT * FROM athletes";
        try {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                athletes.add(new Athlete(rs.getInt(1), rs.getString(2),
                        rs.getString(3), rs.getInt(4), 0,
                        0, 0, 0, false, 0, rs.getString(5)));
                // Db: IDATHLETE,FIRST_NAME,LAST_NAME,IDCATEGORY
                // Constructor: id,firstName,lastName,idCategory,following,color,absolutePosition,categoryPosition,distanceFromStart,time
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return athletes;
    }

    public static Athlete getAthleteByNumber(int idCompetition, int number) {
        connectToDatabase();

        String query = "SELECT a.* FROM athletes AS a, competitions_athletes AS ca "
                + "WHERE a.idathlete = ca.idathlete AND number = " + number
                + " AND idcompetition = " + idCompetition;
        try {
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) {
                return new Athlete(rs.getInt(1), rs.getString(2),
                        rs.getString(3), rs.getInt(4), 0,
                        0, 0, 0, false, 0, rs.getString(5));
                // Db: IDATHLETE,FIRST_NAME,LAST_NAME,IDCATEGORY
                // Constructor: id,firstName,lastName,idCategory,following,color,absolutePosition,categoryPosition,distanceFromStart,time
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Saves a circuit
     */
    public static void saveCircuit(List<CircuitPoint> points, int idCompetition, int distance) {
        connectToDatabase();

        String query = "INSERT INTO circuit_points VALUES (Null, ?, ?, ?)";
        PreparedStatement prep;
        try {
            // Delete the previous circuit points (if any)
            stmt.executeQuery("DELETE FROM circuit_points WHERE idCompetition = " + idCompetition);
            // Update the distance, centre and edge
            stmt.executeQuery("UPDATE competitions SET distance = " + distance
                    + " WHERE idCompetition = " + idCompetition);
            prep = con.prepareStatement(query);
            for (CircuitPoint cp : points) {
                prep.setDouble(1, idCompetition);
                prep.setDouble(2, cp.getLatitudeE6());
                prep.setDouble(3, cp.getLongitudeE6());
                prep.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the circuit of a competition
     */
    public static List<CircuitPoint> getCircuitPoints(int idCompetition) {
        connectToDatabase();

        String query = "SELECT latitudeE6, longitudeE6 FROM circuit_points WHERE idcompetition = " + idCompetition + " ORDER BY idcircuit_point ASC";
        List<CircuitPoint> points = new ArrayList<CircuitPoint>();
        try {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                points.add(new CircuitPoint(rs.getInt(1), rs.getInt(2)));
            }
            return points;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Saves the distance from start of an athlete
     */
    public static void saveDistanceFromStart(int idAthlete, int idCompetition, int distance) {
        connectToDatabase();

        try {
            Competition comp = getCompetition(idCompetition);
            long milis = new Date().getTime() - comp.getRealDate().getTime();
            stmt.executeQuery("INSERT INTO distances_from_start VALUES (Null, " + idAthlete + ", " + idCompetition + ", " + distance + ", " + milis + ")");
            stmt.executeQuery("UPDATE competitions_athletes SET distance_from_start = " + distance + ", time = " + milis + "  WHERE idAthlete = " + idAthlete + " AND idCompetition = " + idCompetition);

            if (!areStillAthletesRunning(idCompetition)) {
                setFinalPositions(idCompetition);
                setCompetitionHasEnded(idCompetition);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves the positions to database when the competition has ended
     */
    public static void setFinalPositions(int idCompetition) {
        // Get the athletes and set the positions
        List<Athlete> athletes = getCompetitionAthletes(idCompetition, true);

        // Save the positions
        connectToDatabase();
        try {
            PreparedStatement prep = con.prepareStatement("UPDATE competitions_athletes SET absolute_position = ?, category_position = ? WHERE idathlete = ? AND idcompetition = ?");
            prep.setInt(4, idCompetition);
            for (Athlete ath : athletes) {
                prep.setInt(1, ath.getAbsolutePosition());
                prep.setInt(2, ath.getCategoryPosition());
                prep.setInt(3, ath.getId());
                prep.executeUpdate();
            }
        } catch (SQLException ex) {
            Logger.getLogger(HsqldbInterface.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Checks if are athletes still running
     */
    public static boolean areStillAthletesRunning(int idCompetition) {
        connectToDatabase();
        try {
            ResultSet rs = stmt.executeQuery("SELECT * FROM competitions_athletes AS ca, competitions AS c WHERE ca.idcompetition = c.idcompetition AND c.idcompetition = " + idCompetition + " AND ca.distance_from_start < c.distance LIMIT 1");
            return rs.next();
        } catch (SQLException ex) {
            Logger.getLogger(HsqldbInterface.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    /**
     * Sets that a competition has started
     */
    public static void setCompetitionHasStarted(int idCompetition) {
        connectToDatabase();

        try {
            stmt.executeUpdate("UPDATE competitions SET state=1, real_timemilis='" + new Date().getTime()
                    + "' WHERE idcompetition = " + idCompetition);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets that a competition has ended
     */
    public static void setCompetitionHasEnded(int idCompetition) {
        connectToDatabase();

        try {
            stmt.executeUpdate("UPDATE competitions SET state = 2, is_simulating = false WHERE idcompetition = " + idCompetition);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

        /**
     * Sets that a competition has ended
     */
    public static void setCompetitionHasNotStarted(int idCompetition) {
        connectToDatabase();

        try {
            stmt.executeUpdate("UPDATE competitions SET state = 0, is_simulating = false WHERE idcompetition = " + idCompetition);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves a new competition
     */
    public static void saveCompetition(Competition comp) {
        connectToDatabase();

        String query = "INSERT INTO competitions VALUES (Null, ?, ?, ?, ?, ?, ?, ?, ?, 0, FALSE)";
        // Db: IDCOMPETITION,NAME,DATE,TIME,LOCATION,STATE,HAS_CIRCUIT,EVENT_NAME,DISTANCE
        try {
            PreparedStatement prep = con.prepareStatement(query);
            prep.setString(1, comp.getName());
            prep.setString(2, new SimpleDateFormat("yyyy-MM-dd").format(comp.getScheduledDate()));
            prep.setString(3, new SimpleDateFormat("HH:mm").format(comp.getScheduledDate()));
            prep.setLong(4, comp.getRealDate().getTime());
            prep.setString(5, comp.getLocation());
            prep.setInt(6, comp.getState());
            prep.setBoolean(7, comp.isHasCircuitMap());
            prep.setString(8, comp.getEventName());
            prep.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Save the categories of a competition
     */
    public static void saveCompetitionCategories(int idCompetition, int[] idCats) {
        connectToDatabase();

        String query = "INSERT INTO competitions_categories VALUES (Null, ?, ?)";
        try {
            PreparedStatement prep = con.prepareStatement(query);
            for (int idCat : idCats) {
                prep.setInt(1, idCompetition);
                prep.setInt(2, idCat);
                prep.executeUpdate();
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Save the athletes of a competition
     */
    public static void saveCompetitionAthletes(int idCompetition, List<Athlete> athletes) {
        connectToDatabase();

        String query = "INSERT INTO competitions_athletes VALUES (Null, ?, ?, 0, 0, 0, 0, ?)";
        try {
            PreparedStatement prep = con.prepareStatement(query);
            for (Athlete ath : athletes) {
                prep.setInt(1, idCompetition);
                prep.setInt(2, ath.getId());
                prep.setInt(3, getCompetitionAthletes(idCompetition, false).size() + 1);
                prep.executeUpdate();
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Gets an athlete if the username and password is correct, null if not
     */
    public static Athlete logIn(String username, String password) {
        connectToDatabase();
        String query = "SELECT * FROM athletes WHERE username = '" + username + "' AND password = '" + password + "'";

        try {
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) {
                return new Athlete(rs.getInt(1), rs.getString(2),
                        rs.getString(3), rs.getInt(4), 0,
                        0, 0, 0, false, 0, rs.getString(5));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Sets if it's simulating a competition. Setting it to false when
     * simulating will stop the simulator
     */
    public static void setIsSimulating(int idCompetition, boolean isSimulating) {
        connectToDatabase();

        try {
            stmt.executeQuery("UPDATE competitions SET is_simulating = " + isSimulating + " WHERE idCompetition = " + idCompetition);

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Get if a competition is being simulated
     */
    public static boolean getIsSimulating(int idCompetition) {
        connectToDatabase();

        String query = "SELECT is_simulating FROM competitions WHERE idCompetition = " + idCompetition;
        try {
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) {
                return rs.getBoolean(1);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * Save the comment
     */
    public static void saveComment(Comment c) {
        connectToDatabase();

        String commentQuery = "INSERT INTO comments VALUES (Null, ?, ?, ?, ?, ?, ?)";
        String usersQuery = "INSERT INTO comments_references VALUES (Null, ?, ?)";
        String idQuery = "SELECT * FROM comments ORDER BY idcomment DESC LIMIT 1";
        try {
            PreparedStatement prep = con.prepareStatement(commentQuery);
            prep.setString(1, c.getText());
            prep.setString(2, c.getWriter());
            prep.setInt(3, c.getIdCompetition());
            prep.setInt(4, c.getLatE6());
            prep.setInt(5, c.getLonE6());
            prep.setLong(6, c.getDate().getTime());
            prep.executeUpdate();

            // Get the id
            ResultSet rs = con.prepareStatement(idQuery).executeQuery();
            if (rs != null && rs.next()) {
                c.setId(rs.getInt(1));
            }

            prep = con.prepareStatement(usersQuery);
            for (String userName : c.getUserNames()) {
                prep.setInt(1, c.getId());
                prep.setString(2, userName);
                System.out.println(usersQuery + c.getId() + userName);
                prep.executeUpdate();
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static List<Comment> getComments(int idCompetition,
            int lastIdComment) {
        connectToDatabase();

        String commentsQuery = "SELECT * FROM comments WHERE idCompetition = "
                + idCompetition + " AND idcomment > " + lastIdComment
                + " ORDER BY idcomment DESC";
        String referencesQuery = "SELECT username FROM comments_references "
                + "WHERE idcomment = ?";

        List<Comment> comments = new ArrayList<Comment>();
        try {
            PreparedStatement prep = con.prepareStatement(referencesQuery);
            ResultSet rs = stmt.executeQuery(commentsQuery);
            while (rs.next()) {
                // Set the references
                List<String> refs = new ArrayList<String>();
                prep.setInt(1, rs.getInt(1));
                ResultSet rs1 = prep.executeQuery();
                while (rs1.next()) {
                    refs.add(rs1.getString(1));
                }

                // Save the comment
                comments.add(new Comment(rs.getInt(1), rs.getString(2),
                        rs.getString(3), refs, idCompetition, rs.getInt(5),
                        rs.getInt(6), new Date(rs.getLong(7))));
            }
            return comments;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Gets all the athletes
     */
    public static boolean doesUsernameExist(String userName) {
        connectToDatabase();

        String query = "SELECT * FROM athletes WHERE username = '" + userName + "'";
        try {
            ResultSet rs = stmt.executeQuery(query);
            return rs.next();
        } catch (SQLException ex) {
            return false;
        }
    }

    public static List<DistanceFromStart> getDistancesFromStart(int idCompetition) {
        connectToDatabase();

        String query = "SELECT * FROM distances_from_start WHERE idCompetition="
                + idCompetition + " ORDER BY time";

        List<DistanceFromStart> distances = new ArrayList<DistanceFromStart>();
        try {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                distances.add(new DistanceFromStart(rs.getInt(2), rs.getInt(3),
                        rs.getInt(4), rs.getLong(5)));
            }
            
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
        return distances;
    }

    public static void removeDistancesFromStart(int idCompetition) {
        connectToDatabase();

        String query = "DELETE FROM distances_from_start WHERE idCompetition="
                + idCompetition;      
        try {
            ResultSet rs = stmt.executeQuery(query);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

        /**
     * Saves a circuit
     */
    public static void distancesToZero(int idCompetition) {
        connectToDatabase();
        try {
            stmt.executeQuery("UPDATE competitions_athletes SET distance_from_start = 0"
                    + ", time = 0, absolute_position = 0, category_position = 0"
                    + " WHERE idCompetition = " + idCompetition);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * Shutdown db
     */
    public static void shutdown() {
        connectToDatabase();
        try {
            stmt.executeQuery("SHUTDOWN");
        } catch (SQLException ex) {
            Logger.getLogger(HsqldbInterface.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
