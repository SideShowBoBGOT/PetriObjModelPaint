package ua.stetsenkoinna.LibNet;

import com.github.sh0nk.matplotlib4j.PythonExecutionException;
import ua.stetsenkoinna.PetriObj.ExceptionInvalidTimeDelay;
import ua.stetsenkoinna.PetriObj.PetriSim;

import java.io.IOException;
import java.util.ArrayList;

public class VerifyCourseWorkNet {
    public static void main(String[] args) throws ExceptionInvalidTimeDelay, PythonExecutionException, IOException {
        final CourseWorkNet courseWorkNet = new CourseWorkNet();
        final MyPetriModel model = new MyPetriModel(new PetriSim(courseWorkNet.net));
        model.isProtocolPrint = false;
        final double timeModeling = 10000;
        model.go(timeModeling);
        double totalPlaceDiskWorkTime = 0;
        double totalIoChannelWorkTime = 0;
        double totalProcessorsWorkTime = 0;

        ArrayList<Double> timeInSystemList = new ArrayList<>();
        ArrayList<Double> waitAllocateTimeList = new ArrayList<>();

        for(final CourseWorkNet.TaskObject taskObject : courseWorkNet.taskObjects) {
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
                Double.toString(CourseWorkNet.TOTAL_PAGES - courseWorkNet.pages.getMean())));
        System.out.println("Total wait allocate task: ".concat(Double.toString(courseWorkNet.total_wait_allocate_task.getMean())));
        System.out.println("Average wait allocate time: ".concat(Double.toString(calculateAverage(waitAllocateTimeList))));
        System.out.println("Standard deviation wait allocate time: ".concat(Double.toString(calculateStandardDeviation(waitAllocateTimeList))));

/*        Plot plt = Plot.create();
        plt.hist().add(timeInSystemList);
        plt.hist().add(waitAllocateTimeList);
        plt.hist().add(courseWorkNet.total_wait_allocate_task.getMarks());
        plt.show();*/
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
