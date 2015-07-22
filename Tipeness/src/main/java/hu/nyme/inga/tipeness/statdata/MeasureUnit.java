/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.nyme.inga.tipeness.statdata;

import hu.nyme.inga.tipeness.statistics.Statistics;
import java.util.HashMap;
import hu.nyme.inga.tipeness.simulation.ConfigParser;
import hu.nyme.inga.tipeness.simulation.NetState;

/**
 *
 * @author András Molnár
 */
public class MeasureUnit {

    public HashMap<String, Integer> tokenPalceList;
    public HashMap<String, StatValues> avgPalceList;
    double length;
    double prevLength;
    ConfigParser configParser;

    public MeasureUnit(ConfigParser configParser) {
        length = 0.0;
        this.tokenPalceList = new HashMap<>();
        this.avgPalceList = new HashMap<>();
        this.configParser = configParser;
        for (String placeName : configParser.getPlaces().keySet()) {
            avgPalceList.put(placeName, new StatValues());
        }
    }

    public boolean hasUnitEnded() {
        switch (configParser.getmType()) {
            case analysis:
                return (length >= configParser.getBatch());
            case batchmean:
                return (length >= configParser.getBatch());
            case repdel:
                return (length >= configParser.getTerminatingTime());
        }
        return true;
    }

    public void updateTimers(double delta) {
        prevLength = this.length;
        this.length += delta;
    }

    public void calculateStatValues(NetState netState) {
        for (String placeName : configParser.getListAvgTokenList()) {
            double previousAvg = avgPalceList.get(placeName).avg;
            avgPalceList.get(placeName).avg = Statistics.avgWithContinousTime(netState.getPlaces().get(placeName).getCurrent(),
                    previousAvg, this.prevLength, this.length);
            avgPalceList.get(placeName).variance = Statistics.variance(netState.getPlaces().get(placeName).getCurrent(), avgPalceList.get(placeName).variance,
                    previousAvg, avgPalceList.get(placeName).avg, this.prevLength, this.length);
        }
    }
}
