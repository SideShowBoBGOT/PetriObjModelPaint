package ua.stetsenkoinna.PetriObj;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class for creating the transition of Petri net
 *
 *  @author Inna V. Stetsenko
 */
public class PetriT {

    private final String name;
    private int buffer;
    private int priority;
    private double probability;

    private double minTime;
    private double timeServ;
    private double totalTimeServ;
    private double parametr; //середнє значення часу обслуговування
    private double paramDeviation; //середнє квадратичне відхилення часу обслуговування
    private String distribution;

    private final ArrayList<Double> timeOut = new ArrayList<>();
    private final ArrayList<Integer> inP = new ArrayList<>();
    private final ArrayList<Integer> inPwithInf = new ArrayList<>();
    private final ArrayList<Integer> quantIn = new ArrayList<>();
    private final ArrayList<Integer> quantInwithInf = new ArrayList<>();
    private final ArrayList<Integer> outP = new ArrayList<>();
    private final ArrayList<Integer> quantOut = new ArrayList<>();

    private int num;  // номер каналу багатоканального переходу, що відповідає найближчий події
    private static int next = 0; //додано 1.10.2012
    
    private final ArrayList<Double> outMoments = new ArrayList<>();
    private boolean moments = false;
    
    public PetriT(String n, double tS) {
        name = n;
        parametr = tS;
        paramDeviation = 0;
        timeServ = parametr;
        buffer = 0;

        minTime = Double.MAX_VALUE; // не очікується вихід маркерів переходу
        num = 0;
        priority = 0;
        probability = 1.0;
        distribution = null;
        next++;
        timeOut.add(Double.MAX_VALUE); // не очікується вихід маркерів з каналів переходу
        this.minEvent();
    }

    public PetriT(String n) {
        this(n,0.0);
    }

    public static void initNext() { //ініціалізація лічильника нульовим значенням
         next = 0;
    }

    public int getPriority() {
        return priority;
    }

    public double getProbability() {
        return probability;
    }

    public int getBuffer() {
        return buffer;
    }

    public void setDistribution(String s, double param) {
        distribution = s;
        parametr = param;
        timeServ = parametr; // додано 26.12.2011, тоді, якщо s==null, то передається час обслуговування
    }

    public double getTimeServ() {
        double a = timeServ;
        if (distribution != null) {
            a = generateTimeServ();
        }
        return a;
    }

    public double generateTimeServ() {
        try {
            if (distribution != null) {
                if (distribution.equalsIgnoreCase("exp")) {
                    timeServ = FunRand.exp(parametr);
                } else if (distribution.equalsIgnoreCase("unif")) {
                    timeServ = FunRand.unif(parametr - paramDeviation, parametr + paramDeviation);
                } else if (distribution.equalsIgnoreCase("norm")) {
                    timeServ = FunRand.norm(parametr, paramDeviation);
                } else if (distribution.equalsIgnoreCase("poisson")) {
                    timeServ = FunRand.poisson(parametr);
                }
            } else {
                timeServ = parametr;
            }
        } catch (ExceptionInvalidTimeDelay ex) {
            Logger.getLogger(PetriT.class.getName()).log(Level.SEVERE, null, ex);
        }
        totalTimeServ += timeServ;
        return timeServ;
    }

    public double getMinTime() {
        this.minEvent();
        return minTime;
    }

    public boolean condition(PetriP[] pp) {
        boolean a = true;
        boolean b = true;
        for (int i = 0; i < inP.size(); i++) {
            if (pp[inP.get(i)].getMark() < quantIn.get(i)) {
                a = false;
                break;
            }
        }
        for (int i = 0; i < inPwithInf.size(); i++) {
            if (pp[inPwithInf.get(i)].getMark() < quantInwithInf.get(i)) {
                b = false;
                break;
            }
        }
        return a && b;

    }

    public void actIn(PetriP[] pp, double currentTime) {
        if (this.condition(pp)) {
            for (int i = 0; i < inP.size(); i++) {
                pp[inP.get(i)].decreaseMark(quantIn.get(i));
            }
            if (buffer == 0) {
                timeOut.set(0, currentTime + this.getTimeServ());
            } else {
                timeOut.add(currentTime + this.getTimeServ());
            }
            buffer++;
            this.minEvent();
        }
    }

    public void actOut(PetriP[] pp, double currentTime) {
        if (buffer > 0) {
            for (int j = 0; j < getOutP().size(); j++) {
                pp[getOutP().get(j)].increaseMark(quantOut.get(j));
            }
            if (num == 0 && (timeOut.size() == 1)) {
                timeOut.set(0, Double.MAX_VALUE);
            } else {
                timeOut.remove(num);
            }
            if(moments){
                outMoments.add(currentTime);
            }
            buffer--;
        }
    }

    public final void minEvent() {
        minTime = Double.MAX_VALUE;
        if (!timeOut.isEmpty()) {
            for (int i = 0; i < timeOut.size(); i++) {
                if (timeOut.get(i) < minTime) {
                    minTime = timeOut.get(i);
                    num = i;
                }
            }
        }

    }

    public ArrayList<Integer> getOutP() {
        return outP;
    }
}
