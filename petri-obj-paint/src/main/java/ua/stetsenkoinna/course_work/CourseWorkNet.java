package ua.stetsenkoinna.course_work;

import ua.stetsenkoinna.PetriObj.*;

import java.util.ArrayList;
import java.util.stream.IntStream;

public class CourseWorkNet {
    public final PetriP generated_task;
    public final PetriP generated_io_request;
    public final PetriP generated_interrupt;
    public final PetriP processors;
    public final PetriP pages;
    public final PetriP free_disks;
    public final PetriP total_wait_allocate_task;
    public final PetriP finished_tasks;
    public final PetriP is_disk_placement_available;

    public static class TaskObject {
        public final PetriT generate;
        public final PetriP task;
        public final PetriT try_allocate;
        public final PetriP allocated;
        public final PetriT fail_allocate;
        public final PetriP fail_allocate_token;
        public final PetriT wait_allocate;
        public final PetriT process;
        public final PetriT create_io;
        public final PetriT take_up_disks;
        public final PetriP busy_disk;
        public final PetriT place_disk;
        public final PetriP disk_placed;
        public final PetriT io_channel_transfer;
        public final PetriP finish;


        public TaskObject(PetriT generate, PetriP task, PetriT tryAllocate, PetriP allocatedTask, PetriT failAllocateTask, PetriP failAllocateTokenTask, PetriT waitAllocateTask, PetriT processTask, PetriT createIoTask, PetriT takeUpDisks, PetriP busyDisk, PetriT placeDiskTask, PetriP diskPlaced, PetriT ioChannelTransfer, PetriP finishedTasks) {
            this.generate = generate;
            this.task = task;
            this.try_allocate = tryAllocate;
            this.allocated = allocatedTask;
            this.fail_allocate = failAllocateTask;
            this.fail_allocate_token = failAllocateTokenTask;
            this.wait_allocate = waitAllocateTask;
            this.process = processTask;
            this.create_io = createIoTask;
            this.take_up_disks = takeUpDisks;
            this.busy_disk = busyDisk;
            this.place_disk = placeDiskTask;
            this.disk_placed = diskPlaced;
            this.io_channel_transfer = ioChannelTransfer;
            this.finish = finishedTasks;
        }
    }

    public final ArrayList<TaskObject> taskObjects;
    public final PetriNet net;

