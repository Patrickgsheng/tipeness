/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.nyme.inga.tipeness.statdata;

import hu.nyme.inga.tipeness.statistics.Statistics;
import java.util.HashMap;
import hu.nyme.inga.tipeness.simulation.ConfigParser;

/**
 *
 * @author András Molnár
 */
public class AnalysisStatistic extends BatchAbstractStatistic {

    public enum StabilityEnum {

        stable, unstable, underthreshold, unknown
    };

    private HashMap<String, StabilityEnum> stabilityList;
    private final double THRESHOLD = 0.001;

    public AnalysisStatistic(ConfigParser configParser) {
        super(configParser);
        this.stabilityList = new HashMap<>();
    }

    @Override
    public boolean isAccurate() {
        if (numberOfN < getConfigParser().getMinNumOfN()) {
            return false;
        }
        for (String placeName : getConfigParser().getPlaces().keySet()) {
            if (getPlaceStability(placeName) == StabilityEnum.unknown) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String outResults() {
        String nl = System.lineSeparator();
        StringBuilder sb = new StringBuilder();
        sb.append(getConfigParser().outSystemParams(this));
        sb.append("------------------------------").append(nl).append(nl); 
        sb.append("RESULTS: ").append(nl).append(nl);
        for (String placeName : getConfigParser().getPlaces().keySet()) {
            switch (getPlaceStability(placeName)) {
                case stable:
                    sb.append(placeName).append(" is stable! (avg: ").append(estimatedAvgPlaceList.get(placeName).avg)
                            .append(")").append(", (diff: ")
                            .append(estimatedAvgDiffPlaceList.get(placeName).avg).append(")").append(nl);
                    break;
                case unstable:
                    sb.append(placeName).append(" is not stable! (avg: ").append(estimatedAvgPlaceList.get(placeName).avg)
                            .append(")").append(", (diff: ")
                            .append(estimatedAvgDiffPlaceList.get(placeName).avg).append(")").append(nl);
                    break;
                case underthreshold:
                    sb.append(placeName).append(" is stable, but it is not recommended as significant place!").append(nl);
                    break;
                case unknown:
                    sb.append("Problem at ").append(placeName).append(" place!").append(nl);
                    break;
            }
        }
        return sb.toString();
    }

    private StabilityEnum getPlaceStability(String placeName) {
        if (estimatedAvgDiffPlaceList.get(placeName).avg == 0 || estimatedAvgPlaceList.get(placeName).avg == 0) {
            return StabilityEnum.stable;
        } else if (Statistics.isAccurateEV(estimatedAvgPlaceList.get(placeName).avg, numberOfN, estimatedAvgPlaceList.get(placeName).variance,
                getConfigParser().getAccuracy(), getConfigParser().getAlpha())) {
            return StabilityEnum.stable;
        } else if (Statistics.isAccurateEV(estimatedAvgDiffPlaceList.get(placeName).avg, numberOfN, estimatedAvgDiffPlaceList.get(placeName).variance,
                getConfigParser().getAccuracy(), getConfigParser().getAlpha())) {
            return StabilityEnum.unstable;
        } else if (THRESHOLD > Statistics.getAbsError(estimatedAvgPlaceList.get(placeName).avg, getConfigParser().getAccuracy())
                && THRESHOLD > Statistics.getAbsError(estimatedAvgDiffPlaceList.get(placeName).avg, getConfigParser().getAccuracy())) {
            return StabilityEnum.underthreshold;
        } else {
            return StabilityEnum.unknown;
        }
    }
}
