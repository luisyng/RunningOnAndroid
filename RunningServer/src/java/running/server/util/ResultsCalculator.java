package running.server.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import running.domain.Athlete;

public class ResultsCalculator {

    public static void setPositions(List<Athlete> athletes) {        
        // Maps
        Map<Integer, Athlete> lastAthsOfCategory = new HashMap<Integer, Athlete>();
        Map<Integer, Integer> numberOfAthletesOfCategory = new HashMap<Integer, Integer>();

        for (int i = 0; i < athletes.size(); i++) {
            Athlete ath = athletes.get(i);

            // ABSOLUTE POSITION
            ath.setAbsolutePosition(i + 1); // By default, the position is i+1
            if (i > 0) {
                Athlete previousAth = athletes.get(i - 1);
                // However, if the athlete has run the same distance than the previous,
                // we give him the same position
                if (!isAfter(previousAth, ath)) {
                    ath.setAbsolutePosition(previousAth.getAbsolutePosition());
                }
            }

            // CATEGORY POSITION
            Athlete previousCategoryAth = lastAthsOfCategory.get(ath.getIdCategory());
            if (previousCategoryAth == null) {
                ath.setCategoryPosition(1);
                numberOfAthletesOfCategory.put(ath.getIdCategory(), 1);
            } else {
                numberOfAthletesOfCategory.put(ath.getIdCategory(), numberOfAthletesOfCategory.get(ath.getIdCategory()) + 1);
                // If the athlete has run the same distance than the previous,
                // we give him the same position
                if (!isAfter(previousCategoryAth, ath)) {
                    ath.setCategoryPosition(previousCategoryAth.getCategoryPosition());
                } else {
                    ath.setCategoryPosition(numberOfAthletesOfCategory.get(ath.getIdCategory()));
                }
            }
            lastAthsOfCategory.put(ath.getIdCategory(), ath);
        }
    }

    private static boolean isAfter(Athlete previousAth, Athlete nextAth) {
        return previousAth.getDistanceFromStart() > nextAth.getDistanceFromStart() ||
                (previousAth.getDistanceFromStart() == nextAth.getDistanceFromStart()
                && previousAth.getTime() < nextAth.getTime());
    }
}
