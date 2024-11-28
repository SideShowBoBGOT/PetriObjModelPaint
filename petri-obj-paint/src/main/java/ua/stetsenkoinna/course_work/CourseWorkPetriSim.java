package ua.stetsenkoinna.course_work;

import ua.stetsenkoinna.PetriObj.*;
import java.util.*;

public class CourseWorkPetriSim {
    private final StateTime timeState = new StateTime();
    private double timeMin;
    private final PetriP[] listP;
    private final PetriT[] listT;
    private PetriT eventMin;
    public ArrayList<Double> timePoints = new ArrayList<>();

    public CourseWorkPetriSim(PetriNet net) {
        timeMin = Double.MAX_VALUE;
        listP = net.getListP();
        listT = net.getListT();
        eventMin = this.getEventMin();
    }

    public void go(
        final double timeModelling,

    ) {
        setSimulationTime(timeModelling);
        setTimeCurr(0);
        input();
        timePoints.add(0.0);
        while (getCurrentTime() < getSimulationTime()) {
            doStatistics();
            setTimeCurr(getTimeMin());
            timePoints.add(getTimeMin());
            output();
            input();
        }
    }

    private void eventMin() {
        PetriT event = null;
        double min = Double.MAX_VALUE;
        for (PetriT transition : listT) {
            if (transition.getMinTime() < min) {
                event = transition;
                min = transition.getMinTime();
            }
        }
        timeMin = min;
        eventMin = event;
    }

    private double getTimeMin() {
        return timeMin;
    }

    private ArrayList<PetriT> findActiveT() {
        ArrayList<PetriT> aT = new ArrayList<>();

        for (PetriT transition : listT) {
            if ((transition.condition(listP)) && (transition.getProbability() != 0)) {
                aT.add(transition);

            }
        }

        if (aT.size() > 1) {
            aT.sort((o1, o2) -> Integer.compare(o2.getPriority(), o1.getPriority()));
        }
        return aT;
    }

    private double getCurrentTime() {
        return timeState.getCurrentTime();
    }

    private void setTimeCurr(double aTimeCurr) {
        timeState.setCurrentTime(aTimeCurr);
    }

    private double getSimulationTime() {
        return timeState.getSimulationTime();
    }

    private void setSimulationTime(double aTimeMod) {
        timeState.setSimulationTime(aTimeMod);
    }

    private void input() {
        ArrayList<PetriT> activeT = this.findActiveT();
        if (activeT.isEmpty() && isBufferEmpty()) {
            timeMin = Double.MAX_VALUE;
        } else {
            while (!activeT.isEmpty()) {
                doConflict(activeT).actIn(listP, this.getCurrentTime());

                activeT = this.findActiveT();
            }
            this.eventMin();
        }
    }

    private void output() {
        if (this.getCurrentTime() <= this.getSimulationTime()) {
            eventMin.actOut(listP, this.getCurrentTime());
            if (eventMin.getBuffer() > 0) {
                boolean u = true;
                while (u) {
                    eventMin.minEvent();
                    if (eventMin.getMinTime() == this.getCurrentTime()) {
                        eventMin.actOut(listP,this.getCurrentTime());
                    } else {
                        u = false;
                    }
                }
            }
            for (PetriT transition : listT) {
                if (transition.getBuffer() > 0 && transition.getMinTime() == this.getCurrentTime()) {
                    transition.actOut(listP, this.getCurrentTime());
                    if (transition.getBuffer() > 0) {
                        boolean u = true;
                        while (u) {
                            transition.minEvent();
                            if (transition.getMinTime() == this.getCurrentTime()) {
                                transition.actOut(listP, this.getCurrentTime());
                            } else {
                                u = false;
                            }
                        }
                    }
                }
            }
        }
    }

    private void doStatistics() {
        for (PetriP position : listP) {
            position.changeMean((timeMin - this.getCurrentTime()) / getSimulationTime());
        }
        for (PetriT transition : listT) {
            transition.changeMean((timeMin - this.getCurrentTime()) / getSimulationTime());
        }
    }

    private boolean isBufferEmpty() {
        boolean c = true;
        for (PetriT e : listT) {
            if (e.getBuffer() > 0) {
                c = false;
                break;
            }
        }
        return c;
    }

    private final PetriT getEventMin() {
        this.eventMin();
        return eventMin;
    }

    private static PetriT doConflict(ArrayList<PetriT> transitions) {
        PetriT aT = transitions.get(0);
        if (transitions.size() > 1) {
            aT = transitions.get(0);
            int i = 0;
            while (i < transitions.size() && transitions.get(i).getPriority() == aT.getPriority()) {
                i++;
            }
            if (i != 1) {
                double r = Math.random();
                int j = 0;
                double sum = 0;
                double prob;
                while (j < transitions.size() && transitions.get(j).getPriority() == aT.getPriority()) {

                    if (transitions.get(j).getProbability() == 1.0) {
                        prob = 1.0 / i;
                    } else {
                        prob = transitions.get(j).getProbability();
                    }

                    sum = sum + prob;
                    if (r < sum) {
                        aT = transitions.get(j);
                        break;
                    }
                    else {
                        j++;
                    }
                }
            }
        }
        return aT;
    }

    public void printMark() {
        System.out.print(" marks: ");
        for (PetriP position : listP) {
            System.out.print(position.getMark() + "  ");
        }
        System.out.println();
    }
}
