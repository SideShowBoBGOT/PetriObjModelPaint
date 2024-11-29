package ua.stetsenkoinna.PetriObj;

public class ArcOut {

    private final int numP;
    private final int numT;
    private final int k;

    public ArcOut(PetriT T, PetriP P, int K) {
        numP = P.getNumber();
        numT = T.getNumber();
        k = K;
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
}