package ua.stetsenkoinna.course_work;

import com.github.sh0nk.matplotlib4j.Plot;
import com.github.sh0nk.matplotlib4j.PythonConfig;
import com.github.sh0nk.matplotlib4j.PythonExecutionException;
import com.github.sh0nk.matplotlib4j.builder.PlotBuilder;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;
import ua.stetsenkoinna.PetriObj.ExceptionInvalidTimeDelay;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class AnalyzeCourseWorkNet {

    private static class TransitivePeriod {
        public static void main(String[] args) throws ExceptionInvalidTimeDelay, PythonExecutionException, IOException {
            final int iterations = 4;
            final int pagesNum = 131;

//            final List<List<Double>> timePointMat = new ArrayList<>();
//            final List<List<Double>> diskLoadMat = new ArrayList<>();
//            final List<List<Double>> ioChannelLoadMat = new ArrayList<>();
//            final List<List<Double>> processorsLoadMat = new ArrayList<>();
//            final List<List<Double>> meanUseOfPageMat = new ArrayList<>();
//            final List<List<Double>> stdDevUseOfPageMat = new ArrayList<>();
//            final List<List<Double>> meanTotalWaitAllocateTaskMat = new ArrayList<>();
//            final List<List<Double>> stdDevTotalWaitAllocateTaskMat = new ArrayList<>();
            final List<List<Double>> meanTimeInSystemMat = new ArrayList<>();
//            final List<List<Double>> stdDevTimeInSystemMat = new ArrayList<>();
            final List<List<Double>> meanTimeInSystemTimePointsMat = new ArrayList<>();
//            final List<List<Double>> meanWaitAllocateTimeMat = new ArrayList<>();
//            final List<List<Double>> stdDevWaitAllocateTimeMat = new ArrayList<>();
//            final List<List<Double>> meanWaitAllocateTimeTimePointsMat = new ArrayList<>();

            IntStream.range(0, iterations).forEach(iteration -> {
                final CourseWorkNet courseWorkNet;
                try {
                    courseWorkNet = new CourseWorkNet(pagesNum, 2, 4, 20, 60, 7);
                } catch (ExceptionInvalidTimeDelay e) {
                    throw new RuntimeException(e);
                }

                final CourseWorkPetriSim sim = new CourseWorkPetriSim(courseWorkNet.net);

//                final List<Double> timePointList = new ArrayList<>();
//                final List<Double> diskLoadList = new ArrayList<>();
//                final List<Double> ioChannelLoadList = new ArrayList<>();
//                final List<Double> processorsLoadList = new ArrayList<>();
//
//                final List<Double> meanUseOfPageList = new ArrayList<>();
//                final List<Double> stdDevUseOfPageList = new ArrayList<>();
//
//                final List<Double> meanTotalWaitAllocateTaskList = new ArrayList<>();
//                final List<Double> stdDevTotalWaitAllocateTaskList = new ArrayList<>();
//
//                final List<Integer> pagesMarkList = new ArrayList<>();
//                final List<Integer> totalWaitAllocateMarkList = new ArrayList<>();

                Consumer<Double> trackStats = (currentTimeModelling) -> {
//                    timePointList.add(currentTimeModelling);
//                    pagesMarkList.add(pagesNum - courseWorkNet.pages.getMark());
//                    totalWaitAllocateMarkList.add(courseWorkNet.total_wait_allocate_task.getMark());
//
//                    double totalPlaceDiskWorkTime = 0;
//                    double totalIoChannelWorkTime = 0;
//                    double totalProcessorsWorkTime = 0;
//
//                    for (final CourseWorkNet.TaskObject taskObject : courseWorkNet.taskObjects) {
//                        totalPlaceDiskWorkTime += taskObject.place_disk.getTotalTimeServ();
//                        totalIoChannelWorkTime += taskObject.io_channel_transfer.getTotalTimeServ();
//                        totalProcessorsWorkTime += taskObject.process.getTotalTimeServ();
//                    }
//
//                    final double meanUseOfPages = timePointList.size() <= 1 ? 0
//                            : calculateAverage(timePointList.stream(), pagesMarkList.stream());
//                    final double stdDevUseOfPages = timePointList.size() <= 1 ? 0
//                            : calculateStdDev(timePointList.stream(), pagesMarkList.stream(), meanUseOfPages);
//                    final double meanTotalWaitAllocateTask = timePointList.size() <= 1 ? 0
//                            : calculateAverage(timePointList.stream(), totalWaitAllocateMarkList.stream());
//                    final double stdDevTotalWaitAllocateTask = timePointList.size() <= 1 ? 0
//                            : calculateStdDev(timePointList.stream(), totalWaitAllocateMarkList.stream(), meanTotalWaitAllocateTask);
//
//                    final double diskLoad = currentTimeModelling < 0.000000001 ? 0 : (totalPlaceDiskWorkTime / currentTimeModelling);
//                    final double ioChannelLoad = currentTimeModelling < 0.000000001 ? 0 : (totalIoChannelWorkTime / currentTimeModelling);
//                    final double processorsLoad = currentTimeModelling < 0.000000001 ? 0 : (totalProcessorsWorkTime / currentTimeModelling);
//
//                    diskLoadList.add(diskLoad);
//                    ioChannelLoadList.add(ioChannelLoad);
//                    processorsLoadList.add(processorsLoad);
//
//                    meanUseOfPageList.add(meanUseOfPages);
//                    stdDevUseOfPageList.add(stdDevUseOfPages);
//                    meanTotalWaitAllocateTaskList.add(meanTotalWaitAllocateTask);
//                    stdDevTotalWaitAllocateTaskList.add(stdDevTotalWaitAllocateTask);
                };

                sim.go(60000, trackStats);

//                timePointMat.add(timePointList);
//                diskLoadMat.add(diskLoadList);
//                ioChannelLoadMat.add(ioChannelLoadList);
//                processorsLoadMat.add(processorsLoadList);
//                meanUseOfPageMat.add(meanUseOfPageList);
//                stdDevUseOfPageMat.add(stdDevUseOfPageList);
//                meanTotalWaitAllocateTaskMat.add(meanTotalWaitAllocateTaskList);
//                stdDevTotalWaitAllocateTaskMat.add(stdDevTotalWaitAllocateTaskList);

                final List<DiffTimePoint> diffTimePointsInSystem = new ArrayList<>();
//                final List<DiffTimePoint> diffTimePointsWaitAllocate = new ArrayList<>();
                for(final CourseWorkNet.TaskObject taskObject : courseWorkNet.taskObjects) {
                    updateDiffTimePointArray(
                            taskObject.io_channel_transfer.getOutMoments(),
                            taskObject.generate.getOutMoments(),
                            diffTimePointsInSystem
                    );
//                    updateDiffTimePointArray(
//                            taskObject.wait_allocate.getOutMoments(),
//                            taskObject.fail_allocate.getOutMoments(),
//                            diffTimePointsWaitAllocate
//                    );
                }
                diffTimePointsInSystem.sort((first, second) -> Double.compare(first.timePoint, second.timePoint));
//                diffTimePointsWaitAllocate.sort((first, second) -> Double.compare(first.timePoint, second.timePoint));

                final ValuesThroughTime meanTimeInSystemList = calculateMeansThroughTime(diffTimePointsInSystem);
                meanTimeInSystemMat.add(meanTimeInSystemList.values);
//                stdDevTimeInSystemMat.add(calculateStdDevsThroughTime(diffTimePointsInSystem, meanTimeInSystemList));
                meanTimeInSystemTimePointsMat.add(meanTimeInSystemList.timePoints);

//                final ValuesThroughTime meanWaitAllocateTimeList = calculateMeansThroughTime(diffTimePointsWaitAllocate);
//                meanWaitAllocateTimeMat.add(meanWaitAllocateTimeList.values);
//                stdDevWaitAllocateTimeMat.add(calculateStdDevsThroughTime(diffTimePointsWaitAllocate, meanWaitAllocateTimeList));
//                meanWaitAllocateTimeTimePointsMat.add(meanWaitAllocateTimeList.timePoints);
            });
            plot(meanTimeInSystemTimePointsMat, meanTimeInSystemMat, "Mean time in system");
//            plot(meanTimeInSystemTimePointsMat, stdDevTimeInSystemMat, "Std dev time in system");
//            plot(meanWaitAllocateTimeTimePointsMat, meanWaitAllocateTimeMat, "Mean wait allocate");
//            plot(meanWaitAllocateTimeTimePointsMat, stdDevWaitAllocateTimeMat, "Std dev wait allocate");
//            plot(timePointMat, diskLoadMat, "Disk load");
//            plot(timePointMat, ioChannelLoadMat, "Io channel load mat");
//            plot(timePointMat, processorsLoadMat, "Processors load");
//            plot(timePointMat, meanUseOfPageMat, "Mean use of pages");
//            plot(timePointMat, stdDevUseOfPageMat, "Std dev use of pages");
//            plot(timePointMat, meanTotalWaitAllocateTaskMat, "Mean total wait allocate tasks");
//            plot(timePointMat, stdDevTotalWaitAllocateTaskMat, "Std dev total wait allocate tasks");
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
            plt.savefig(yLabelName);
            plt.show();
        }
    }

    static class DetermineDistribution {
        public static void main(String[] args) throws ExceptionInvalidTimeDelay, PythonExecutionException, IOException {

            final int pagesNum = 131;
            final CourseWorkNet courseWorkNet;
            try {
                courseWorkNet = new CourseWorkNet(pagesNum, 2, 4, 20, 60, 7);
            } catch (ExceptionInvalidTimeDelay e) {
                throw new RuntimeException(e);
            }

            final CourseWorkPetriSim sim = new CourseWorkPetriSim(courseWorkNet.net);
            final Consumer<Double> trackStats = (currentTimeModelling) -> {};

            final int timeModelling = 160000;
            final int transitivePeriod = 60000;

            sim.go(timeModelling, trackStats);

            final List<DiffTimePoint> diffTimePointsInSystem = new ArrayList<>();
            for(final CourseWorkNet.TaskObject taskObject : courseWorkNet.taskObjects) {
                updateDiffTimePointArray(
                        taskObject.io_channel_transfer.getOutMoments(),
                        taskObject.generate.getOutMoments(),
                        diffTimePointsInSystem
                );
            }
            final List<DiffTimePoint> filteredDiffTimePointsInSystem = diffTimePointsInSystem.stream()
                    .filter(v -> v.timePoint > transitivePeriod)
                    .sorted((first, second) -> Double.compare(first.timePoint, second.timePoint))
                    .collect(Collectors.toList());

            final double mean = calculateAverage(
                    filteredDiffTimePointsInSystem.stream().map(v -> v.timePoint),
                    filteredDiffTimePointsInSystem.stream().map(v -> v.diff)
            );

            final double stdDev = calculateStdDev(
                    filteredDiffTimePointsInSystem.stream().map(v -> v.timePoint),
                    filteredDiffTimePointsInSystem.stream().map(v -> v.diff),
                    mean
            );

            final List<Double> data = filteredDiffTimePointsInSystem.stream()
                    .mapToDouble(v -> v.diff)
                    .boxed()
                    .collect(Collectors.toList());

//            final boolean res = performChiSquaredTest(
//                    data,
//                    mean,
//                    stdDev,
//                    0.05
//            );

            final Plot plt = Plot.create();
            plt.hist().log(false).add(data);
            plt.show();

            System.out.println(mean);
            System.out.println(stdDev);
//            System.out.println(res);
        }



//        private static int calculateBinsUsingSturges(int dataSize) {
//            if (dataSize <= 0) {
//                throw new IllegalArgumentException("Dataset size must be greater than 0.");
//            }
//            return (int) Math.ceil(log2(dataSize) + 1);
//        }
//
//        private static double log2(double x) {
//            return Math.log(x) / Math.log(2); // Using log base conversion formula
//        }
//
//        public static boolean performChiSquaredTest(
//                final List<Double> data,
//                final double mean,
//                final double stdDev,
//                final double alpha
//        ) {
//            final int numBins = calculateBinsUsingSturges(data.size());
//            // Create a normal distribution with the calculated mean and stdDev
//            NormalDistribution normalDist = new NormalDistribution(mean, stdDev);
//
//            // Determine bin boundaries
//            double min = Collections.min(data);
//            double max = Collections.max(data);
//            double binWidth = (max - min) / numBins;
//
//            // Count observed frequencies
//            int[] observedFrequencies = new int[numBins];
//            for (double value : data) {
//                int binIndex = (int) Math.min((value - min) / binWidth, numBins - 1); // Ensure the last bin is inclusive
//                observedFrequencies[binIndex]++;
//            }
//
//            // Calculate expected frequencies
//            double[] expectedFrequencies = new double[numBins];
//            for (int i = 0; i < numBins; i++) {
//                double binStart = min + i * binWidth;
//                double binEnd = binStart + binWidth;
//                expectedFrequencies[i] = (normalDist.cumulativeProbability(binEnd) - normalDist.cumulativeProbability(binStart)) * data.size();
//            }
//
//            // Calculate Chi-squared statistic
//            double chiSquared = 0.0;
//            for (int i = 0; i < numBins; i++) {
//                if (expectedFrequencies[i] > 0) {
//                    chiSquared += Math.pow(observedFrequencies[i] - expectedFrequencies[i], 2) / expectedFrequencies[i];
//                }
//            }
//
//            // Degrees of freedom = bins - 1 - parameters estimated (mean and variance)
//            int degreesOfFreedom = numBins - 1 - 2;
//
//            // Calculate critical value or p-value
//            org.apache.commons.math3.distribution.ChiSquaredDistribution chiSquaredDist =
//                    new org.apache.commons.math3.distribution.ChiSquaredDistribution(degreesOfFreedom);
//
//            double criticalValue = chiSquaredDist.inverseCumulativeProbability(1 - alpha);
//
//            // Compare Chi-squared statistic with the critical value
//            return chiSquared <= criticalValue;
//        }
    }

    static <T extends Number> double calculateAverage(
            final Stream<Double> timePointList,
            final Stream<T> values
    ) {
        return calculateAverage(timePointList.mapToDouble(Double::doubleValue), values.mapToDouble(T::doubleValue));
    }

    static double calculateAverage(final List<DiffTimePoint> list) {
        return calculateAverage(
                list.stream().map(v -> v.timePoint),
                list.stream().map(v -> v.diff)
        );
    }

    static double calculateAverage(
            final DoubleStream timePointList,
            final DoubleStream values
    ) {
        final Iterator<Double> timePointIt = timePointList.iterator();
        final Iterator<Double> valuesIt = values.iterator();

        double prevTimePoint = timePointIt.next();
        double delaySum = 0.0;
        double valueSum = 0;

        while(timePointIt.hasNext()) {
            final double timePoint = timePointIt.next();
            final double value = valuesIt.next();
            final double delay = timePoint - prevTimePoint;
            prevTimePoint = timePoint;
            delaySum += delay;
            valueSum += value * delay;
        }

        return valueSum / delaySum;
    }

    static <T extends Number> double calculateStdDev(
            final Stream<Double> timePointList,
            final Stream<T> values,
            final double mean
    ) {
        return calculateStdDev(timePointList.mapToDouble(Double::doubleValue), values.mapToDouble(T::doubleValue), mean);
    }

    static double calculateStdDev(final List<DiffTimePoint> list, final double mean) {
        return calculateStdDev(
                list.stream().map(v -> v.timePoint),
                list.stream().map(v -> v.diff),
                mean
        );
    }

    static <T extends Number> double calculateStdDev(
            final DoubleStream timePointList,
            final DoubleStream values,
            final double mean
    ) {
        final Iterator<Double> timePointIt = timePointList.iterator();
        final Iterator<Double> valuesIt = values.iterator();

        double prevTimePoint = timePointIt.next();
        double delaySum = 0.0;
        double valueSum = 0;

        while(timePointIt.hasNext()) {
            final double timePoint = timePointIt.next();
            final double value = valuesIt.next();
            final double delay = timePoint - prevTimePoint;
            delaySum += delay;
            prevTimePoint = timePoint;
            valueSum += Math.pow(value - mean, 2) * delay;
        }

        return Math.sqrt(valueSum / delaySum);
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

    static class ValuesThroughTime {
        public final List<Double> timePoints = new ArrayList<>();
        public final List<Double> values = new ArrayList<>();
    }

    static ValuesThroughTime calculateMeansThroughTime(final List<DiffTimePoint> list) {
        final Iterator<Double> timePointIt = list.stream().map(v -> v.timePoint).iterator();
        final Iterator<Double> valuesIt = list.stream().map(v -> v.diff).iterator();

        double prevTimePoint = timePointIt.next();
        double delaySum = 0.0;
        double valueSum = 0;

        final ValuesThroughTime valuesThroughTime = new ValuesThroughTime();

        while(timePointIt.hasNext()) {
            final double timePoint = timePointIt.next();
            final double value = valuesIt.next();
            final double delay = timePoint - prevTimePoint;
            prevTimePoint = timePoint;
            delaySum += delay;
            valueSum += value * delay;
            valuesThroughTime.timePoints.add(timePoint);
            valuesThroughTime.values.add(valueSum / delaySum);
        }

        return valuesThroughTime;
    }
}
