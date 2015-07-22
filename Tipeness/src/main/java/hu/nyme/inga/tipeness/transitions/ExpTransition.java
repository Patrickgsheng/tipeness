/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.nyme.inga.tipeness.transitions;

import java.util.HashMap;
import hu.nyme.inga.tipeness.simulation.NetState;
import umontreal.iro.lecuyer.randvar.ExponentialGen;

/**
 *
 * @author András Molnár
 */
public class ExpTransition extends DelayedTransition {

    private ExponentialGen exponentialGen;

    public enum ServerType {

        exclusive, infinite
    };
    private ServerType sType;

    public ExpTransition(String name, double delay, HashMap<String, Integer> input, HashMap<String, Integer> output, HashMap<String, Integer> inhibitor) {
        super(name, delay, input, output, inhibitor);
        this.exponentialGen = new ExponentialGen(this.getRandom(), 1 / delay);
        this.sType = ServerType.exclusive;
    }

    public ExpTransition(String name, double delay, ServerType sType, HashMap<String, Integer> input, HashMap<String, Integer> output, HashMap<String, Integer> inhibitor) {
        super(name, delay, input, output, inhibitor);
        this.exponentialGen = new ExponentialGen(this.getRandom(), 1 / delay);
        this.sType = sType;
    }

    @Override
    public void generateWorkTime(NetState netState) {
        if (!isEnabled(netState)) {
            return;
        }
        if (sType == ServerType.infinite) {
            ExponentialGen tempgen = new ExponentialGen(this.getRandom(), ((1 / getDelay()) * this.getEnablingDegree(netState)));
            this.setRemTime(tempgen.nextDouble());
        } else {
            this.setRemTime(exponentialGen.nextDouble());
        }
    }

    public ServerType getsType() {
        return sType;
    }

    @Override
    public String toString() {
        String nl = System.lineSeparator();
        StringBuilder sb = new StringBuilder(super.toString());
        switch (sType) {
            case exclusive:
                sb.append("Servertype: exclusive").append(nl);
                break;
            case infinite:
                sb.append("Servertype: infinite").append(nl);
                break;
        }
        sb.append("---------------------------------------------").append(nl);
        return sb.toString();
    }

    @Override
    public ExpTransition copy() {
        DelayedTransition curr = super.copy();
        return new ExpTransition(curr.getName(), curr.getDelay(), sType, curr.getInput(), curr.getOutput(), curr.getInhibitor());
    }

}