    public CourseWorkNet(
            final int pages_num,
            final int processors_num,
            final int disk_num,
            final int pages_start,
            final int pages_end,
            final double tasksTimeMean
    ) throws ExceptionInvalidTimeDelay {
        final ArrayList<PetriP> d_P = new ArrayList<>();
        final ArrayList<PetriT> d_T = new ArrayList<>();
        final ArrayList<ArcIn> d_In = new ArrayList<>();
        final ArrayList<ArcOut> d_Out = new ArrayList<>();

        generated_task = create_task_generator(d_P, d_T, d_In, d_Out, tasksTimeMean);
        generated_io_request = create_io_request_generator(d_P, d_T, d_In, d_Out);
        generated_interrupt = create_interrupt_generator(d_P, d_T, d_In, d_Out);
        processors = create_processors(processors_num, d_P);
        pages = create_pages(pages_num, d_P);
        free_disks = create_free_disks(disk_num, d_P);

        total_wait_allocate_task = new PetriP("total_wait_allocate_task");
        d_P.add(total_wait_allocate_task);

        finished_tasks = new PetriP("finished_tasks");
        d_P.add(finished_tasks);

        is_disk_placement_available = new PetriP("is_disk_placement_available", 1);
        d_P.add(is_disk_placement_available);

        final double probability = 1.0 / (double) ((pages_end + 1) - pages_start);

        taskObjects = new ArrayList<>();

        IntStream.rangeClosed(pages_start, pages_end).forEach((pages_count) -> {
            final String task_n_name = "task_".concat(Integer.toString(pages_count)).concat("_pages");
            final int priority = (pages_end - pages_count) * 2;

            final PetriT generate_task_n = new PetriT("generate_".concat(task_n_name));
            generate_task_n.setProbability(probability);
            d_T.add(generate_task_n);
            d_In.add(new ArcIn(generated_task, generate_task_n));

            final PetriP task_n_pages = new PetriP(task_n_name);
            d_P.add(task_n_pages);
            d_Out.add(new ArcOut(generate_task_n, task_n_pages, 1));

            final PetriT try_allocate_task_n = new PetriT("try_allocate_".concat(task_n_name));
            d_T.add(try_allocate_task_n);
            try_allocate_task_n.setPriority(priority - 1);
            d_In.add(new ArcIn(task_n_pages, try_allocate_task_n));
            d_In.add(new ArcIn(pages, try_allocate_task_n, pages_count));

            final PetriP allocated_task_n = new PetriP("allocated_".concat(task_n_name));
            d_P.add(allocated_task_n);
            d_Out.add(new ArcOut(try_allocate_task_n, allocated_task_n, 1));

            final PetriT fail_allocate_task_n = new PetriT("fail_allocate_".concat(task_n_name));
            d_T.add(fail_allocate_task_n);
            fail_allocate_task_n.setPriority(Integer.MIN_VALUE);
            d_In.add(new ArcIn(task_n_pages, fail_allocate_task_n));
            d_Out.add(new ArcOut(fail_allocate_task_n, total_wait_allocate_task, 1));

            final PetriP fail_allocate_token_task_n = new PetriP("fail_allocate_token_".concat(task_n_name));
            d_P.add(fail_allocate_token_task_n);
            d_Out.add(new ArcOut(fail_allocate_task_n, fail_allocate_token_task_n, 1));

            final PetriT wait_allocate_task_n = new PetriT("wait_allocate_".concat(task_n_name));
            d_T.add(wait_allocate_task_n);
            wait_allocate_task_n.setPriority(priority);
            d_In.add(new ArcIn(fail_allocate_token_task_n, wait_allocate_task_n));
            d_In.add(new ArcIn(total_wait_allocate_task, wait_allocate_task_n));
            d_In.add(new ArcIn(pages, wait_allocate_task_n, pages_count));
            d_Out.add(new ArcOut(wait_allocate_task_n, allocated_task_n, 1));

            final PetriT process_task_n = new PetriT("process_".concat(task_n_name), 10.0);
            process_task_n.setDistribution("norm", process_task_n.getTimeServ());
            process_task_n.setParamDeviation(3.0);
            process_task_n.setPriority(priority);
            d_T.add(process_task_n);
            d_In.add(new ArcIn(allocated_task_n, process_task_n));
            d_In.add(new ArcIn(processors, process_task_n));

            final PetriP processed_task_n = new PetriP("processed_".concat(task_n_name));
            d_P.add(processed_task_n);
            d_Out.add(new ArcOut(process_task_n, processed_task_n, 1));

            final PetriT create_io_task_n = new PetriT("create_io_".concat(task_n_name));
            create_io_task_n.setPriority(priority);
            d_T.add(create_io_task_n);
            d_In.add(new ArcIn(processed_task_n, create_io_task_n));
            d_In.add(new ArcIn(generated_io_request, create_io_task_n));

            final PetriP io_task_n = new PetriP("io_".concat(task_n_name));
            d_P.add(io_task_n);
            d_Out.add(new ArcOut(create_io_task_n, io_task_n, 1));

            final PetriT take_up_disks_task_n = new PetriT("take_up_disks_".concat(task_n_name));
            take_up_disks_task_n.setPriority(priority);
            d_T.add(take_up_disks_task_n);
            d_In.add(new ArcIn(io_task_n, take_up_disks_task_n));
            d_In.add(new ArcIn(generated_interrupt, take_up_disks_task_n));
            d_In.add(new ArcIn(free_disks, take_up_disks_task_n));
            d_Out.add(new ArcOut(take_up_disks_task_n, processors, 1));
            d_Out.add(new ArcOut(take_up_disks_task_n, pages, pages_count));

            final PetriP busy_disk_task_n = new PetriP("busy_disk_".concat(task_n_name));
            d_P.add(busy_disk_task_n);
            d_Out.add(new ArcOut(take_up_disks_task_n, busy_disk_task_n, 1));

            final PetriT place_disk_task_n = new PetriT("place_disk_".concat(task_n_name), 0.0375);
            place_disk_task_n.setDistribution("unif", place_disk_task_n.getTimeServ());
            place_disk_task_n.setParamDeviation(0.021650635);
            d_T.add(place_disk_task_n);
            d_In.add(new ArcIn(is_disk_placement_available, place_disk_task_n));
            d_In.add(new ArcIn(busy_disk_task_n, place_disk_task_n));

            final PetriP disk_placed_task_n = new PetriP("disk_placed_".concat(task_n_name));
            d_P.add(disk_placed_task_n);
            d_Out.add(new ArcOut(place_disk_task_n, disk_placed_task_n, 1));

            final PetriT io_channel_transfer_task_n = new PetriT("io_channel_transfer_".concat(task_n_name), 0.015);
            io_channel_transfer_task_n.setDistribution("unif", io_channel_transfer_task_n.getTimeServ());
            io_channel_transfer_task_n.setParamDeviation(0.007216878);
            d_T.add(io_channel_transfer_task_n);
            d_In.add(new ArcIn(disk_placed_task_n, io_channel_transfer_task_n));
            d_Out.add(new ArcOut(io_channel_transfer_task_n, is_disk_placement_available, 1));
            d_Out.add(new ArcOut(io_channel_transfer_task_n, free_disks, 1));

            final PetriP finish_task_n = new PetriP("finished_tasks_".concat(task_n_name));
            d_P.add(finish_task_n);
            d_Out.add(new ArcOut(io_channel_transfer_task_n, finish_task_n, 1));
            d_Out.add(new ArcOut(io_channel_transfer_task_n, finished_tasks, 1));

            taskObjects.add(new TaskObject(
                generate_task_n,
                task_n_pages,
                try_allocate_task_n,
                allocated_task_n,
                fail_allocate_task_n,
                fail_allocate_token_task_n,
                wait_allocate_task_n,
                process_task_n,
                create_io_task_n,
                take_up_disks_task_n,
                busy_disk_task_n,
                place_disk_task_n,
                disk_placed_task_n,
                io_channel_transfer_task_n,
                finish_task_n
            ));
        });
        for(final PetriT transition : d_T) {
            transition.setMoments(true);
        }
        net = new PetriNet("CourseWork", d_P, d_T, d_In, d_Out);
        PetriP.initNext();
        PetriT.initNext();
        ArcIn.initNext();
        ArcOut.initNext();
    }

