package ua.stetsenkoinna.PetriObj;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * This class for creating the place of Petri net.
 *
 *  @author Inna V. Stetsenko
 */
public class PetriP {

    private int mark;
    private final String name;
    private final int number;
    private double mean;
    private static int next = 0;//додано 1.10.2012, лічильник об"єктів
    private int observedMax;
    private int observedMin;

    /**
     *
     * @param n name of place
     * @param m quantity of markers
     */
    public PetriP(String n, int m) {
        name = n;
        mark = m;
        mean = 0;
        number = next; //додано 1.10.2012
        next++;
        observedMax = m;
        observedMin = m;
    }
    
     /**
     *
     * @param n - the name of place
     */
    public PetriP(String n) { //changed by Inna 21.03.2018
        this(n, 0);
        
    }

    public static void initNext() { //ініціалізація лічильника нульовим значенням
        next = 0;
    }

    /**
     * /**
     * Recalculates the mean value
     *
     * @param a value for recalculate of mean value (value equals product of
     * marking and time divided by time modeling)
     */
    public void changeMean(double a) {
        mean = mean + (mark - mean) * a;
    }

    /**
     *
     * @return mean value of quantity of markers
     */
    public double getMean() {
        return mean;
    }

    /**
     *
     * @param a value on which increase the quantity of markers
     */
    public void increaseMark(int a) {
        mark += a;
        if (observedMax < mark) {
            observedMax = mark;
        }
        if (observedMin > mark) {
            observedMin = mark;
        }

    }

    /**
     *
     * @param a value on which decrease the quantity of markers
     */
    public void decreaseMark(int a) {
        mark -= a;
        if (observedMax < mark) {
            observedMax = mark;
        }
        if (observedMin > mark) {
            observedMin = mark;
        }
    }

    /**
     *
     * @return current quantity of markers
     */
    public int getMark() {
        return mark;
    }

    public String getName() {
        return name;
    }

    /**
     *
     * @return number of the place
     */
    public int getNumber() {
        return number;
    }

}
