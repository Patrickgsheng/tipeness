/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.nyme.inga.tipeness.simulation;

import hu.nyme.inga.tipeness.transitions.Transition;
import hu.nyme.inga.tipeness.transitions.TimedTransition;
import hu.nyme.inga.tipeness.transitions.ExpTransition;
import java.util.ArrayList;
import java.util.HashMap;
import hu.nyme.inga.tipeness.statistics.Statistics;

/**
 *
 * @author András Molnár
 */
public class EventQueue {

    private final ArrayList<Event> eventQueue;

    public EventQueue() {
        this.eventQueue = new ArrayList<>();
    }

    public void updateQueue(ConfigParser configParser, NetState netState) {
        updateImmedTransitions(netState);
        if (netState.getCurrentEvent() == null) {
            for (String transitionName : configParser.getMemoryTransitions().keySet()) {
                Transition transition = netState.getMemoryTransitions().get(transitionName);
                if (transition instanceof ExpTransition) {
                    ExpTransition exponentialTransition = (ExpTransition) transition;
                    updateExpTransition(exponentialTransition, netState);
                } else {
                    TimedTransition timedTranstion = (TimedTransition) transition;
                    updateTimedTransitionWResampling(timedTranstion, netState);
                }
            }

        } else {
            String firedTransitionName = netState.getCurrentEvent().getTransitionName();
            HashMap<String, ConfigParser.MemoryPolicy> affectedTransitions = configParser.getMemoryMatrix().get(firedTransitionName);
            for (String affectedTransitionName : affectedTransitions.keySet()) {
                Transition transition = netState.getMemoryTransitions().get(affectedTransitionName);

                if (transition instanceof ExpTransition) {
                    ExpTransition exponentioalTransition = (ExpTransition) transition;
                    updateExpTransition(exponentioalTransition, netState);
                } else {
                    TimedTransition timedTransition = (TimedTransition) transition;
                    switch (affectedTransitions.get(affectedTransitionName)) {
                        case resampling:
                            updateTimedTransitionWResampling(timedTransition, netState);
                            break;
                        case enablingMemory:
                            updateTimedTransitionWEnabling(timedTransition, netState);
                            break;
                        case ageMemory:
                            updateTimedTransitionWAgeMemory(timedTransition, netState);
                            break;
                    }
                }
            }
        }

    }

    private void updateTimedTransitionWResampling(TimedTransition timedTransition, NetState netState) {
        String timedTransitionName = timedTransition.getName();
        removeEvent(getEventByTransName(timedTransitionName));
        timedTransition.generateWorkTime(netState);
        if (timedTransition.isEnabled(netState)) {
            addEvent(new Event(timedTransitionName, netState.getTime() + timedTransition.getRemTime(), netState));
        }
    }

    private void updateTimedTransitionWEnabling(TimedTransition timedTransition, NetState netState) {
        String timedTransitionName = timedTransition.getName();
        if (timedTransition.isEnabled(netState)) {
            if (getEventByTransName(timedTransitionName) == null) {
                addEvent(new Event(timedTransitionName, netState.getTime() + timedTransition.getRemTime(), netState));
            }
        } else {
            if (getEventByTransName(timedTransitionName) != null) {
                removeEvent(getEventByTransName(timedTransitionName));
                timedTransition.generateWorkTime(netState);
            }
        }
    }

    private void updateTimedTransitionWAgeMemory(TimedTransition timedTransition, NetState netState) {
        String timedTransitionName = timedTransition.getName();
        if (timedTransition.isEnabled(netState)) {
            if (getEventByTransName(timedTransitionName) == null) {
                addEvent(new Event(timedTransitionName, netState.getTime() + timedTransition.getRemTime(), netState));
            }
        } else {
            if (getEventByTransName(timedTransitionName) != null) {
                netState.getMemoryTransitions().get(timedTransitionName).setRemTime(getEventByTransName(timedTransitionName).getTime() - netState.getTime());
                removeEvent(getEventByTransName(timedTransitionName));
            }
        }

    }

