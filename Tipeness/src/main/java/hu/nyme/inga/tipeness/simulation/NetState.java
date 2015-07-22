/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.nyme.inga.tipeness.simulation;

import hu.nyme.inga.tipeness.transitions.TimedTransition;
import hu.nyme.inga.tipeness.transitions.ImmedTransition;
import java.util.HashMap;

/**
 *
 * @author András Molnár
 */
public class NetState {

    private HashMap<String, Place> places;
    private HashMap<String, ImmedTransition> immedTransitions;
    private HashMap<String, TimedTransition> memoryTransitions;

    private double previousTime;
    private double time;
    private EventQueue eventqueue;
    private boolean endCurrentUnit;
    private Event currentEvent;

    public NetState(ConfigParser configParser) {
        this.time = 0.0;
        this.currentEvent = null;
        this.eventqueue = new EventQueue();

        this.places = copyPlaces(configParser);
        this.immedTransitions = copyImmedTransitions(configParser);
        this.memoryTransitions = copyMemoryTransitions(configParser);
    }

    public NetState(ConfigParser configParser, HashMap<String, Place> placeList) {
        this.time = 0.0;
        this.currentEvent = null;
        this.eventqueue = new EventQueue();
        this.places = copyPlaces(placeList);
        this.immedTransitions = copyImmedTransitions(configParser);
        this.memoryTransitions = copyMemoryTransitions(configParser);
    }

    public static HashMap<String, Place> copyPlaces(ConfigParser configParser) {
        HashMap<String, Place> copy = new HashMap<>();
        HashMap<String, Place> original = configParser.getPlaces();
        for (String placeName : original.keySet()) {
            Place currentPlace = original.get(placeName).copy();
            copy.put(currentPlace.getName(), currentPlace);
        }
        return copy;
    }

    public static HashMap<String, Place> copyPlaces(HashMap<String, Place> current) {
        HashMap<String, Place> copy = new HashMap<>();
        for (String placeName : current.keySet()) {
            Place currentPlace = current.get(placeName).copy();
            copy.put(currentPlace.getName(), currentPlace);
        }
        return copy;
    }

    public static HashMap<String, ImmedTransition> copyImmedTransitions(ConfigParser configParser) {
        HashMap<String, ImmedTransition> copy = new HashMap<>();
        HashMap<String, ImmedTransition> original = configParser.getImmedTranstions();
        for (String immedTransitionName : original.keySet()) {
            ImmedTransition curr = original.get(immedTransitionName).copy();
            copy.put(curr.getName(), curr);
        }
        return copy;
    }

    public static HashMap<String, TimedTransition> copyMemoryTransitions(ConfigParser configParser) {
        HashMap<String, TimedTransition> copy = new HashMap<>();
        HashMap<String, TimedTransition> original = configParser.getMemoryTransitions();
        for (String timedTransitionName : original.keySet()) {
            TimedTransition curr = original.get(timedTransitionName).copy();
            copy.put(curr.getName(), curr);
        }
        return copy;
    }

    public HashMap<String, Place> getPlaces() {
        return places;
    }

    public EventQueue getEventqueue() {
        return eventqueue;
    }

    public Event getCurrentEvent() {
        return currentEvent;
    }

    public void setCurrentEvent(Event currentEvent) {
        this.currentEvent = currentEvent;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public HashMap<String, ImmedTransition> getImmedTransitions() {
        return immedTransitions;
    }

    public HashMap<String, TimedTransition> getMemoryTransitions() {
        return memoryTransitions;
    }

    public double getTime() {
        return time;
    }

    public double getPreviousTime() {
        return previousTime;
    }

    public boolean isEndCurrentUnit() {
        return endCurrentUnit;
    }

    public void setPreviousTime(double previousTime) {
        this.previousTime = previousTime;
    }

}
