/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.nyme.inga.tipeness.statdata;

import java.util.HashMap;
import hu.nyme.inga.tipeness.simulation.ConfigParser;
import hu.nyme.inga.tipeness.simulation.NetState;
import hu.nyme.inga.tipeness.statistics.Statistics;

/**
 *
 * @author András Molnár
 */
public abstract class BatchAbstractStatistic extends MeasureUnitStatistic {

    HashMap<String, StatValues> estimatedAvgDiffPlaceList;

    public BatchAbstractStatistic(ConfigParser configParser) {
        super(configParser);
        estimatedAvgDiffPlaceList = new HashMap<>();
        for (String place : getConfigParser().getListDiffTokenList()) {
            estimatedAvgDiffPlaceList.put(place, new StatValues());
            estimatedAvgPlaceList.put(place, new StatValues());
        }
    }

    @Override
    protected void updateEstimates(NetState netState, MeasureUnit measureUnit) {
        super.updateAvgTokenNumEstimates(netState, measureUnit);
        updateAvgDiffEstimates(netState, measureUnit);
    }

    @Override
    public abstract boolean isAccurate();

    protected void updateAvgDiffEstimates(NetState netState, MeasureUnit measureUnit) {
        if (!estimatedAvgDiffPlaceList.isEmpty()) {
            for (String placeName : estimatedAvgDiffPlaceList.keySet()) {
                StatValues tempBatchStatValues = measureUnit.avgPalceList.get(placeName);
                if (prevMeasureUnit != null) {
                    StatValues prevBatchStatValues = prevMeasureUnit.avgPalceList.get(placeName);
                    double diff = tempBatchStatValues.avg - prevBatchStatValues.avg;
                    double prevAvg = estimatedAvgDiffPlaceList.get(placeName).avg;
                    estimatedAvgDiffPlaceList.get(placeName).avg = Statistics.avgWithDistinctWeights(diff,
                            measureUnit.length, prevAvg, sumLength);
                    estimatedAvgDiffPlaceList.get(placeName).variance = Statistics.variance(diff,
                            estimatedAvgDiffPlaceList.get(placeName).variance, prevAvg, estimatedAvgDiffPlaceList.get(placeName).avg, sumLength,
                            sumLength + measureUnit.length);
                }
            }
        }
    }

    public String outDiffs() {
        if (!estimatedAvgDiffPlaceList.isEmpty()) {
            String nl = System.lineSeparator();
            StringBuilder sb = new StringBuilder();
            sb.append("------------------------------").append(nl);
            sb.append("Average difference in tokennumbers ").append(" after ").append(numberOfN).append(" number of batches:")
                    .append(nl);
            for (String s : estimatedAvgDiffPlaceList.keySet()) {
                sb.append("Difference at ").append(s).append(": ").append(estimatedAvgDiffPlaceList.get(s).avg).append(nl);
            }
            sb.append("------------------------------").append(nl);
            return sb.toString();
        }
        return "";
    }
}
