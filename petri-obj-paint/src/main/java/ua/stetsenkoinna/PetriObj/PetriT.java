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
    private final int number; // номер переходу за списком
    private static int next = 0; //додано 1.10.2012
    
    private final ArrayList<Double> outMoments = new ArrayList<>();
    private boolean moments = false;
    
    /**
     *
     * @param n name of transition
     * @param tS timed delay
     */
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
        number = next;
        next++;
        timeOut.add(Double.MAX_VALUE); // не очікується вихід маркерів з каналів переходу
        this.minEvent();
    }

    
    public PetriT(String n) { //changed by Inna 21.03.2018
        this(n,0.0); //parametr = 0.0
    }

    /**
     * Set the counter of transitions to zero.
     */
    public static void initNext() { //ініціалізація лічильника нульовим значенням
         next = 0;
    }

    /**
     *
     * @return the value of priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Set the new value of priority
     *
     * @param r - the new value of priority
     */
    public void setPriority(int r) {
        priority = r;
    }

    /**
     *
     * @return the value of priority
     */
    public double getProbability() {
        return probability;
    }

    /**
     * Set the new value of probability
     *
     * @param v the value of probability
     */
    public void setProbability(double v) {
        probability = v;
    }

    /**
     *
     * @return the numbers of planed moments of markers outputs
     */
    public int getBuffer() {
        return buffer;
    }

    /**
     * This method sets the distribution of service time
     *
     * @param s the name of distribution as "exp", "norm", "unif". If <i>s</i>
     * equals null then the service time is determine value
     * @param param - the mean value of service time. If s equals null then the
     * service time equals <i>param</i>.
     */
    public void setDistribution(String s, double param) {
        distribution = s;
        parametr = param;
        timeServ = parametr; // додано 26.12.2011, тоді, якщо s==null, то передається час обслуговування
    }

    /**
     *
     * @return current value of service time
     */
    public double getTimeServ() {
        double a = timeServ;
        if (distribution != null) {
            a = generateTimeServ();
        }
        return a;
    }

    public double getTotalTimeServ() {
        return totalTimeServ;
    }

    /**
     * Generating the value of service time
     *
     * @return value of service time which has been generated
     */
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

    /**
     *
     * @return the name of transition
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return the time of nearest event
     */
    public double getMinTime() {
        this.minEvent();
        return minTime;
    }

    /**
     *
     * @return the number of transition
     */
    public int getNumber() {
        return number;
    }

    /**
     * This method determines the places which is input for the transition. <br>
     * The class PetriNet use this method for creating net with given arrays of
     * places, transitions, input arcs and output arcs.
     *
     * @param inPP array of places  // не використовується методом, видалити
     * @param arcs array of input arcs
     * @throws ExceptionInvalidTimeDelay if Petri net has invalid structure
     */
    public void createInP(ArcIn[] arcs) throws ExceptionInvalidTimeDelay {
        inPwithInf.clear();    //додано 28.11.2012  список має бути порожнім!!!
        quantInwithInf.clear(); //додано 28.11.2012
        inP.clear();            //додано 28.11.2012
        quantIn.clear();        //додано 28.11.2012
        for (ArcIn arc: arcs) {
            if (arc.getNumT() == this.getNumber()) {
                if (arc.getIsInf()) {
                    inPwithInf.add(arc.getNumP());
                    quantInwithInf.add(arc.getQuantity());
                } else {
                    //if (arcs[j].getQuantity() > 0) { //вхідна позиція додається у разі позитивної кількості зв'язків, 9.11.2015
                    inP.add(arc.getNumP());
                    quantIn.add(arc.getQuantity());
                   // }
                }
            }
        }
        if (inP.isEmpty()) {
            throw new ExceptionInvalidTimeDelay("Transition " + this.getName() + " hasn't input positions!");
        }

    }

    /**
     * This method determines the places which is output for the transition.
     * <br>
     * The class PetriNet use this method for creating net with given arrays of
     * places, transitions, input arcs and output arcs.
     *
     * @param inPP array of places
     * @param arcs array of output arcs
     * @throws ExceptionInvalidTimeDelay if Petri net has invalid structure
     */
    public void createOutP(ArcOut[] arcs) throws ExceptionInvalidTimeDelay {
        getOutP().clear(); //додано 28.11.2012
        quantOut.clear();   //додано 28.11.2012
        for (ArcOut arc: arcs) {
            if ( arc.getNumT() == this.getNumber()) {
                getOutP().add(arc.getNumP());
                quantOut.add(arc.getQuantity());
            }
        }
        if (getOutP().isEmpty()) {
            throw new ExceptionInvalidTimeDelay("Transition " + this.getName() + " hasn't output positions!");
        }
    }

    /**
     * This method determines is firing condition of transition true.<br>
     * Condition is true if for each input place the quality of tokens in ....
     *
     * @param pp array of places of Petri net
     * @return true if firing condition is executed
     */
    public boolean condition(PetriP[] pp) { //Нумерація позицій тут відносна!!!  inP.get(i) - номер позиції у списку позицій, який побудований при конструюванні мережі Петрі, 

        boolean a = true;
        boolean b = true;  // Саме тому при з"єднанні спільних позицій зміна номера не призводить до трагічних наслідків (руйнування зв"язків)!!! 
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

    /**
     * The firing transition consists of two actions - tokens input and
     * output.<br>
     * This method provides tokens input in the transition.
     *
     * @param pp array of Petri net places
     * @param currentTime current time
     */
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

    /**
     * The firing transition consists of two actions - tokens input and
     * output.<br>
     * This method provides tokens output in the transition.
     *
     * @param pp array of Petri net places
     * @param currentTime current time
     */
    public void actOut(PetriP[] pp, double currentTime) {  // parameter current time ia added by Inna 11.07.2018 for protocol events
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


    /**
     * Determines the transition nearest event among the events of its tokens
     * outputs. and the number of transition channel
     */
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

    public ArrayList<Integer> getInP() {
        return inP;
    }

    /**
     *
     * @return list of transition output places
     */
    public ArrayList<Integer> getOutP() {
        return outP;
    }

    public void setParamDeviation(double parameter) {
        paramDeviation = parameter;
    }

    /**
     * @return the outMoments
     */
    public ArrayList<Double> getOutMoments() {
        return outMoments;
    }

    public void setMoments(boolean moments) {
        this.moments = moments;
    }
}
