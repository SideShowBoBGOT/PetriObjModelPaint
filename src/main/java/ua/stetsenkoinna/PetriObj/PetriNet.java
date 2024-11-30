package ua.stetsenkoinna.PetriObj;

import java.util.ArrayList;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * This class provides constructing Petri net
 *
 * @author Inna V. Stetsenko
 */
public class PetriNet {

    private final PetriP[] ListP;
    private final PetriT[] ListT;

    /**
     * Construct Petri net for given set of places, set of transitions, set of
     * arcs and the name of Petri net
     *
     * @param s name of Petri net
     * @param pp set of places
     * @param TT set of transitions
     * @param In set of arcs directed from place to transition
     * @param Out set of arcs directed from transition to place
     */

    public PetriNet(String s, ArrayList<PetriP> pp, ArrayList<PetriT> TT, ArrayList<ArcIn> In, ArrayList<ArcOut> Out) throws ExceptionInvalidTimeDelay //додано 16 серпня 2011
    {//Працює прекрасно, якщо номера у списку співпадають із номерами, що присвоюються, і з номерами, які використовувались при створенні зв"язків!!!
        int numP = pp.size();
        int numT = TT.size();
        int numIn = In.size();
        int numOut = Out.size();
        ListP = new PetriP[numP];
        ListT = new PetriT[numT];
        ArcIn[] listIn = new ArcIn[numIn];
        ArcOut[] listOut = new ArcOut[numOut];

        for (int j = 0; j < numP; j++) {
            ListP[j] = pp.get(j);
        }

        for (int j = 0; j < numT; j++) {
            ListT[j] = TT.get(j);
        }

        for (int j = 0; j < numIn; j++) {
            listIn[j] = In.get(j);
        }
        for (int j = 0; j < numOut; j++) {
            listOut[j] = Out.get(j);
        }

        for (PetriT transition : ListT) {
            transition.createInP(listIn);
            transition.createOutP(listOut);
        }

    }

    /**
     *
     * @return array of Petri net places
     */
    public PetriP[] getListP() {
        return ListP;
    }

    /**
     *
     * @return array of Petri net transitions
     */
    public PetriT[] getListT() {
        return ListT;
    }
}
