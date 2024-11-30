package ua.stetsenkoinna.course_work;

import ua.stetsenkoinna.PetriObj.ExceptionInvalidTimeDelay;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

public class VerifyCourseWorkNet {

//    public static void main(String[] args) throws ExceptionInvalidTimeDelay, PythonExecutionException, IOException {
//        final int iterations = 20;
//        final int[][] paramMat = {
//                {131, 2, 4, 20, 60},
//                {300, 5, 4, 10, 30},
//                {400, 7, 4, 20, 40},
//                {500, 10, 10, 20, 40},
//                {600, 10, 4, 20, 40},
//                {600, 10, 4, 20, 45},
//                {700, 12, 10, 20, 30},
//                {400, 10, 10, 10, 40},
//                {900, 30, 30, 10, 40},
//                {1000, 100, 100, 20, 60},
//                {2000, 200, 200, 20, 60},
//                {3000, 300, 300, 20, 60},
//        };
//        final double timeModelling = 10000;
//        final String outputFileName = "experiment_results.csv";
//
//        try (FileWriter writer = new FileWriter(outputFileName)) {
//            writer.write("Прогін,Кількість сторінок,Кількість процесорів,Кількість дисків,Початок сторінок,Кінець сторінок,Час в системі,Час очікування,Завантаження дисків,Завантаження каналу передачі,Завантаження процесорів,Зайняті сторінки,Кількість завдань в очікуванні\n");
//            for(int runIndex = 0; runIndex < paramMat.length; runIndex++) {
//                final int[] paramRow = paramMat[runIndex];
//                final int pagesNum = paramRow[0];
//
//                final AtomicReference<Double> totalPlaceDiskWorkTime = new AtomicReference<>((double) 0);
//                final AtomicReference<Double> totalIoChannelWorkTime = new AtomicReference<>((double) 0);
//                final AtomicReference<Double> totalProcessorsWorkTime = new AtomicReference<>((double) 0);
//                final ArrayList<Double> timeInSystemList = new ArrayList<>();
//                final ArrayList<Double> waitAllocateTimeList = new ArrayList<>();
//                final ArrayList<ArrayList<Integer>> pagesMarks = new ArrayList<>();
//                final ArrayList<ArrayList<Integer>> totalWaitAllocateTaskMarks = new ArrayList<>();
//
//                IntStream.rangeClosed(0, iterations).forEach(iteration -> {
//                    final CourseWorkNet courseWorkNet;
//                    try {
//                        courseWorkNet = new CourseWorkNet(pagesNum, paramRow[1], paramRow[2], paramRow[3], paramRow[4]);
//                    } catch (ExceptionInvalidTimeDelay e) {
//                        throw new RuntimeException(e);
//                    }
//                    final CourseWorkPetriSim sim = new CourseWorkPetriSim(courseWorkNet.net);
//                    sim.go(timeModelling);
//                    for (final CourseWorkNet.TaskObject taskObject : courseWorkNet.taskObjects) {
//                        for (int i = 0; i < taskObject.io_channel_transfer.getOutMoments().size(); i++) {
//                            final double timeInSystem = taskObject.io_channel_transfer.getOutMoments().get(i)
//                                    - taskObject.generate.getOutMoments().get(i);
//                            timeInSystemList.add(timeInSystem);
//                        }
//                        for (int i = 0; i < taskObject.wait_allocate.getOutMoments().size(); i++) {
//                            final double waitTime = taskObject.wait_allocate.getOutMoments().get(i)
//                                    - taskObject.fail_allocate.getOutMoments().get(i);
//                            waitAllocateTimeList.add(waitTime);
//                        }
//                        totalPlaceDiskWorkTime.updateAndGet(v -> (double) (v + taskObject.place_disk.getTotalTimeServ()));
//                        totalIoChannelWorkTime.updateAndGet(v -> (double) (v + taskObject.io_channel_transfer.getTotalTimeServ()));
//                        totalProcessorsWorkTime.updateAndGet(v -> (double) (v + taskObject.process.getTotalTimeServ()));
//                    }
//                    pagesMarks.add(courseWorkNet.pages.getMarks());
//                    totalWaitAllocateTaskMarks.add(courseWorkNet.total_wait_allocate_task.getMarks());
//                });
//                final double iterationsTimeModelling = timeModelling * iterations;
//                final double meanTimeInSystem = calculateAverage(timeInSystemList);
//                final double meanWaitAllocateTime = calculateAverage(waitAllocateTimeList);
//                final double diskLoad = totalPlaceDiskWorkTime.get() / iterationsTimeModelling;
//                final double ioChannelLoad = totalIoChannelWorkTime.get() / iterationsTimeModelling;
//                final double processorsLoad = totalProcessorsWorkTime.get() / iterationsTimeModelling;
//                final double meanUseOfPages = pagesNum - (calculateAverageMat(pagesMarks));
//                final double meanTotalWaitAllocateTasks = calculateAverageMat(totalWaitAllocateTaskMarks);
//
//                final String formattedRow = String.format(
//                        "%d,%d,%d,%d,%d,%d,%.5f,%.5f,%.5f,%.5f,%.5f,%.5f,%.5f\n",
//                        runIndex + 1, paramRow[0], paramRow[1], paramRow[2], paramRow[3], paramRow[4],
//                        meanTimeInSystem, meanWaitAllocateTime, diskLoad, ioChannelLoad, processorsLoad,
//                        meanUseOfPages, meanTotalWaitAllocateTasks
//                );
//                writer.write(formattedRow);
//            }
//        }
//        System.out.println("Results have been saved to " + outputFileName);
//    }
//
//    public static <T extends Number> double calculateAverage(final ArrayList<T> list) {
//        return list.stream().map(Number::doubleValue).reduce(Double::sum).get() / list.size();
//    }
//
//    public static <T extends Number> double calculateAverageMat(ArrayList<ArrayList<T>> list) {
//        double sum = 0.0;
//        double size = 0;
//        for (final ArrayList<T> row : list) {
//            for(final Number num : row) {
//                sum += num.doubleValue();
//            }
//            size += row.size();
//        }
//        return sum / size;
//    }
}
