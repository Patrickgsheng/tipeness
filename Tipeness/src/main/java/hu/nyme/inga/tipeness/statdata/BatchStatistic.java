/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.nyme.inga.tipeness.statdata;

import hu.nyme.inga.tipeness.simulation.ConfigParser;
import hu.nyme.inga.tipeness.statistics.Statistics;

/**
 *
 * @author András Molnár
 */
public class BatchStatistic extends BatchAbstractStatistic {

    public BatchStatistic(ConfigParser configParser) {
        super(configParser);
    }

    @Override
    public boolean isAccurate() {
        if (this.numberOfN < getConfigParser().getMinNumOfN()) {
            return false;
        }
        return (super.areAvgTokenNumEstimatesAccurate() && areAvgTokenDiffEstimatesAccurate());
    }

    protected boolean areAvgTokenDiffEstimatesAccurate() {
        ConfigParser currentConfig = getConfigParser();
        if (estimatedAvgDiffPlaceList.isEmpty()) {
            return true;
        }
        for (String placeName : currentConfig.getWatchDiffTokenList()) {
            if (!Statistics.isAccurateEV(estimatedAvgDiffPlaceList.get(placeName).avg, numberOfN,
                    estimatedAvgDiffPlaceList.get(placeName).variance, currentConfig.getAccuracy(), currentConfig.getAlpha())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String outResults() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.outResults());        
        return sb.append(outDiffValues()).toString();
    }

    private String outDiffValues() {
        if (!getConfigParser().getListDiffTokenList().isEmpty()) {
            String nl = System.lineSeparator();
            StringBuilder sb = new StringBuilder();
            sb.append("------------------------------").append(nl);            
            for (String placeName : getConfigParser().getListDiffTokenList()) {
                sb.append("Average difference at ").append(placeName).append(": ").append(estimatedAvgDiffPlaceList.get(placeName).avg).append(nl);
            }
            sb.append("------------------------------").append(nl);
            return sb.toString();
        }
        return "";
    }
}
