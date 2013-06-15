/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package running.server.util;

import java.util.ArrayList;
import running.domain.Athlete;
import running.domain.Comment;
import running.server.db.HsqldbInterface;

/**
 *
 * @author luis
 */
public class CommentProcessor {


    private static void replaceH (Comment c) {
        StringBuilder sb = new StringBuilder();
        String text = c.getText();
        for(int i = 0; i < text.length(); i++) {
            if(text.charAt(i) == '*') {
                sb.append('#');
            } else {
                sb.append(c.getText().charAt(i));
            }
        }
        c.setText(sb.toString());
    }
    public static void addReferences(Comment c) {

        replaceH(c);
        
        // Create an empty arraylist for the references
        c.setUserNames(new ArrayList<String>());

        String text = c.getText();

        int lastIndexAt;
        // Variable for not to cut the string in future searches
        int lastEndOfUserName = 0;
        while ((lastIndexAt = text.indexOf('@', lastEndOfUserName)) != -1) {

            // Only ends with the end of comment or white space
            int lastIndexSpace = text.indexOf(' ', lastIndexAt);
            String userName;
            if (lastIndexSpace != -1) {
                lastEndOfUserName = lastIndexSpace;
            } else {
                lastEndOfUserName = text.length();
            }

            // Cut the string to get the username
            userName = text.substring(lastIndexAt + 1, lastEndOfUserName);

            // Compare if the username exists
            if (HsqldbInterface.doesUsernameExist(userName)) {
                // Compare if the reference is not already set
                if (!userName.equals(c.getWriter())) {
                    boolean found = false;
                    for (String ref : c.getUserNames()) {
                        if (userName.equals(ref)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        c.getUserNames().add(userName);
                        System.out.println("user @" + userName);
                    }
                }
            } else {
                System.out.println("no existe @" + userName);
            }

        }

        int lastIndexH;
        // Variable for not to cut the string in future searches
        lastEndOfUserName = 0;
        while ((lastIndexH = text.indexOf('#', lastEndOfUserName)) != -1) {

            // Only ends with the end of comment or white space
            int lastIndexSpace = text.indexOf(' ', lastIndexH);
            if (lastIndexSpace != -1) {
                lastEndOfUserName = lastIndexSpace;
            } else {
                lastEndOfUserName = text.length();
            }

            // Cut the string to get the number
            int number = -1;
            try {
                number = Integer.parseInt(text.substring(lastIndexH + 1, lastEndOfUserName));
            } catch (NumberFormatException e) {

            }

            if(number != -1) {
                // Compare if the username exists
                Athlete ath = HsqldbInterface.getAthleteByNumber(c.getIdCompetition(), number);
                if (ath != null) {
                    // Compare if the reference is not already set
                    String userName = ath.getUserName();
                    if (!userName.equals(c.getWriter())) {
                        boolean found = false;
                        for (String ref : c.getUserNames()) {
                            if (userName.equals(ref)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            c.getUserNames().add(userName);
                            System.out.println("user @" + userName);
                        }
                    }
                } else {
                    System.out.println("no existe #" + number);
                }
            }
        }
    }
}
