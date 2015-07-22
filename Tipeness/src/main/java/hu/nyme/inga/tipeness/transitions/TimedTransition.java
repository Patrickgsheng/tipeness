/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.nyme.inga.tipeness.transitions;

import java.util.HashMap;
import hu.nyme.inga.tipeness.simulation.NetState;
import umontreal.iro.lecuyer.rng.RandRijndael;

/**
 *
 * @author András Molnár
 */
public class TimedTransition extends Transition {

    private String removedByTransition;
    private final RandRijndael rrj;
    private boolean hasRemTime;
    private double remainingTime;

    public TimedTransition(String name, HashMap<String, Integer> input, HashMap<String, Integer> output, HashMap<String, Integer> inhibitor) {
        super(name, input, output, inhibitor);
        this.rrj = new RandRijndael();
        this.hasRemTime = false;
    }

    public void generateWorkTime(NetState netState) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void fire(NetState netState) {
        super.fire(netState);
        this.hasRemTime = false;
        this.generateWorkTime(netState);
    }

    public RandRijndael getRandom() {
        return rrj;
    }

    public double getRemTime() {
        return remainingTime;
    }

    public void setRemTime(double remTime) {
        this.remainingTime = remTime;
    }

    public boolean isHasRemTime() {
        return hasRemTime;
    }

    public void setHasRemTime(boolean hasRemTime) {
        this.hasRemTime = hasRemTime;
    }

    @Override
    public TimedTransition copy() {
        Transition curr = super.copy();
        return new TimedTransition(curr.getName(), curr.getInput(), curr.getOutput(), curr.getInhibitor());
    }

    public String getRemovedByTransition() {
        return removedByTransition;
    }

    public void setRemovedByTransition(String removedByTransition) {
        this.removedByTransition = removedByTransition;
    }

}
