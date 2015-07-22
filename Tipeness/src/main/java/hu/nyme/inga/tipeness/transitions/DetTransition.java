/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.nyme.inga.tipeness.transitions;

import java.util.HashMap;
import hu.nyme.inga.tipeness.simulation.ShowError;
import hu.nyme.inga.tipeness.simulation.NetState;
import umontreal.iro.lecuyer.randvar.ConstantGen;

/**
 *
 * @author András Molnár
 */
public class DetTransition extends DelayedTransition {

    private final ConstantGen constantGen;

    public DetTransition(String name, double delay, HashMap<String, Integer> input, HashMap<String, Integer> output, HashMap<String, Integer> inhibitor) {
        super(name, delay, input, output, inhibitor);
        if (delay <= 0) {
            ShowError.showError(ShowError.ErrorType.wrongDetParam, true, name);
        }
        this.constantGen = new ConstantGen(delay);
    }

    @Override
    public void generateWorkTime(NetState netState) {
        this.setRemTime(constantGen.nextDouble());
    }

    @Override
    public String toString() {
        String nl = System.lineSeparator();
        String sb = super.toString() + "-------------------------------------------" + nl;
        return sb;
    }

    @Override
    public DetTransition copy() {
        DelayedTransition curr = super.copy();
        return new DetTransition(curr.getName(), curr.getDelay(), curr.getInput(), curr.getOutput(), curr.getInhibitor());
    }
}
