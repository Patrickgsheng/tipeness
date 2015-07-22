/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.nyme.inga.tipeness.transitions;

import java.util.HashMap;
import org.mvel2.MVEL;
import hu.nyme.inga.tipeness.simulation.ShowError;
import hu.nyme.inga.tipeness.simulation.NetState;

/**
 *
 * @author András Molnár
 */
public class ImmedTransition extends Transition {

    private final double priority;
    private final double weight;
    private String conditionString;

    public ImmedTransition(String name, double priority, double weight, HashMap<String, Integer> input, HashMap<String, Integer> output,
            HashMap<String, Integer> inhibitor, String condititionString) {
        super(name, input, output, inhibitor);
        this.conditionString = condititionString;
        this.priority = priority;
        this.weight = weight;
    }

    public ImmedTransition(String name, double priority, double weight, HashMap<String, Integer> input, HashMap<String, Integer> output,
            HashMap<String, Integer> inhibitor) {
        super(name, input, output, inhibitor);
        this.priority = priority;
        this.weight = weight;
    }

    public double getPriority() {
        return priority;
    }

    public double getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        String nl = System.lineSeparator();
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append("Priority: ").append(priority).append(nl);
        sb.append("Weight: ").append(weight).append(nl);
        sb.append("---------------------------------").append(nl);
        return sb.toString();
    }

    @Override
    public ImmedTransition copy() {
        Transition curr = super.copy();
        return new ImmedTransition(curr.getName(), priority, weight, curr.getInput(), curr.getOutput(), curr.getInhibitor(), conditionString);
    }

    @Override
    public boolean isEnabled(NetState netState) {
        if (!super.isEnabled(netState)) {
            return false;
        } else {
            if (conditionString != null) {
                return parsePlaceNameInCondition(netState);
            }
            return true;
        }

    }

    private boolean parsePlaceNameInCondition(NetState netState) {
        String placePatern = "#";
        String tempConString = conditionString;
        String placeName;
        try {
            while (tempConString.contains("#")) {
                placeName = tempConString.substring(tempConString.indexOf("#") + 1);
                placeName = placeName.substring(0, placeName.indexOf(placePatern));
                if (netState.getPlaces().containsKey(placeName)) {
                    tempConString = tempConString.replaceAll("#" + placeName + "#", Integer.toString(netState.getPlaces().get(placeName).getCurrent()));
                } else {
                    ShowError.showError(ShowError.ErrorType.wrongFireCondition, true, super.getName());
                }
            }
            return (boolean) MVEL.eval(tempConString);
        } catch (ClassCastException ce) {
            ShowError.showError(ShowError.ErrorType.wrongFireCondition, true, super.getName());
            return false;
        } catch (Exception e) {
            ShowError.showError(ShowError.ErrorType.wrongFireCondition, true, super.getName());
            return false;
        }
    }
}
