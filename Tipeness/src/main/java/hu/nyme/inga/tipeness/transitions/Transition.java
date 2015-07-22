/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.nyme.inga.tipeness.transitions;

import java.util.HashMap;
import hu.nyme.inga.tipeness.simulation.NetState;

/**
 *
 * @author András Molnár
 */
public class Transition {

    private final String name;

    public static enum MemoryPolicy {

        resampling, enablingMemory, ageMemory
    };

    private final HashMap<String, Integer> input;
    private final HashMap<String, Integer> inhibitor;
    private final HashMap<String, Integer> output;

    public Transition(String name, HashMap<String, Integer> input, HashMap<String, Integer> output, HashMap<String, Integer> inhibitor) {
        this.name = name;
        this.input = input;
        this.output = output;
        this.inhibitor = inhibitor;
    }

    public int getEnablingDegree(NetState netState) {
        int enablingDegree = -1;
        if (!isEnabled(netState)) {
            return 0;
        }
        int currentFire;
        for (String s : input.keySet()) {
            currentFire = netState.getPlaces().get(s).getCurrent() / input.get(s);
            if (enablingDegree == -1) {
                enablingDegree = currentFire;
            }
            if ((currentFire < enablingDegree)) {
                enablingDegree = currentFire;
            }
        }
        return enablingDegree;
    }

    public void fire(NetState netState) {
        if (isEnabled(netState)) {
            if (!input.isEmpty()) {
                for (String place : input.keySet()) {
                    netState.getPlaces().get(place).decreaseToken(input.get(place));
                }
            }
            if (!output.isEmpty()) {
                for (String place : output.keySet()) {
                    netState.getPlaces().get(place).increaseToken(output.get(place));
                }
            }
        }
    }

    public boolean isEnabled(NetState netState) {
        for (String place : inhibitor.keySet()) {
            if (inhibitor.get(place) <= netState.getPlaces().get(place).getCurrent()) {
                return false;
            }
        }
        for (String place : input.keySet()) {
            if (input.get(place) != 0 && netState.getPlaces().get(place).getCurrent() < input.get(place)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        String nl = System.lineSeparator();
        StringBuilder sb = new StringBuilder();

        sb.append("Transition name: ").append(name).append(nl);
        sb.append("Transition type: ").append(this.getClass().getSimpleName()).append(nl);
        sb.append("Input places:").append(nl);
        for (String placeName : input.keySet()) {
            sb.append(placeName).append(", ").append(input.get(placeName)).append(nl);
        }
        sb.append("Output places:").append(nl);
        for (String placeName : output.keySet()) {
            sb.append(placeName).append(", ").append(output.get(placeName)).append(nl);
        }
        return sb.toString();
    }

    public Transition copy() {
        HashMap<String, Integer> copyInput = new HashMap<>();
        HashMap<String, Integer> copyInhibitor = new HashMap<>();
        HashMap<String, Integer> copyOutput = new HashMap<>();
        for (String placeName : input.keySet()) {
            copyInput.put(placeName, input.get(placeName));
        }
        for (String placeName : inhibitor.keySet()) {
            copyInhibitor.put(placeName, inhibitor.get(placeName));
        }
        for (String s : output.keySet()) {
            copyOutput.put(s, output.get(s));
        }
        return new Transition(name, copyInput, copyOutput, copyInhibitor);
    }

    public String getName() {
        return name;
    }

    public HashMap<String, Integer> getInput() {
        return input;
    }

    public HashMap<String, Integer> getInhibitor() {
        return inhibitor;
    }

    public HashMap<String, Integer> getOutput() {
        return output;
    }
}
