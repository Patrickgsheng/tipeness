/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.nyme.inga.tipeness.simulation;

import hu.nyme.inga.tipeness.statdata.AnalysisStatistic;
import hu.nyme.inga.tipeness.statdata.ReplicationStatistic;
import hu.nyme.inga.tipeness.statdata.BatchStatistic;
import java.util.HashMap;
import hu.nyme.inga.tipeness.statdata.MeasureUnit;

/**
 *
 * @author András Molnár
 */
public class SimulationMethod {

    public static void replicationSimulation(SimulationUnit simUnit) {

        ConfigParser currentConfig = simUnit.getConfigParser();
        ReplicationStatistic repStat = (ReplicationStatistic) simUnit.getMeasureUnitStatistic();
        HashMap<String, Place> initialMarking = runWarmingupPeriod(currentConfig);

        while (!repStat.isAccurate()) {
            NetState netState = new NetState(currentConfig, initialMarking);
            EventQueue eq = netState.getEventqueue();
            eq.updateQueue(currentConfig, netState);
            MeasureUnit currentReplication = new MeasureUnit(currentConfig);
            while (!currentReplication.hasUnitEnded()) {
                netState.setCurrentEvent(eq.getNextEvent());
                Event currentEvent = netState.getCurrentEvent();

                netState.setTime(currentEvent.getTime());
                currentReplication.updateTimers(netState.getTime() - netState.getPreviousTime());
                currentReplication.calculateStatValues(netState);
                if (currentReplication.hasUnitEnded()) {
                    repStat.update(netState, currentReplication);
                }
                currentEvent.getTransiton(netState).fire(netState);
                netState.getEventqueue().updateQueue(currentConfig, netState);
                netState.setPreviousTime(netState.getTime());
            }
        }
    }

    public static void batchMeansSimulation(SimulationUnit simUnit) {

        BatchStatistic batchStat = (BatchStatistic) simUnit.getMeasureUnitStatistic();
        ConfigParser currentConfig = simUnit.getConfigParser();

        HashMap<String, Place> initialMarking = runWarmingupPeriod(currentConfig);
        NetState netState = new NetState(currentConfig, initialMarking);
        EventQueue eq = netState.getEventqueue();
        eq.updateQueue(currentConfig, netState);
        MeasureUnit currentBatch = new MeasureUnit(currentConfig);
        while (!batchStat.isAccurate() && !eq.getEventQueue().isEmpty()) {
            netState.setCurrentEvent(eq.getNextEvent());
            Event currentEvent = netState.getCurrentEvent();
            netState.setTime(currentEvent.getTime());
            currentBatch.updateTimers(netState.getTime() - netState.getPreviousTime());

            currentBatch.calculateStatValues(netState);
            currentEvent.getTransiton(netState).fire(netState);
            netState.getEventqueue().updateQueue(currentConfig, netState);
            if (currentBatch.hasUnitEnded()) {
                batchStat.update(netState, currentBatch);
                currentBatch = new MeasureUnit(currentConfig);
            }
            netState.setPreviousTime(netState.getTime());
        }
    }

    public static void analysisSimulation(SimulationUnit simUnit) {

        AnalysisStatistic analysisStat = (AnalysisStatistic) simUnit.getMeasureUnitStatistic();
        ConfigParser currentConfig = simUnit.getConfigParser();

        HashMap<String, Place> initialMarking = runWarmingupPeriod(currentConfig);
        NetState netState = new NetState(currentConfig, initialMarking);
        EventQueue eq = netState.getEventqueue();
        eq.updateQueue(currentConfig, netState);
        MeasureUnit currentBatch = new MeasureUnit(currentConfig);
        while (!analysisStat.isAccurate() && !eq.getEventQueue().isEmpty()) {
            netState.setCurrentEvent(eq.getNextEvent());
            Event currentEvent = netState.getCurrentEvent();
            netState.setTime(currentEvent.getTime());
            currentBatch.updateTimers(netState.getTime() - netState.getPreviousTime());

            currentBatch.calculateStatValues(netState);
            currentEvent.getTransiton(netState).fire(netState);
            netState.getEventqueue().updateQueue(currentConfig, netState);

            if (currentBatch.hasUnitEnded()) {
                analysisStat.update(netState, currentBatch);

                currentBatch = new MeasureUnit(currentConfig);

            }
            netState.setPreviousTime(netState.getTime());
        }
    }

    private static HashMap<String, Place> runWarmingupPeriod(ConfigParser configParser) {
        NetState netState = new NetState(configParser);
        EventQueue eq = netState.getEventqueue();
        eq.updateQueue(configParser, netState);
        while (netState.getTime() <= configParser.getWarmupLength()) {
            netState.setCurrentEvent(eq.getNextEvent());
            Event currentEvent = netState.getCurrentEvent();
            netState.setTime(currentEvent.getTime());
            currentEvent.getTransiton(netState).fire(netState);
            netState.getEventqueue().updateQueue(configParser, netState);
        }
        return netState.getPlaces();
    }
}
