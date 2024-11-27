package ua.stetsenkoinna.LibNet;

import ua.stetsenkoinna.PetriObj.PetriSim;
import ua.stetsenkoinna.PetriObj.StateTime;

public class MyPetriModel {

    public boolean isProtocolPrint = true;
    public boolean isStatistics = true;
    private final PetriSim sim;
    private final StateTime timeState = new StateTime();

    public MyPetriModel(final PetriSim sim) {
        this.sim = sim;
        sim.setTimeState(timeState);
    }

    public void go(final double timeModeling) {
        this.setSimulationTime(timeModeling);
        this.setCurrentTime(0);
        sim.input();

        while (this.getCurrentTime() < this.getSimulationTime()) {
            final double min = sim.getTimeMin();
            if (isStatistics) {
                if (min > 0) {
                    if (min < this.getSimulationTime())
                        sim.doStatistics((min - this.getCurrentTime()) / min);
                    else
                        sim.doStatistics((this.getSimulationTime() - this.getCurrentTime()) / this.getSimulationTime());
                }
            }

            this.setCurrentTime(min);
            if (this.getCurrentTime() <= this.getSimulationTime()) {

                if (isProtocolPrint) {
                    System.out.println(" Selected object  " + sim.getName() + "\n" + " NextEvent " + "\n");
                }
                if (isProtocolPrint) {
                    System.out.println(" time =   " + this.getCurrentTime() + "   Event '" + sim.getEventMin().getName() + "'\n"
                            + "                       is occuring for the object   " + sim.getName() + "\n");
                }
                sim.doT();
                sim.output();
                sim.input();
            }
        }
    }

    public void setCurrentTime(double t) {
        timeState.setCurrentTime(t);
        sim.setTimeCurr(t);
    }

    public double getCurrentTime() {
        return timeState.getCurrentTime();
    }

    public void setSimulationTime(double t) {
        timeState.setSimulationTime(t);
        sim.setSimulationTime(t);
    }

    public double getSimulationTime() {
        return timeState.getSimulationTime();
    }
}