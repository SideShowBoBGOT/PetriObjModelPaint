package ua.stetsenkoinna.course_work;

import com.github.sh0nk.matplotlib4j.Plot;
import com.github.sh0nk.matplotlib4j.PythonExecutionException;
import com.github.sh0nk.matplotlib4j.builder.PlotBuilder;
import ua.stetsenkoinna.PetriObj.ExceptionInvalidTimeDelay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

public class DetermineDistributionCourseWork {

    public static void main(String[] args) throws ExceptionInvalidTimeDelay, PythonExecutionException, IOException {
        final int iterations = 20;
        final double timeModelling = 10000;
        final int pagesNum = 131;

        IntStream.rangeClosed(0, iterations).forEach(iteration -> {
            final CourseWorkNet courseWorkNet;
            try {
                courseWorkNet = new CourseWorkNet(pagesNum, 2, 4, 20, 60);
            } catch (ExceptionInvalidTimeDelay e) {
                throw new RuntimeException(e);
            }

            AtomicReference<Integer> timeInSystemLastIndex = new AtomicReference<>();
            final ArrayList<Double> timeInSystemList = new ArrayList<>();

            AtomicReference<Integer> waitAllocateTimeLastIndex = new AtomicReference<>();
            final ArrayList<Double> waitAllocateTimeList = new ArrayList<>();

            final CourseWorkPetriSim sim = new CourseWorkPetriSim(courseWorkNet.net);

            Runnable trackStats = () -> {
                final AtomicReference<Double> totalPlaceDiskWorkTime = new AtomicReference<>((double) 0);
                final AtomicReference<Double> totalIoChannelWorkTime = new AtomicReference<>((double) 0);
                final AtomicReference<Double> totalProcessorsWorkTime = new AtomicReference<>((double) 0);

                for (final CourseWorkNet.TaskObject taskObject : courseWorkNet.taskObjects) {
                    for (int i = timeInSystemLastIndex.get(); i < taskObject.io_channel_transfer.getOutMoments().size(); i++) {
                        final double timeInSystem = taskObject.io_channel_transfer.getOutMoments().get(i)
                                - taskObject.generate.getOutMoments().get(i);
                        timeInSystemList.add(timeInSystem);
                    }
                    timeInSystemLastIndex.updateAndGet(v -> taskObject.io_channel_transfer.getOutMoments().size());

                    for (int i = waitAllocateTimeLastIndex.get(); i < taskObject.wait_allocate.getOutMoments().size(); i++) {
                        final double waitTime = taskObject.wait_allocate.getOutMoments().get(i)
                                - taskObject.fail_allocate.getOutMoments().get(i);
                        waitAllocateTimeList.add(waitTime);
                    }
                    waitAllocateTimeLastIndex.updateAndGet(v -> taskObject.wait_allocate.getOutMoments().size());

                    totalPlaceDiskWorkTime.updateAndGet(v -> (v + taskObject.place_disk.getTotalTimeServ()));
                    totalIoChannelWorkTime.updateAndGet(v -> (v + taskObject.io_channel_transfer.getTotalTimeServ()));
                    totalProcessorsWorkTime.updateAndGet(v -> (v + taskObject.process.getTotalTimeServ()));
                }
                final double iterationsTimeModelling = timeModelling * iterations;
                final double meanTimeInSystem = calculateAverage(timeInSystemList);
                final double meanWaitAllocateTime = calculateAverage(waitAllocateTimeList);
                final double diskLoad = totalPlaceDiskWorkTime.get() / iterationsTimeModelling;
                final double ioChannelLoad = totalIoChannelWorkTime.get() / iterationsTimeModelling;
                final double processorsLoad = totalProcessorsWorkTime.get() / iterationsTimeModelling;
                final double meanUseOfPages = pagesNum - (calculateAverage(courseWorkNet.pages.getMarks()));
                final double meanTotalWaitAllocateTasks = calculateAverage(courseWorkNet.total_wait_allocate_task.getMarks());
            };

            sim.go(timeModelling);
        });







    }

    public static <T extends Number> double calculateAverage(final ArrayList<T> list) {
        return list.stream().map(Number::doubleValue).reduce(Double::sum).get() / list.size();
    }

    public static <T extends Number> double calculateAverageMat(ArrayList<ArrayList<T>> list) {
        double sum = 0.0;
        double size = 0;
        for (final ArrayList<T> row : list) {
            for(final Number num : row) {
                sum += num.doubleValue();
            }
            size += row.size();
        }
        return sum / size;
    }

    public static <T extends Number> double calculateStandardDeviation(ArrayList<T> list, final double mean) {
        double sumSquaredDifferences = 0.0;
        for (T num : list) {
            double difference = num.doubleValue() - mean;
            sumSquaredDifferences += difference * difference;
        }
        double variance = sumSquaredDifferences / list.size();
        return Math.sqrt(variance);
    }

    public static <T extends Number> double calculateStandardDeviationMat(ArrayList<ArrayList<T>> list, final double mean) {
        double sumSquaredDifferences = 0.0;
        double size = 0;
        for (final ArrayList<T> row : list) {
            for (final T num : row) {
                double difference = num.doubleValue() - mean;
                sumSquaredDifferences += difference * difference;
            }
            size += row.size();
        }

        double variance = sumSquaredDifferences / size;
        return Math.sqrt(variance);
    }
}
