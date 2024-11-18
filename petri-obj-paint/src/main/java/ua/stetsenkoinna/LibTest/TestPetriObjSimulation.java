/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ua.stetsenkoinna.LibTest;

//import PetriObj.PetriObjModel;
import ua.stetsenkoinna.LibNet.NetLibrary;
import ua.stetsenkoinna.PetriObj.*;

import java.util.ArrayList;


/**
 *
 * @author Inna V. Stetsenko
 */
public class TestPetriObjSimulation {

    public static void main(String[] args) throws ExceptionInvalidTimeDelay, ExceptionInvalidNetStructure {
        final PetriObjModel model = getModel();
        model.setIsProtokol(false);
        double timeModeling = 10000;
        model.go(timeModeling);
        for(final PetriSim petriSim : model.getListObj()) {
            final PetriNet net = petriSim.getNet();
            for(final PetriP place : net.getListP()) {
                System.out.println(place.getName().concat(", mean = ").concat(Double.toString(place.getMean())));
            }
        }
    }
      
    public static PetriObjModel getModel() throws ExceptionInvalidTimeDelay {
        final ArrayList<PetriSim> list = new ArrayList<>();
        list.add(new PetriSim(NetLibrary.create_net()));
        return new PetriObjModel(list);
    }
}
