/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.nyme.inga.tipeness.transitions;

import java.util.HashMap;

/**
 *
 * @author András Molnár
 */
public class DelayedTransition extends TimedTransition {

    private final double delay;

    public DelayedTransition(String name, double delay, HashMap<String, Integer> input, HashMap<String, Integer> output, HashMap<String, Integer> inhibitor) {
        super(name, input, output, inhibitor);
        this.delay = delay;
    }

    public double getDelay() {
        return delay;
    }

    @Override
    public String toString() {
        String nl = System.lineSeparator();
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append("Delay: ").append(delay).append(nl);
        return sb.toString();
    }

    @Override
    public DelayedTransition copy() {
        TimedTransition curr = super.copy();
        return new DelayedTransition(curr.getName(), delay, curr.getInput(), curr.getOutput(), curr.getInhibitor());

    }
}
