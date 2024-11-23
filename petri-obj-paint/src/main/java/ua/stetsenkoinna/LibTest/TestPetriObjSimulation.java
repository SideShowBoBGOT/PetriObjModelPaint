/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ua.stetsenkoinna.LibTest;

//import PetriObj.PetriObjModel;
import ua.stetsenkoinna.LibNet.NetLibrary;
import ua.stetsenkoinna.PetriObj.*;

import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author Inna V. Stetsenko
 */
public class TestPetriObjSimulation {

    public static void main(String[] args) throws ExceptionInvalidTimeDelay, ExceptionInvalidNetStructure {
        final ArrayList<PetriSim> list = new ArrayList<>();
        final NetLibrary.CourseWorkNet courseWorkNet = new NetLibrary.CourseWorkNet();
        list.add(new PetriSim(courseWorkNet.net));
        final PetriObjModel model = new PetriObjModel(list);
        model.setIsProtokol(false);
        double timeModeling = 10000;
        model.go(timeModeling);

        double totalTimeInSystem = 0;
        int totalFinishedTasks = 0;
        double totalPlaceDiskWorkTime = 0;
        double totalIoChannelWorkTime = 0;
        double totalProcessorsWorkTime = 0;
        double totalWaitAllocateTime = 0;
        int totalWaitAllocatedTasks = 0;
        for(final NetLibrary.CourseWorkNet.TaskObject taskObject : courseWorkNet.taskObjects) {
            for(int i = 0; i < taskObject.io_channel_transfer.actOutTimePoints.size(); i++) {
                final double timeInSystem = taskObject.io_channel_transfer.actOutTimePoints.get(i)
                        - taskObject.generate.actOutTimePoints.get(i);
                totalTimeInSystem += timeInSystem;
            }
            for(int i = 0; i < taskObject.wait_allocate.actOutTimePoints.size(); i++) {
                final double waitTime = taskObject.wait_allocate.actOutTimePoints.get(i)
                        - taskObject.fail_allocate.actOutTimePoints.get(i);
                totalWaitAllocateTime += waitTime;
            }
            totalWaitAllocatedTasks += taskObject.wait_allocate.actOutTimePoints.size();
            totalPlaceDiskWorkTime += taskObject.place_disk.getTotalTimeServ();
            totalFinishedTasks += taskObject.io_channel_transfer.actOutTimePoints.size();
            totalIoChannelWorkTime += taskObject.io_channel_transfer.getTotalTimeServ();
            totalProcessorsWorkTime += taskObject.process.getTotalTimeServ();
        }
        final double averageTimeInSystem = totalTimeInSystem / (double)totalFinishedTasks;
        System.out.println("Average time in system: ".concat(Double.toString(averageTimeInSystem)));
        System.out.println("Disk load: ".concat(Double.toString(totalPlaceDiskWorkTime / timeModeling)));
        System.out.println("Io channel load: ".concat(Double.toString(totalIoChannelWorkTime / timeModeling)));
        System.out.println("Processors load: ".concat(Double.toString(totalProcessorsWorkTime / timeModeling)));
        System.out.println("Average use of pages: ".concat(
                Double.toString(NetLibrary.CourseWorkNet.TOTAL_PAGES - courseWorkNet.pages.getMean())));
        System.out.println("Total wait allocate task: ".concat(Double.toString(courseWorkNet.total_wait_allocate_task.getMean())));
        final double averageWaitAllocate = totalWaitAllocateTime / (double)totalWaitAllocatedTasks;
        System.out.println("Average wait allocate time: ".concat(Double.toString(averageWaitAllocate)));
        /*for(final PetriSim petriSim : model.getListObj()) {
            final PetriNet net = petriSim.getNet();
            for(final PetriP place : net.getListP()) {
                System.out.println(place.getName().concat(", mean = ").concat(Double.toString(place.getMean())));
            }
        }*/


    }
}
