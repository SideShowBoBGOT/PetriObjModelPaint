package ua.stetsenkoinna.course_work;

import ua.stetsenkoinna.PetriObj.*;
import java.util.*;
import java.util.function.Consumer;

import ua.stetsenkoinna.PetriObj.*;
import java.util.*;
import java.util.function.Consumer;

public class CourseWorkPetriSim {
    public static void simulatePetriNet(
            PetriNet net,
            double timeModelling,
            Consumer<Double> trackStats
    ) {
        PetriP[] listP = net.getListP();
        PetriT[] listT = net.getListT();
        double currentTime = 0.0;
        double timeMin = Double.MAX_VALUE;
        PetriT eventMin = null;

        timeMin = processInput(listP, listT, currentTime, eventMin);
        eventMin = findEventMin(listT);

        while (currentTime < timeModelling) {
            trackStats.accept(currentTime);
            currentTime = timeMin;
            timeMin = processOutput(listP, listT, currentTime, timeModelling, eventMin);
            timeMin = processInput(listP, listT, currentTime, eventMin);
            eventMin = findEventMin(listT);
        }
    }

    private static PetriT findEventMin(PetriT[] listT) {
        PetriT event = null;
        double min = Double.MAX_VALUE;

        for (PetriT transition : listT) {
            if (transition.getMinTime() < min) {
                event = transition;
                min = transition.getMinTime();
            }
        }

        return event;
    }

    private static ArrayList<PetriT> findActiveTransitions(PetriP[] listP, PetriT[] listT) {
        ArrayList<PetriT> activeTransitions = new ArrayList<>();

        for (PetriT transition : listT) {
            if (transition.condition(listP) && transition.getProbability() != 0) {
                activeTransitions.add(transition);
            }
        }

        if (activeTransitions.size() > 1) {
            activeTransitions.sort((t1, t2) -> Integer.compare(t2.getPriority(), t1.getPriority()));
        }

        return activeTransitions;
    }

    private static double processInput(PetriP[] listP, PetriT[] listT, double currentTime, PetriT eventMin) {
        ArrayList<PetriT> activeTransitions = findActiveTransitions(listP, listT);
        double timeMin = Double.MAX_VALUE;

        if (activeTransitions.isEmpty() && isBufferEmpty(listT)) {
            return timeMin;
        }

        while (!activeTransitions.isEmpty()) {
            resolveConflict(activeTransitions).actIn(listP, currentTime);
            activeTransitions = findActiveTransitions(listP, listT);
        }

        for (PetriT transition : listT) {
            if (transition.getMinTime() < timeMin) {
                timeMin = transition.getMinTime();
            }
        }

        return timeMin;
    }

    private static double processOutput(PetriP[] listP, PetriT[] listT, double currentTime,
                                        double timeModelling, PetriT eventMin) {
        if (currentTime <= timeModelling) {
            eventMin.actOut(listP, currentTime);
            processBufferedEvents(eventMin, listP, currentTime);

            for (PetriT transition : listT) {
                if (transition.getBuffer() > 0 && transition.getMinTime() == currentTime) {
                    transition.actOut(listP, currentTime);
                    processBufferedEvents(transition, listP, currentTime);
                }
            }
        }

        double timeMin = Double.MAX_VALUE;
        for (PetriT transition : listT) {
            if (transition.getMinTime() < timeMin) {
                timeMin = transition.getMinTime();
            }
        }
        return timeMin;
    }

    private static void processBufferedEvents(PetriT transition, PetriP[] listP, double currentTime) {
        if (transition.getBuffer() > 0) {
            boolean hasMoreEvents = true;
            while (hasMoreEvents) {
                transition.minEvent();
                if (transition.getMinTime() == currentTime) {
                    transition.actOut(listP, currentTime);
                } else {
                    hasMoreEvents = false;
                }
            }
        }
    }

    private static boolean isBufferEmpty(PetriT[] listT) {
        for (PetriT transition : listT) {
            if (transition.getBuffer() > 0) {
                return false;
            }
        }
        return true;
    }

    private static PetriT resolveConflict(ArrayList<PetriT> transitions) {
        if (transitions.size() <= 1) {
            return transitions.get(0);
        }

        PetriT selectedTransition = transitions.get(0);
        int i = 0;
        while (i < transitions.size() &&
                transitions.get(i).getPriority() == selectedTransition.getPriority()) {
            i++;
        }

        if (i == 1) {
            return selectedTransition;
        }

        double random = Math.random();
        double sumProbability = 0.0;

        for (int j = 0; j < transitions.size() &&
                transitions.get(j).getPriority() == selectedTransition.getPriority(); j++) {

            double probability = (transitions.get(j).getProbability() == 1.0)
                    ? 1.0 / i
                    : transitions.get(j).getProbability();

            sumProbability += probability;

            if (random < sumProbability) {
                return transitions.get(j);
            }
        }

        return selectedTransition;
    }
}