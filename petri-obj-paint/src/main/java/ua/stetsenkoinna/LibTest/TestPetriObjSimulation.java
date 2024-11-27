/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ua.stetsenkoinna.LibTest;

//import PetriObj.PetriObjModel;
import ua.stetsenkoinna.LibNet.NetLibrary;
import ua.stetsenkoinna.PetriObj.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.github.sh0nk.matplotlib4j.*;


/**
 *
 * @author Inna V. Stetsenko
 */
public class TestPetriObjSimulation {

    public static void main(String[] args) throws ExceptionInvalidTimeDelay, PythonExecutionException, IOException {
        final ArrayList<PetriSim> list = new ArrayList<>();
        final NetLibrary.CourseWorkNet courseWorkNet = new NetLibrary.CourseWorkNet();
        list.add(new PetriSim(courseWorkNet.net));
        final PetriObjModel model = new PetriObjModel(list);
        model.setIsProtokol(false);
        final double timeModeling = 10000;
        model.go(timeModeling);
        double totalPlaceDiskWorkTime = 0;
        double totalIoChannelWorkTime = 0;
        double totalProcessorsWorkTime = 0;

        ArrayList<Double> timeInSystemList = new ArrayList<>();
        ArrayList<Double> waitAllocateTimeList = new ArrayList<>();

        for(final NetLibrary.CourseWorkNet.TaskObject taskObject : courseWorkNet.taskObjects) {
            for(int i = 0; i < taskObject.io_channel_transfer.getOutMoments().size(); i++) {
                final double timeInSystem = taskObject.io_channel_transfer.getOutMoments().get(i)
                        - taskObject.generate.getOutMoments().get(i);
                timeInSystemList.add(timeInSystem);
            }
            for(int i = 0; i < taskObject.wait_allocate.getOutMoments().size(); i++) {
                final double waitTime = taskObject.wait_allocate.getOutMoments().get(i)
                        - taskObject.fail_allocate.getOutMoments().get(i);
                waitAllocateTimeList.add(waitTime);
            }
            totalPlaceDiskWorkTime += taskObject.place_disk.getTotalTimeServ();
            totalIoChannelWorkTime += taskObject.io_channel_transfer.getTotalTimeServ();
            totalProcessorsWorkTime += taskObject.process.getTotalTimeServ();
        }
        System.out.println("Average time in system: ".concat(Double.toString(calculateAverage(timeInSystemList))));
        System.out.println("Standard deviation time in system: ".concat(Double.toString(calculateStandardDeviation(timeInSystemList))));
        System.out.println("Disk load: ".concat(Double.toString(totalPlaceDiskWorkTime / timeModeling)));
        System.out.println("Io channel load: ".concat(Double.toString(totalIoChannelWorkTime / timeModeling)));
        System.out.println("Processors load: ".concat(Double.toString(totalProcessorsWorkTime / timeModeling)));
        System.out.println("Average use of pages: ".concat(
                Double.toString(NetLibrary.CourseWorkNet.TOTAL_PAGES - courseWorkNet.pages.getMean())));
        System.out.println("Total wait allocate task: ".concat(Double.toString(courseWorkNet.total_wait_allocate_task.getMean())));
        System.out.println("Average wait allocate time: ".concat(Double.toString(calculateAverage(waitAllocateTimeList))));
        System.out.println("Standard deviation wait allocate time: ".concat(Double.toString(calculateStandardDeviation(waitAllocateTimeList))));

        Plot plt = Plot.create();
        plt.hist().add(timeInSystemList);
        plt.hist().add(waitAllocateTimeList);
        plt.hist().add(courseWorkNet.total_wait_allocate_task.getMarks());
        plt.show();
    }

    public static <T extends Number> double calculateAverage(ArrayList<T> list) {
        double sum = 0.0;
        for (T num : list) {
            sum += num.doubleValue(); // Cast to Double
        }
        return sum / list.size();
    }

    public static <T extends Number> double calculateStandardDeviation(ArrayList<T> list) {
        double mean = calculateAverage(list);
        double sumSquaredDifferences = 0.0;
        for (T num : list) {
            double difference = num.doubleValue() - mean; // Cast to Double
            sumSquaredDifferences += difference * difference;
        }
        double variance = sumSquaredDifferences / list.size();
        return Math.sqrt(variance);
    }
}
