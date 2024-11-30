package ua.stetsenkoinna.course_work;

import com.github.sh0nk.matplotlib4j.Plot;
import com.github.sh0nk.matplotlib4j.PythonExecutionException;
import com.github.sh0nk.matplotlib4j.builder.PlotBuilder;
import ua.stetsenkoinna.PetriObj.ExceptionInvalidTimeDelay;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class AnalyzeCourseWorkNet {

    static class DiffTimePoint {
        public double diff;
        public double timePoint;
    }

    static class PropertyStats {
        List<Double> timePoint = new ArrayList<>();
        List<Double> diskLoad = new ArrayList<>();
        List<Double> ioChannelLoad = new ArrayList<>();
        List<Double> processorsLoad = new ArrayList<>();
        List<Double> useOfPage = new ArrayList<>();
        List<Double> totalWaitAllocate = new ArrayList<>();
        List<DiffTimePoint> timeInSystem = new ArrayList<>();
        List<DiffTimePoint> timeWaitAllocate = new ArrayList<>();
    }

    static class PropertyMeanStdDev {
        List<Double> means = new ArrayList<>();
        List<Double> stdDev = new ArrayList<>();
    }

    static void sortDiffTimePointArray(final List<DiffTimePoint> diffTimePointArray) {
        diffTimePointArray.sort(Comparator.comparingDouble(v -> v.timePoint));
    }

    static PropertyStats collectStats(
        final double timeModelling,
        final double collectStatsStartingAtTime,
        final int pagesNum,
        final int processorsNum,
        final int diskNum,
        final int pagesStart,
        final int pagesEnd,
        final double tasksTimeMean
    ) {
        final CourseWorkNet courseWorkNet;
        try {
            courseWorkNet = new CourseWorkNet(pagesNum, processorsNum, diskNum, pagesStart, pagesEnd, tasksTimeMean);
        } catch (ExceptionInvalidTimeDelay e) {
            throw new RuntimeException(e);
        }

        final CourseWorkPetriSim sim = new CourseWorkPetriSim(courseWorkNet.net);

        final PropertyStats propertyStats = new PropertyStats();

        Consumer<Double> trackStats = (currentTimeModelling) -> {
            if(currentTimeModelling > collectStatsStartingAtTime) {
                propertyStats.timePoint.add(currentTimeModelling);
                propertyStats.useOfPage.add((double)(pagesNum - courseWorkNet.pages.getMark()));
                propertyStats.totalWaitAllocate.add((double)(courseWorkNet.total_wait_allocate_task.getMark()));

                double totalPlaceDiskWorkTime = 0;
                double totalIoChannelWorkTime = 0;
                double totalProcessorsWorkTime = 0;

                for (final CourseWorkNet.TaskObject taskObject : courseWorkNet.taskObjects) {
                    totalPlaceDiskWorkTime += taskObject.place_disk.getTotalTimeServ();
                    totalIoChannelWorkTime += taskObject.io_channel_transfer.getTotalTimeServ();
                    totalProcessorsWorkTime += taskObject.process.getTotalTimeServ();
                }

                final double diskLoad = currentTimeModelling < 0.000000001 ? 0 : (totalPlaceDiskWorkTime / currentTimeModelling);
                final double ioChannelLoad = currentTimeModelling < 0.000000001 ? 0 : (totalIoChannelWorkTime / currentTimeModelling);
                final double processorsLoad = currentTimeModelling < 0.000000001 ? 0 : (totalProcessorsWorkTime / currentTimeModelling);

                propertyStats.diskLoad.add(diskLoad);
                propertyStats.ioChannelLoad.add(ioChannelLoad);
                propertyStats.processorsLoad.add(processorsLoad);
            }
        };

        sim.go(timeModelling, trackStats);

        for(final CourseWorkNet.TaskObject taskObject : courseWorkNet.taskObjects) {
            updateDiffTimePointArray(
                    collectStatsStartingAtTime,
                    taskObject.io_channel_transfer.getOutMoments(),
                    taskObject.generate.getOutMoments(),
                    propertyStats.timeInSystem
            );
            updateDiffTimePointArray(
                    collectStatsStartingAtTime,
                    taskObject.wait_allocate.getOutMoments(),
                    taskObject.fail_allocate.getOutMoments(),
                    propertyStats.timeWaitAllocate
            );
        }
        sortDiffTimePointArray(propertyStats.timeInSystem);
        sortDiffTimePointArray(propertyStats.timeWaitAllocate);

        return propertyStats;
    }

    private static PropertyMeanStdDev calcMeanStdDevThroughTime(
            final List<Double> timePoints,
            final List<Double> props
    ) {
        return calcMeanStdDevThroughTime(
                () -> timePoints.stream().mapToDouble(Double::doubleValue),
                () -> props.stream().mapToDouble(Double::doubleValue)
        );
    }

    private static PropertyMeanStdDev calcMeanStdDevThroughTime(
            final List<DiffTimePoint> timeProps
    ) {
        return calcMeanStdDevThroughTime(
                () -> timeProps.stream().mapToDouble(v -> v.timePoint),
                () -> timeProps.stream().mapToDouble(v -> v.diff)
        );
    }

    private static PropertyMeanStdDev calcMeanStdDevThroughTime(
            final Supplier<DoubleStream> timePoints,
            final Supplier<DoubleStream> props
    ) {
        final PropertyMeanStdDev propertyMeanStdDev = new PropertyMeanStdDev();
        propertyMeanStdDev.means = calculateMeansThroughTime(timePoints.get(), props.get());
        propertyMeanStdDev.stdDev = calculateStdDevsThroughTime(timePoints.get(), props.get(), propertyMeanStdDev.means);
        return propertyMeanStdDev;
    }

    private static class TransitivePeriod {
        public static void main(String[] args) throws ExceptionInvalidTimeDelay, PythonExecutionException, IOException {
            final int iterations = 2;
            final List<List<Double>> timePointMat = new ArrayList<>();
            final List<PropertyMeanStdDev> diskLoadMat = new ArrayList<>();
            final List<PropertyMeanStdDev> ioChannelLoadMat = new ArrayList<>();
            final List<PropertyMeanStdDev> processorsLoadMat = new ArrayList<>();
            final List<PropertyMeanStdDev> useOfPageMat = new ArrayList<>();
            final List<PropertyMeanStdDev> totalWaitAllocateMat = new ArrayList<>();
            final List<PropertyMeanStdDev> meanTimeInSystem = new ArrayList<>();
            final List<List<Double>> meanTimeInSystemTimePointsMat = new ArrayList<>();
            final List<PropertyMeanStdDev> waitAllocate = new ArrayList<>();
            final List<List<Double>> waitAllocateTimePointsMat = new ArrayList<>();

            IntStream.range(0, iterations).forEach(iteration -> {
                final PropertyStats propertyStats = collectStats(
                        400000,
                        0,
                        131,
                        2,
                        4,
                        20,
                        60,
                        7
                );
                diskLoadMat.add(calcMeanStdDevThroughTime(propertyStats.timePoint, propertyStats.diskLoad));
                ioChannelLoadMat.add(calcMeanStdDevThroughTime(propertyStats.timePoint, propertyStats.ioChannelLoad));
                processorsLoadMat.add(calcMeanStdDevThroughTime(propertyStats.timePoint, propertyStats.processorsLoad));
                useOfPageMat.add(calcMeanStdDevThroughTime(propertyStats.timePoint, propertyStats.useOfPage));
                totalWaitAllocateMat.add(calcMeanStdDevThroughTime(propertyStats.timePoint, propertyStats.totalWaitAllocate));
                meanTimeInSystem.add(calcMeanStdDevThroughTime(propertyStats.timeInSystem));
                waitAllocate.add(calcMeanStdDevThroughTime(propertyStats.timeWaitAllocate));
                propertyStats.timePoint.remove(propertyStats.timePoint.size() - 1);
                timePointMat.add(propertyStats.timePoint);
                {
                    final List<Double> timeInSystemTimePoints = propertyStats.timeInSystem.stream()
                            .mapToDouble(v -> v.timePoint).boxed().collect(Collectors.toList());
                    timeInSystemTimePoints.remove(timeInSystemTimePoints.size() - 1);
                    meanTimeInSystemTimePointsMat.add(timeInSystemTimePoints);
                }
                {
                    final List<Double> waitAllocateTimePoints = propertyStats.timeWaitAllocate.stream()
                            .mapToDouble(v -> v.timePoint).boxed().collect(Collectors.toList());
                    waitAllocateTimePoints.remove(waitAllocateTimePoints.size() - 1);
                    waitAllocateTimePointsMat.add(waitAllocateTimePoints);
                }
            });
            plotTransitiveMeanPeriod(meanTimeInSystemTimePointsMat, meanTimeInSystem, "MeanTimeInSystem");
            plotTransitiveStdDevPeriod(meanTimeInSystemTimePointsMat, meanTimeInSystem, "StdDevTimeInSystem");

            plotTransitiveMeanPeriod(waitAllocateTimePointsMat, waitAllocate, "MeanWaitAllocate");
            plotTransitiveStdDevPeriod(waitAllocateTimePointsMat, waitAllocate, "StdDevWaitAllocate");

            plotTransitiveMeanPeriod(timePointMat, diskLoadMat, "MeanDiskLoad");
            plotTransitiveStdDevPeriod(timePointMat, diskLoadMat, "StdDevDiskLoad");

            plotTransitiveMeanPeriod(timePointMat, ioChannelLoadMat, "MeanIoChannelLoad");
            plotTransitiveStdDevPeriod(timePointMat, ioChannelLoadMat, "StdDevIoChannelLoad");

            plotTransitiveMeanPeriod(timePointMat, processorsLoadMat, "MeanProcessorsLoad");
            plotTransitiveStdDevPeriod(timePointMat, processorsLoadMat, "StdDevProcessorsLoad");

            plotTransitiveMeanPeriod(timePointMat, useOfPageMat, "MeanUseOfPages");
            plotTransitiveStdDevPeriod(timePointMat, useOfPageMat, "StdDevUseOfPages");

            plotTransitiveMeanPeriod(timePointMat, totalWaitAllocateMat, "MeanTotalWaitAllocateTasks");
            plotTransitiveStdDevPeriod(timePointMat, totalWaitAllocateMat, "StdDevTotalWaitAllocateTasks");
        }
        static void plotTransitiveMeanPeriod(
                final List<List<Double>> timePointMat,
                final List<PropertyMeanStdDev> valueMat,
                final String yLabelName
        ) throws PythonExecutionException, IOException {
            plotTransitivePeriod(timePointMat, valueMat.stream().map(v -> v.means), yLabelName);
        }

        static void plotTransitiveStdDevPeriod(
                final List<List<Double>> timePointMat,
                final List<PropertyMeanStdDev> valueMat,
                final String yLabelName
        ) throws PythonExecutionException, IOException {
            plotTransitivePeriod(timePointMat, valueMat.stream().map(v -> v.stdDev), yLabelName);
        }

        static void plotTransitivePeriod(
                final List<List<Double>> timePointMat,
                final Stream<List<Double>> valueMat,
                final String yLabelName
        ) throws PythonExecutionException, IOException {
            final Plot plt = Plot.create();
            final PlotBuilder plotBuilder = plt.plot();
            final Iterator<List<Double>> timePointIt = timePointMat.iterator();
            final Iterator<List<Double>> valueIt = valueMat.iterator();
            while(timePointIt.hasNext()) {
                final List<Double> timePointRow = timePointIt.next();
                final List<Double> valueRow = valueIt.next();
                plotBuilder.add(timePointRow, valueRow);
            }
            plt.xlabel("Time modelling");
            plt.ylabel(yLabelName);
            plt.savefig(yLabelName);
            plt.show();
        }
    }

    static class DetermineDistribution {
        public static void main(String[] args) throws ExceptionInvalidTimeDelay, PythonExecutionException, IOException {

            final PropertyStats propertyStats = collectStats(
                    500000,
                    0,
                    131,
                    2,
                    4,
                    20,
                    60,
                    7
            );

            try (BufferedWriter writer = new BufferedWriter(new FileWriter("commonProps.csv"))) {
                writer.write("timePoint"
                        + "," + "diskLoad"
                        + "," + "ioChannelLoad"
                        + "," + "processorsLoad"
                        + "," + "totalWaitAllocate"
                        + "," + "useOfPage"
                );
                writer.newLine();
                for(int i = 0; i < propertyStats.timePoint.size(); i++) {
                    writer.write(propertyStats.timePoint.get(i)
                            + "," + propertyStats.diskLoad.get(i)
                            + "," + propertyStats.ioChannelLoad.get(i)
                            + "," + propertyStats.processorsLoad.get(i)
                            + "," + propertyStats.totalWaitAllocate.get(i)
                            + "," + propertyStats.useOfPage.get(i)
                    );
                    writer.newLine();
                }
            } catch (IOException e) {
                System.err.println("Error writing to file: " + e.getMessage());
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter("timeInSystem.csv"))) {
                writer.write("timePoint,value");
                writer.newLine();
                for(final DiffTimePoint diffTimePoint : propertyStats.timeInSystem) {
                    writer.write(diffTimePoint.timePoint + "," + diffTimePoint.diff);
                    writer.newLine();
                }
            } catch (IOException e) {
                System.err.println("Error writing to file: " + e.getMessage());
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter("waitAllocate.csv"))) {
                writer.write("timePoint,value");
                writer.newLine();
                for(final DiffTimePoint diffTimePoint : propertyStats.timeWaitAllocate) {
                    writer.write(diffTimePoint.timePoint + "," + diffTimePoint.diff);
                    writer.newLine();
                }
            } catch (IOException e) {
                System.err.println("Error writing to file: " + e.getMessage());
            }
        }
    }

    static void updateDiffTimePointArray(
            final double collectStatsStartingAtTimePoint,
            final List<Double> toPoints,
            final List<Double> fromPoints,
            final List<DiffTimePoint> diffTimePoints
    ) {
        for(int i = 0; i < toPoints.size(); i++) {
            final double point = toPoints.get(i);
            if(point > collectStatsStartingAtTimePoint) {
                final DiffTimePoint diffTimePoint = new DiffTimePoint();
                diffTimePoint.timePoint = point;
                diffTimePoint.diff = point - fromPoints.get(i);
                diffTimePoints.add(diffTimePoint);
            }
        }
    }

    static List<Double> calculateMeansThroughTime(
            final DoubleStream timePoints,
            final DoubleStream values
    ) {
        final Iterator<Double> timePointIt = timePoints.iterator();
        final Iterator<Double> valuesIt = values.iterator();

        double prevTimePoint = timePointIt.next();
        double delaySum = 0.0;
        double valueSum = 0;

        final List<Double> valuesThroughTime = new ArrayList<>();

        while(timePointIt.hasNext()) {
            final double timePoint = timePointIt.next();
            final double value = valuesIt.next();
            final double delay = timePoint - prevTimePoint;
            prevTimePoint = timePoint;
            delaySum += delay;
            valueSum += value * delay;
            valuesThroughTime.add(valueSum / delaySum);
        }

        return valuesThroughTime;
    }

    static List<Double> calculateStdDevsThroughTime(
            final DoubleStream timePoints,
            final DoubleStream values,
            final List<Double> means
    ) {
        final Iterator<Double> timePointIt = timePoints.iterator();
        final Iterator<Double> valuesIt = values.iterator();
        final Iterator<Double> meansIt = means.iterator();
        double prevTimePoint = timePointIt.next();
        double delaySum = 0.0;
        double valueSum = 0;

        final List<Double> valuesThroughTime = new ArrayList<>();

        while(timePointIt.hasNext()) {
            final double timePoint = timePointIt.next();
            final double value = valuesIt.next();
            final double delay = timePoint - prevTimePoint;
            delaySum += delay;
            prevTimePoint = timePoint;
            valueSum += Math.pow(value - meansIt.next(), 2) * delay;
            valuesThroughTime.add(Math.sqrt(valueSum / delaySum));
        }

        return valuesThroughTime;
    }
}