    private static PetriP create_task_generator(
            ArrayList<PetriP> d_P,
            ArrayList<PetriT> d_T,
            ArrayList<ArcIn> d_In,
            ArrayList<ArcOut> d_Out,
            final double tasksTimeMean
    ) {
        final PetriP task_generator = new PetriP("generator_task", 1);
        d_P.add(task_generator);
        final PetriT generate_task = new PetriT("generate_task", tasksTimeMean);
        generate_task.setDistribution("poisson", generate_task.getTimeServ());
        d_T.add(generate_task);
        d_In.add(new ArcIn(task_generator, generate_task, 1));
        d_Out.add(new ArcOut(generate_task, task_generator, 1));
        final PetriP generated_task = new PetriP("generated_task", 0);
        d_P.add(generated_task);
        d_Out.add(new ArcOut(generate_task, generated_task, 1));
        return generated_task;
    }

    private static PetriP create_processors(
            final int num,
            ArrayList<PetriP> d_P
    ) {
        final PetriP processors = new PetriP("processors", num);
        d_P.add(processors);
        return processors;
    }

    private static PetriP create_pages(
            final int num,
            ArrayList<PetriP> d_P
    ) {
        final PetriP pages = new PetriP("pages", num);
        d_P.add(pages);
        return pages;
    }

    private static PetriP create_free_disks(
            final int num,
            ArrayList<PetriP> d_P
    ) {
        final PetriP disks = new PetriP("free_disks", num);
        d_P.add(disks);
        return disks;
    }

    private static PetriP create_io_request_generator(ArrayList<PetriP> d_P, ArrayList<PetriT> d_T, ArrayList<ArcIn> d_In, ArrayList<ArcOut> d_Out) {
        final PetriP generator_io_request = new PetriP("generator_io_request", 1);
        d_P.add(generator_io_request);
        final PetriT generate_io_request = new PetriT("generate_io_request", 6.0);
        generate_io_request.setDistribution("unif", generate_io_request.getTimeServ());
        generate_io_request.setParamDeviation(5.33);
        d_T.add(generate_io_request);
        d_In.add(new ArcIn(generator_io_request, generate_io_request, 1));
        d_Out.add(new ArcOut(generate_io_request, generator_io_request, 1));
        final PetriP generated_io_request = new PetriP("generated_io_request", 0);
        d_P.add(generated_io_request);
        d_Out.add(new ArcOut(generate_io_request, generated_io_request, 1));
        return generated_io_request;
    }

    private static PetriP create_interrupt_generator(ArrayList<PetriP> d_P, ArrayList<PetriT> d_T, ArrayList<ArcIn> d_In, ArrayList<ArcOut> d_Out) {
        final PetriP generator_interrupt = new PetriP("generator_interrupt", 1);
        d_P.add(generator_interrupt);
        final PetriT generate_interrupt = new PetriT("generate_interrupt", 6.0);
        generate_interrupt.setDistribution("exp", generate_interrupt.getTimeServ());
        d_T.add(generate_interrupt);
        d_In.add(new ArcIn(generator_interrupt, generate_interrupt, 1));
        d_Out.add(new ArcOut(generate_interrupt, generator_interrupt, 1));
        final PetriP generated_interrupt = new PetriP("generated_interrupt", 0);
        d_P.add(generated_interrupt);
        d_Out.add(new ArcOut(generate_interrupt, generated_interrupt, 1));
        return generated_interrupt;
    }
}
