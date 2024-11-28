package ua.stetsenkoinna.course_work;

import com.github.sh0nk.matplotlib4j.Plot;
import com.github.sh0nk.matplotlib4j.PythonExecutionException;
import com.github.sh0nk.matplotlib4j.builder.PlotBuilder;
import ua.stetsenkoinna.PetriObj.ExceptionInvalidTimeDelay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DetermineDistributionCourseWork {

    public static void main(String[] args) throws ExceptionInvalidTimeDelay, PythonExecutionException, IOException {
        final int iterations = 4;
        final int pagesNum = 131;

        final List<List<Double>> timePointMat = new ArrayList<>();
        final List<List<Double>> diskLoadMat = new ArrayList<>();
        final List<List<Double>> ioChannelLoadMat = new ArrayList<>();
        final List<List<Double>> processorsLoadMat = new ArrayList<>();

        final List<List<Double>> meanUseOfPageMat = new ArrayList<>();
        final List<List<Double>> stdDevUseOfPageMat = new ArrayList<>();

        final List<List<Double>> meanTotalWaitAllocateTaskMat = new ArrayList<>();
        final List<List<Double>> stdDevTotalWaitAllocateTaskMat = new ArrayList<>();

        final List<List<Double>> meanTimeInSystemMat = new ArrayList<>();
        final List<List<Double>> stdDevTimeInSystemMat = new ArrayList<>();
        final List<List<Double>> meanTimeInSystemTimePointsMat = new ArrayList<>();

        final List<List<Double>> meanWaitAllocateTimeMat = new ArrayList<>();
        final List<List<Double>> stdDevWaitAllocateTimeMat = new ArrayList<>();
        final List<List<Double>> meanWaitAllocateTimeTimePointsMat = new ArrayList<>();

        IntStream.range(0, iterations).forEach(iteration -> {
            final CourseWorkNet courseWorkNet;
            try {
                courseWorkNet = new CourseWorkNet(pagesNum, 2, 4, 20, 60, 7);
            } catch (ExceptionInvalidTimeDelay e) {
                throw new RuntimeException(e);
            }

            final CourseWorkPetriSim sim = new CourseWorkPetriSim(courseWorkNet.net);

            final List<Double> timePointList = new ArrayList<>();
            final List<Double> diskLoadList = new ArrayList<>();
            final List<Double> ioChannelLoadList = new ArrayList<>();
            final List<Double> processorsLoadList = new ArrayList<>();

            final List<Double> meanUseOfPageList = new ArrayList<>();
            final List<Double> stdDevUseOfPageList = new ArrayList<>();

            final List<Double> meanTotalWaitAllocateTaskList = new ArrayList<>();
            final List<Double> stdDevTotalWaitAllocateTaskList = new ArrayList<>();

            final List<Integer> pagesMarkList = new ArrayList<>();
            final List<Integer> totalWaitAllocateMarkList = new ArrayList<>();

            Consumer<Double> trackStats = (currentTimeModelling) -> {
                timePointList.add(currentTimeModelling);
                pagesMarkList.add(pagesNum - courseWorkNet.pages.getMark());
                totalWaitAllocateMarkList.add(courseWorkNet.total_wait_allocate_task.getMark());

                double totalPlaceDiskWorkTime = 0;
                double totalIoChannelWorkTime = 0;
                double totalProcessorsWorkTime = 0;

                for (final CourseWorkNet.TaskObject taskObject : courseWorkNet.taskObjects) {
                    totalPlaceDiskWorkTime += taskObject.place_disk.getTotalTimeServ();
                    totalIoChannelWorkTime += taskObject.io_channel_transfer.getTotalTimeServ();
                    totalProcessorsWorkTime += taskObject.process.getTotalTimeServ();
                }

                final double meanUseOfPages = timePointList.size() <= 1 ? 0
                        : calculateAverage(timePointList, pagesMarkList);
                final double stdDevUseOfPages = timePointList.size() <= 1 ? 0
                        : calculateStdDev(timePointList, pagesMarkList, meanUseOfPages);
                final double meanTotalWaitAllocateTask = timePointList.size() <= 1 ? 0
                        : calculateAverage(timePointList, totalWaitAllocateMarkList);
                final double stdDevTotalWaitAllocateTask = timePointList.size() <= 1 ? 0
                        : calculateStdDev(timePointList, totalWaitAllocateMarkList, meanTotalWaitAllocateTask);

                final double diskLoad = currentTimeModelling < 0.000000001 ? 0 : (totalPlaceDiskWorkTime / currentTimeModelling);
                final double ioChannelLoad = currentTimeModelling < 0.000000001 ? 0 : (totalIoChannelWorkTime / currentTimeModelling);
                final double processorsLoad = currentTimeModelling < 0.000000001 ? 0 : (totalProcessorsWorkTime / currentTimeModelling);

                diskLoadList.add(diskLoad);
                ioChannelLoadList.add(ioChannelLoad);
                processorsLoadList.add(processorsLoad);

                meanUseOfPageList.add(meanUseOfPages);
                stdDevUseOfPageList.add(stdDevUseOfPages);
                meanTotalWaitAllocateTaskList.add(meanTotalWaitAllocateTask);
                stdDevTotalWaitAllocateTaskList.add(stdDevTotalWaitAllocateTask);
            };

            sim.go(10000, trackStats);

            timePointMat.add(timePointList);
            diskLoadMat.add(diskLoadList);
            ioChannelLoadMat.add(ioChannelLoadList);
            processorsLoadMat.add(processorsLoadList);
            meanUseOfPageMat.add(meanUseOfPageList);
            stdDevUseOfPageMat.add(stdDevUseOfPageList);
            meanTotalWaitAllocateTaskMat.add(meanTotalWaitAllocateTaskList);
            stdDevTotalWaitAllocateTaskMat.add(stdDevTotalWaitAllocateTaskList);

            final List<DiffTimePoint> diffTimePointsInSystem = new ArrayList<>();
            final List<DiffTimePoint> diffTimePointsWaitAllocate = new ArrayList<>();
            for(final CourseWorkNet.TaskObject taskObject : courseWorkNet.taskObjects) {
                updateDiffTimePointArray(
                        taskObject.io_channel_transfer.getOutMoments(),
                        taskObject.generate.getOutMoments(),
                        diffTimePointsInSystem
                );
                updateDiffTimePointArray(
                        taskObject.wait_allocate.getOutMoments(),
                        taskObject.fail_allocate.getOutMoments(),
                        diffTimePointsWaitAllocate
                );
            }
            diffTimePointsInSystem.sort((first, second) -> Double.compare(first.timePoint, second.timePoint));
            diffTimePointsWaitAllocate.sort((first, second) -> Double.compare(first.timePoint, second.timePoint));

            final List<Double> meanTimeInSystemList = calculateMeansThroughTime(diffTimePointsInSystem);
            meanTimeInSystemMat.add(meanTimeInSystemList);
            stdDevTimeInSystemMat.add(calculateStdDevsThroughTime(diffTimePointsInSystem, meanTimeInSystemList));
            meanTimeInSystemTimePointsMat.add(diffTimePointsInSystem.stream().mapToDouble(v -> v.timePoint).boxed().collect(Collectors.toList()));

            final List<Double> meanWaitAllocateTimeList = calculateMeansThroughTime(diffTimePointsWaitAllocate);
            meanWaitAllocateTimeMat.add(meanWaitAllocateTimeList);
            stdDevWaitAllocateTimeMat.add(calculateStdDevsThroughTime(diffTimePointsWaitAllocate, meanWaitAllocateTimeList));
            meanWaitAllocateTimeTimePointsMat.add(diffTimePointsWaitAllocate.stream().mapToDouble(v -> v.timePoint).boxed().collect(Collectors.toList()));
        });
        plot(meanTimeInSystemTimePointsMat, meanTimeInSystemMat, "Mean time in system");
        plot(meanTimeInSystemTimePointsMat, stdDevTimeInSystemMat, "Std dev time in system");
        plot(meanWaitAllocateTimeTimePointsMat, meanWaitAllocateTimeMat, "Mean wait allocate");
        plot(meanWaitAllocateTimeTimePointsMat, stdDevWaitAllocateTimeMat, "Std dev wait allocate");
        plot(timePointMat, diskLoadMat, "Disk load");
        plot(timePointMat, ioChannelLoadMat, "Io channel load mat");
        plot(timePointMat, processorsLoadMat, "Processors load");
        plot(timePointMat, meanUseOfPageMat, "Mean use of pages");
        plot(timePointMat, stdDevUseOfPageMat, "Std dev use of pages");
        plot(timePointMat, meanTotalWaitAllocateTaskMat, "Mean total wait allocate tasks");
        plot(timePointMat, stdDevTotalWaitAllocateTaskMat, "Std dev total wait allocate tasks");
    }

    static void plot(
            final List<List<Double>> timePointMat,
            final List<List<Double>> valueMat,
            final String yLabelName
    ) throws PythonExecutionException, IOException {
        final Plot plt = Plot.create();
        final PlotBuilder plotBuilder = plt.plot();
        for(int i = 0; i < timePointMat.size(); i++) {
            plotBuilder.add(timePointMat.get(i), valueMat.get(i));
        }
        plt.xlabel("Time modelling");
        plt.ylabel(yLabelName);
        plt.show();
    }

    static <T extends Number> double calculateAverage(
            final List<Double> timePointList,
            final List<T> values
    ) {
        double prevTimePoint = 0;
        double valueSum = 0;
        for(int i = 0; i < timePointList.size(); i++) {
            final double delay = timePointList.get(i) - prevTimePoint;
            prevTimePoint = timePointList.get(i);
            valueSum += values.get(i).doubleValue() * delay;
        }
        return valueSum / timePointList.get(timePointList.size() - 1);
    }

    static <T extends Number> double calculateStdDev(
            final List<Double> timePointList,
            final List<T> values,
            final double mean
    ) {
        double prevTimePoint = 0;
        double valueSum = 0;
        for(int i = 0; i < timePointList.size(); i++) {
            final double delay = timePointList.get(i) - prevTimePoint;
            prevTimePoint = timePointList.get(i);
            valueSum += Math.pow(values.get(i).doubleValue() - mean, 2) * delay;
        }
        return Math.sqrt(valueSum / timePointList.get(timePointList.size() - 1));
    }

    static private class DiffTimePoint {
        public double diff;
        public double timePoint;
    }

    static void updateDiffTimePointArray(
            final List<Double> toPoints,
            final List<Double> fromPoints,
            final List<DiffTimePoint> allPoints
    ) {
        for(int i = 0; i < toPoints.size(); i++) {
            final DiffTimePoint diffTimePoint = new DiffTimePoint();
            diffTimePoint.diff = toPoints.get(i) - fromPoints.get(i);
            diffTimePoint.timePoint = toPoints.get(i);
            allPoints.add(diffTimePoint);
        }
    }

    static List<Double> calculateMeansThroughTime(final List<DiffTimePoint> diffTimePoints) {
        final List<Double> means = new ArrayList<>();
        double valueDelayProductSum = 0;
        double prevTimePoint = 0;
        for(final DiffTimePoint diffTimePoint : diffTimePoints) {
            final double delay = diffTimePoint.timePoint - prevTimePoint;
            prevTimePoint = diffTimePoint.timePoint;
            valueDelayProductSum += diffTimePoint.diff * delay;
            means.add(valueDelayProductSum / diffTimePoint.timePoint);
        }
        return means;
    }

    static List<Double> calculateStdDevsThroughTime(
            final List<DiffTimePoint> diffTimePoints,
            final List<Double> means
    ) {
        final List<Double> stdDevs = new ArrayList<>();
        double sum = 0;
        double prevTimePoint = 0;
        for(int i = 0; i < diffTimePoints.size(); i++) {
            final DiffTimePoint diffTimePoint = diffTimePoints.get(i);
            final double delay = diffTimePoint.timePoint - prevTimePoint;
            prevTimePoint = diffTimePoint.timePoint;
            sum += Math.pow(diffTimePoint.diff - means.get(i), 2) * delay;
            stdDevs.add(Math.sqrt(sum / diffTimePoint.timePoint));
        }
        return stdDevs;
    }
}
