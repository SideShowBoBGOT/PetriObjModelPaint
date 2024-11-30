package ua.stetsenkoinna.PetriObj;

public class ArcIn {

    private final int numP;
    private final int numT;
    private final int k;
    boolean inf;

    public ArcIn(PetriP P, PetriT T) {
        numP = P.getNumber();
        numT = T.getNumber();
        k = 1;
        inf = false;
    }

    public ArcIn(PetriP P, PetriT T, int K) {
        numP = P.getNumber();
        numT = T.getNumber();
        k = K;
        inf = false;
    }

    public int getQuantity() {
        return k;
    }

    public int getNumP() {
        return numP;
    }

    public int getNumT() {
        return numT;
    }

    public boolean getIsInf() {
        return inf;
    }
}
