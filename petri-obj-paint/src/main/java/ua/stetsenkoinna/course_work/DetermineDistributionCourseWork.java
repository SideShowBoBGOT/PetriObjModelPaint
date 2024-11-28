package ua.stetsenkoinna.course_work;

import com.github.sh0nk.matplotlib4j.Plot;
import com.github.sh0nk.matplotlib4j.PythonExecutionException;
import com.github.sh0nk.matplotlib4j.builder.PlotBuilder;
import ua.stetsenkoinna.PetriObj.ExceptionInvalidTimeDelay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class DetermineDistributionCourseWork {

    public static void main(String[] args) throws ExceptionInvalidTimeDelay, PythonExecutionException, IOException {
        final int iterations = 3;
        final int pagesNum = 131;

        final ArrayList<ArrayList<Double>> timePointMat = new ArrayList<>();
        final ArrayList<ArrayList<Double>> meanTimeInSystemMat = new ArrayList<>();
        final ArrayList<ArrayList<Double>> meanWaitAllocateTimeMat = new ArrayList<>();
        final ArrayList<ArrayList<Double>> diskLoadMat = new ArrayList<>();
        final ArrayList<ArrayList<Double>> ioChannelLoadMat = new ArrayList<>();
        final ArrayList<ArrayList<Double>> processorsLoadMat = new ArrayList<>();
        final ArrayList<ArrayList<Double>> meanUseOfPageMat = new ArrayList<>();
        final ArrayList<ArrayList<Double>> meanTotalWaitAllocateTaskMat = new ArrayList<>();

        IntStream.range(0, iterations).forEach(iteration -> {
            final CourseWorkNet courseWorkNet;
            try {
                courseWorkNet = new CourseWorkNet(pagesNum, 2, 4, 20, 60);
            } catch (ExceptionInvalidTimeDelay e) {
                throw new RuntimeException(e);
            }

            final CourseWorkPetriSim sim = new CourseWorkPetriSim(courseWorkNet.net);

            final ArrayList<Double> timePointList = new ArrayList<>();
            final ArrayList<Double> meanTimeInSystemList = new ArrayList<>();
            final ArrayList<Double> meanWaitAllocateTimeList = new ArrayList<>();
            final ArrayList<Double> diskLoadList = new ArrayList<>();
            final ArrayList<Double> ioChannelLoadList = new ArrayList<>();
            final ArrayList<Double> processorsLoadList = new ArrayList<>();
            final ArrayList<Double> meanUseOfPageList = new ArrayList<>();
            final ArrayList<Double> meanTotalWaitAllocateTaskList = new ArrayList<>();

            final ArrayList<Integer> pagesMarkList = new ArrayList<>();
            final ArrayList<Integer> totalWaitAllocateMarkList = new ArrayList<>();

            Consumer<Double> trackStats = (currentTimeModelling) -> {

                double totalPlaceDiskWorkTime = 0;
                double totalIoChannelWorkTime = 0;
                double totalProcessorsWorkTime = 0;
                double meanTimeInSystem = 0;
                double meanWaitAllocate = 0;
                int numTimeInSystem = 0;
                int numWaitAllocate = 0;

                for (final CourseWorkNet.TaskObject taskObject : courseWorkNet.taskObjects) {
                    final double taskObjectTimeInSystemSum = calculateAverageDifferenceSum(
                            taskObject.io_channel_transfer.getOutMoments(),
                            taskObject.generate.getOutMoments()
                    );
                    meanTimeInSystem += taskObjectTimeInSystemSum;
                    numTimeInSystem += taskObject.io_channel_transfer.getOutMoments().size();

                    final double taskObjectWaitAllocateSum = calculateAverageDifferenceSum(
                            taskObject.wait_allocate.getOutMoments(),
                            taskObject.fail_allocate.getOutMoments()
                    );
                    meanWaitAllocate += taskObjectWaitAllocateSum;
                    numWaitAllocate += taskObject.wait_allocate.getOutMoments().size();

                    totalPlaceDiskWorkTime += taskObject.place_disk.getTotalTimeServ();
                    totalIoChannelWorkTime += taskObject.io_channel_transfer.getTotalTimeServ();
                    totalProcessorsWorkTime += taskObject.process.getTotalTimeServ();

                }

                meanTimeInSystem = numTimeInSystem == 0 ? 0 : meanTimeInSystem / numTimeInSystem;
                meanWaitAllocate /= numWaitAllocate == 0 ? 0 : meanWaitAllocate / numWaitAllocate;

                meanTimeInSystemList.add(meanTimeInSystem);
                meanWaitAllocateTimeList.add(meanWaitAllocate);

                timePointList.add(currentTimeModelling);
                diskLoadList.add(totalPlaceDiskWorkTime / currentTimeModelling);
                ioChannelLoadList.add(totalIoChannelWorkTime / currentTimeModelling);
                processorsLoadList.add(totalProcessorsWorkTime / currentTimeModelling);

                pagesMarkList.add(courseWorkNet.pages.getMark());
                totalWaitAllocateMarkList.add(courseWorkNet.total_wait_allocate_task.getMark());
                meanUseOfPageList.add(pagesNum - (calculateAverage(timePointList, pagesMarkList)));
                meanTotalWaitAllocateTaskList.add(calculateAverage(timePointList, totalWaitAllocateMarkList));
            };

            sim.go(40000, trackStats);
            timePointMat.add(timePointList);
            meanTimeInSystemMat.add(meanTimeInSystemList);
            meanWaitAllocateTimeMat.add(meanWaitAllocateTimeList);
            diskLoadMat.add(diskLoadList);
            ioChannelLoadMat.add(ioChannelLoadList);
            processorsLoadMat.add(processorsLoadList);
            meanUseOfPageMat.add(meanUseOfPageList);
            meanTotalWaitAllocateTaskMat.add(meanTotalWaitAllocateTaskList);
        });

        final Plot plt = Plot.create();
        PlotBuilder plotBuilder = plt.plot();
        IntStream.range(0, iterations).forEach(iteration -> {
            plotBuilder.add(timePointMat.get(iteration), meanTimeInSystemMat.get(iteration));
        });
        plt.xlabel("Time modelling");
        plt.ylabel("Mean time in system");
        plt.show();
    }

    static <T extends Number> double calculateAverage(
            final ArrayList<Double> timePointList,
            final ArrayList<T> values
    ) {
        if(timePointList.isEmpty()) {
            return 0;
        }

        double prevTimePoint = 0;
        double valueSum = 0;
        for(int i = 0; i < timePointList.size(); i++) {
            final double delay = timePointList.get(i) - prevTimePoint;
            prevTimePoint = timePointList.get(i);
            valueSum += values.get(i).doubleValue() * delay;
        }
        return valueSum / timePointList.get(timePointList.size() - 1);
    }

    static <T extends Number> double calculateAverageDifferenceSum(
        final ArrayList<Double> toMoments,
        final ArrayList<Double> fromMoments
    ) {
        return IntStream.range(0, toMoments.size())
                .mapToDouble((index) -> {
                    return toMoments.get(index) - fromMoments.get(index);
                }).reduce(Double::sum).orElse(0);
    }



}
