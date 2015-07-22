/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.nyme.inga.tipeness.transitions;

import java.util.HashMap;
import hu.nyme.inga.tipeness.simulation.ShowError;
import hu.nyme.inga.tipeness.simulation.NetState;
import umontreal.iro.lecuyer.randvar.NormalGen;

/**
 *
 * @author András Molnár
 */
public class TruncNormalTransition extends TimedTransition {

    private NormalGen normalGen;
    private double mean;
    private double variance;

    public TruncNormalTransition(String name, double mean, double variance, HashMap<String, Integer> input, HashMap<String, Integer> output, HashMap<String, Integer> inhibitor) {
        super(name, input, output, inhibitor);
        this.mean = mean;
        this.variance = variance;

        if (mean <= 0) {
            ShowError.showError(ShowError.ErrorType.wrongNormDistParam, true, name);
        }
        try {
            this.normalGen = new NormalGen(getRandom(), mean, variance);
        } catch (Exception e) {
            ShowError.showError(ShowError.ErrorType.wrongNormDistParam, true, name);
        }
    }

    @Override
    public String toString() {
        String nl = System.lineSeparator();
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append("Mean: ").append(mean).append(nl);
        sb.append("Variance: ").append(variance).append(nl);
        sb.append("---------------------------------------").append(nl);
        return sb.toString();
    }

    @Override
    public TruncNormalTransition copy() {
        TimedTransition curr = super.copy();
        return new TruncNormalTransition(curr.getName(), mean, variance, curr.getInput(), curr.getOutput(), curr.getInhibitor());
    }

    @Override
    public void generateWorkTime(NetState netState) {
        double time = normalGen.nextDouble();
        while (time < 0) {
            time = normalGen.nextDouble();
        }
        this.setRemTime(time);
    }
}
