package ua.stetsenkoinna.course_work;

import ua.stetsenkoinna.PetriObj.ExceptionInvalidTimeDelay;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

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

    static class DetermineDistribution {
        public static void main(String[] args) throws IOException {
            try (
                    BufferedWriter writerCommonProps = new BufferedWriter(new FileWriter("commonProps.csv"));
                    BufferedWriter writerTimeInSystem = new BufferedWriter(new FileWriter("timeInSystem.csv"));
                    BufferedWriter writerTimeWaitAllocate = new BufferedWriter(new FileWriter("timeWaitAllocate.csv"))
            ) {
                for(int runNumber = 0; runNumber < 8; runNumber++) {
                    final PropertyStats propertyStats = collectStats(
                            800000,
                            0,
                            131,
                            2,
                            4,
                            1,
                            60,
                            7
                    );
                    writerCommonProps.write("runNumber"
                            + "," + "timePoint"
                            + "," + "diskLoad"
                            + "," + "ioChannelLoad"
                            + "," + "processorsLoad"
                            + "," + "totalWaitAllocate"
                            + "," + "useOfPage"
                    );
                    writerCommonProps.newLine();
                    for(int i = 0; i < propertyStats.timePoint.size(); i++) {
                        writerCommonProps.write(
                                runNumber
                                + "," + propertyStats.timePoint.get(i)
                                + "," + propertyStats.diskLoad.get(i)
                                + "," + propertyStats.ioChannelLoad.get(i)
                                + "," + propertyStats.processorsLoad.get(i)
                                + "," + propertyStats.totalWaitAllocate.get(i)
                                + "," + propertyStats.useOfPage.get(i)
                        );
                        writerCommonProps.newLine();
                    }

                    writerTimeInSystem.write("runNumber,timePoint,timeInSystem");
                    writerTimeInSystem.newLine();
                    for(final DiffTimePoint diffTimePoint : propertyStats.timeInSystem) {
                        writerTimeInSystem.write(runNumber + "," + diffTimePoint.timePoint + "," + diffTimePoint.diff);
                        writerTimeInSystem.newLine();
                    }

                    writerTimeWaitAllocate.write("runNumber,timePoint,timeWaitAllocate");
                    writerTimeWaitAllocate.newLine();
                    for(final DiffTimePoint diffTimePoint : propertyStats.timeWaitAllocate) {
                        writerTimeWaitAllocate.write(runNumber + "," + diffTimePoint.timePoint + "," + diffTimePoint.diff);
                        writerTimeWaitAllocate.newLine();
                    }
                }
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
}