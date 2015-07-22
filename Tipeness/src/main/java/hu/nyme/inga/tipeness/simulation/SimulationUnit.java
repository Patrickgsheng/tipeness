/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.nyme.inga.tipeness.simulation;

import hu.nyme.inga.tipeness.statdata.AnalysisStatistic;
import hu.nyme.inga.tipeness.statdata.BatchStatistic;
import hu.nyme.inga.tipeness.statdata.MeasureUnitStatistic;
import hu.nyme.inga.tipeness.statdata.ReplicationStatistic;

/**
 *
 * @author András Molnár
 */
public class SimulationUnit {

    private ConfigParser configParser;
    private MeasureUnitStatistic measureUnitStatistic;

    public SimulationUnit(String paramFilePath) {
        configParser = new ConfigParser(paramFilePath);
        switch (configParser.getmType()) {
            case analysis:
                measureUnitStatistic = new AnalysisStatistic(configParser);
                break;
            case batchmean:
                measureUnitStatistic = new BatchStatistic(configParser);
                break;
            case repdel:
                measureUnitStatistic = new ReplicationStatistic(configParser);
                break;
        }
    }

    public void runSimulation() {
        switch (configParser.getmType()) {
            case analysis:
                SimulationMethod.analysisSimulation(this);
                break;
            case batchmean:
                SimulationMethod.batchMeansSimulation(this);
                break;
            case repdel:
                SimulationMethod.replicationSimulation(this);
                break;
        }
    }

    public ConfigParser getConfigParser() {
        return configParser;
    }

    public MeasureUnitStatistic getMeasureUnitStatistic() {
        switch (configParser.getmType()) {
            case analysis:
                return (AnalysisStatistic) measureUnitStatistic;
            case batchmean:
                return (BatchStatistic) measureUnitStatistic;
            case repdel:
                return (ReplicationStatistic) measureUnitStatistic;
            default:
                return measureUnitStatistic;
        }
    }
}