    private void updateExpTransition(ExpTransition exponentialTransition, NetState netState) {
        String exponentialTransitionName = exponentialTransition.getName();
        if (!exponentialTransition.isEnabled(netState)) {
            removeEvent(getEventByTransName(exponentialTransitionName));
        } else {
            if (exponentialTransition.getsType() == ExpTransition.ServerType.exclusive) {
                if (getEventByTransName(exponentialTransitionName) == null) {
                    exponentialTransition.generateWorkTime(netState);
                    addEvent(new Event(exponentialTransitionName, netState.getTime() + exponentialTransition.getRemTime(), netState));
                }
            } else {
                if (getEventByTransName(exponentialTransitionName) == null) {
                    exponentialTransition.generateWorkTime(netState);
                    addEvent(new Event(exponentialTransitionName, netState.getTime() + exponentialTransition.getRemTime(), netState));
                } else {
                    if (getEventByTransName(exponentialTransitionName).getEnablingDegree() != exponentialTransition.getEnablingDegree(netState)) {
                        removeEvent(getEventByTransName(exponentialTransitionName));
                        exponentialTransition.generateWorkTime(netState);
                        addEvent(new Event(exponentialTransitionName, netState.getTime() + exponentialTransition.getRemTime(), netState));
                    }
                }
            }
        }
    }

    private void updateImmedTransitions(NetState netState) {
        if (!netState.getImmedTransitions().isEmpty()) {
            ArrayList<String> maxPriorityList = new ArrayList<>();
            for (String s : netState.getImmedTransitions().keySet()) {
                if (netState.getImmedTransitions().get(s).isEnabled(netState)) {
                    if (maxPriorityList.isEmpty()) {
                        maxPriorityList.add(s);
                    } else {
                        if (netState.getImmedTransitions().get(s).getPriority() > netState.getImmedTransitions().get(maxPriorityList.get(0)).getPriority()) {
                            maxPriorityList.clear();
                            maxPriorityList.add(s);
                        } else if (netState.getImmedTransitions().get(s).getPriority() == netState.getImmedTransitions().get(maxPriorityList.get(0)).
                                getPriority()) {
                            maxPriorityList.add(s);
                        }
                    }
                } else {
                    Event event = getEventByTransName(s);
                    if (event != null) {
                        removeEvent(event);
                    }
                }
            }
            if (!maxPriorityList.isEmpty()) {
                String transitionName = getRandomElementwWeight(maxPriorityList, netState);
                addEvent(new Event(transitionName, netState.getTime(), netState));
            }
        }
    }

    private void addEvent(Event newEvent) {
        if (eventQueue.isEmpty()) {
            eventQueue.add(newEvent);
        } else {
            for (int i = 0; i < eventQueue.size(); i++) {
                if (newEvent.getTime() < eventQueue.get(i).getTime()) {
                    eventQueue.add(i, newEvent);
                    return;
                }
            }
            eventQueue.add(newEvent);
        }
    }

    private void removeEvent(Event remEvent) {
        eventQueue.remove(remEvent);
    }

    public Event getNextEvent() {
        if (!eventQueue.isEmpty()) {
            return eventQueue.remove(0);
        }
        return null;
    }

    public ArrayList<Event> getEventQueue() {
        return eventQueue;
    }

    private Event getEventByTransName(String transitionName) {
        for (Event event : eventQueue) {
            if (event.getTransitionName().equals(transitionName)) {
                return event;
            }
        }
        return null;
    }

    private String getRandomElementwWeight(ArrayList<String> concurrentTransitionList, NetState netState) {
        HashMap<String, Double> minList = new HashMap<>();
        for (String transitionName : concurrentTransitionList) {
            minList.put(transitionName, netState.getImmedTransitions().get(transitionName).getWeight());
        }
        String transitionName = null;
        try {
            transitionName = Statistics.getRandomElementFromWeightedElements(minList);
        } catch (Exception e) {
            System.out.println("Error at the weightcalculation!");
        }
        return transitionName;
    }
}
