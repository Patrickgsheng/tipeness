/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.nyme.inga.tipeness.simulation;

import hu.nyme.inga.tipeness.transitions.Transition;

/**
 *
 * @author András Molnár
 */
public class Event {

    private final double occurenceTime;
    private final String transitionName;
    private final int enablingDegree;

    public Event(String transName, double time, NetState netState) {
        this.transitionName = transName;
        this.occurenceTime = time;
        this.enablingDegree = getTransiton(netState).getEnablingDegree(netState);
    }

    public String getTransitionName() {
        return transitionName;
    }

    public double getTime() {
        return occurenceTime;
    }

    public final Transition getTransiton(NetState netState) {
        if (netState.getImmedTransitions().containsKey(transitionName)) {
            return netState.getImmedTransitions().get(this.transitionName);
        } else if (netState.getMemoryTransitions().containsKey(transitionName)) {
            return netState.getMemoryTransitions().get(this.transitionName);
        } else {
            return null;
        }
    }

    public int getEnablingDegree() {
        return enablingDegree;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("-------------------------------------\n");
        sb.append("Event transition: ").append(transitionName).append("\n");
        sb.append("Time: ").append(occurenceTime).append("\n");
        sb.append("Enabling degree: ").append(enablingDegree).append("\n");
        sb.append("-------------------------------------\n");
        return sb.toString();
    }
}
