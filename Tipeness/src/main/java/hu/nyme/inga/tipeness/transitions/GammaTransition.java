/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.nyme.inga.tipeness.transitions;

import java.util.HashMap;
import hu.nyme.inga.tipeness.simulation.ShowError;
import hu.nyme.inga.tipeness.simulation.NetState;
import umontreal.iro.lecuyer.randvar.GammaGen;

/**
 *
 * @author András Molnár
 */
public class GammaTransition extends TimedTransition {

    private GammaGen gammaGen;
    private double shape;
    private double rate;

    public GammaTransition(String name, double shape, double rate, HashMap<String, Integer> input, HashMap<String, Integer> output,
            HashMap<String, Integer> inhibitor) {
        super(name, input, output, inhibitor);
        this.shape = shape;
        this.rate = rate;
        try {
            gammaGen = new GammaGen(getRandom(), shape, rate);
        } catch (Exception e) {
            ShowError.showError(ShowError.ErrorType.wrongGammaParam, true, name);
        }
    }

    @Override
    public void generateWorkTime(NetState netState) {
        this.setRemTime(gammaGen.nextDouble());
    }

    @Override
    public String toString() {
        String nl = System.lineSeparator();
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append("Shape parameter: ").append(shape).append(nl);
        sb.append("Rate parameter: ").append(rate).append(nl);
        sb.append("-------------------------------------------------").append(nl);
        return sb.toString();
    }

    @Override
    public GammaTransition copy() {
        TimedTransition curr = super.copy();
        return new GammaTransition(curr.getName(), shape, rate, curr.getInput(), curr.getOutput(), curr.getInhibitor());
    }
}
