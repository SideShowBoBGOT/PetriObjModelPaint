/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package LibTest;

//import PetriObj.PetriObjModel;
import LibNet.NetLibrary;
import PetriObj.*;

import java.util.ArrayList;
import java.util.stream.IntStream;


/**
 *
 * @author Inna V. Stetsenko
 */
public class TestPetriObjSimulation {

    public static PetriNet create_model() throws ExceptionInvalidNetStructure, ExceptionInvalidTimeDelay {
        ArrayList<PetriP> d_P = new ArrayList<>();
        ArrayList<PetriT> d_T = new ArrayList<>();
        ArrayList<ArcIn> d_In = new ArrayList<>();
        ArrayList<ArcOut> d_Out = new ArrayList<>();

        final var generated_task = create_task_generator(d_P, d_T, d_In, d_Out);
        final var generated_io_request = create_io_request_generator(d_P, d_T, d_In, d_Out);
        final var generated_interrupt = create_interrupt_generator(d_P, d_T, d_In, d_Out);

        final var processors = create_processors(d_P);
        final var pages = create_pages(d_P);
        final var free_disks = create_free_disks(d_P);
        final var busy_disks = create_busy_disks(d_P);

        final var disk_placed = new PetriP("disk_placed");
        d_P.add(disk_placed);
        final var finished_tasks = new PetriP("finished_tasks");
        d_P.add(finished_tasks);
        final var is_disk_placement_available = new PetriP("is_disk_placement_available", 1);
        d_P.add(is_disk_placement_available);

        final var place_disk = new PetriT("place_disk", 0.0375);
        place_disk.setDistribution("unif", place_disk.getTimeServ());
        place_disk.setParamDeviation(0.021650635);
        d_T.add(place_disk);
        d_In.add(new ArcIn(is_disk_placement_available, place_disk));
        d_In.add(new ArcIn(busy_disks, place_disk));
        d_Out.add(new ArcOut(place_disk, disk_placed, 1));

        final var io_channel_transfer = new PetriT("io_channel_transfer", 0.015);
        io_channel_transfer.setDistribution("unif", io_channel_transfer.getTimeServ());
        io_channel_transfer.setParamDeviation(0.007216878);
        d_T.add(place_disk);
        d_In.add(new ArcIn(disk_placed, io_channel_transfer));
        d_Out.add(new ArcOut(io_channel_transfer, is_disk_placement_available, 1));
        d_Out.add(new ArcOut(io_channel_transfer, finished_tasks, 1));

        final var pages_start = 20;
        final var pages_end = 60;
        final var probability = 1.0 / (double)(pages_end - pages_start);

        IntStream.rangeClosed(pages_start, pages_end).forEach((pages_count) -> {
            final var task_n_pages_name = "task_".concat(Integer.toString(pages_count)).concat("_pages");
            final var priority = pages_end - pages_count;

            final var generate_task_n_pages = new PetriT("generate_".concat(task_n_pages_name));
            generate_task_n_pages.setProbability(probability);
            d_T.add(generate_task_n_pages);
            d_In.add(new ArcIn(generated_task, generate_task_n_pages));

            final var task_n_pages = new PetriP(task_n_pages_name);
            d_P.add(task_n_pages);
            d_Out.add(new ArcOut(generate_task_n_pages, task_n_pages, 1));

            final var process_task_n = new PetriT("process_".concat(task_n_pages_name), 10.0);
            process_task_n.setDistribution("norm", process_task_n.getTimeServ());
            process_task_n.setParamDeviation(3.0);
            process_task_n.setPriority(priority);
            d_T.add(process_task_n);
            d_In.add(new ArcIn(task_n_pages, process_task_n));
            d_In.add(new ArcIn(processors, process_task_n));
            d_In.add(new ArcIn(pages, process_task_n, pages_count));

            final var processed_task_n = new PetriP("processed_".concat(task_n_pages_name));
            d_P.add(processed_task_n);
            d_Out.add(new ArcOut(process_task_n, processed_task_n, 1));

            final var create_io_task_n = new PetriT("create_io_".concat(task_n_pages_name));
            create_io_task_n.setPriority(priority);
            d_T.add(create_io_task_n);
            d_In.add(new ArcIn(processed_task_n, create_io_task_n));
            d_In.add(new ArcIn(generated_io_request, create_io_task_n));

            final var io_task_n = new PetriP("io_".concat(task_n_pages_name));
            d_Out.add(new ArcOut(create_io_task_n, io_task_n, 1));

            final var take_up_disks_task_n = new PetriT("take_up_disks_".concat(task_n_pages_name));
            take_up_disks_task_n.setPriority(priority);
            d_In.add(new ArcIn(io_task_n, take_up_disks_task_n));
            d_In.add(new ArcIn(generated_interrupt, take_up_disks_task_n));
            d_In.add(new ArcIn(free_disks, take_up_disks_task_n));

            d_Out.add(new ArcOut(take_up_disks_task_n, processors, 1));
            d_Out.add(new ArcOut(take_up_disks_task_n, busy_disks, 1));
            d_Out.add(new ArcOut(take_up_disks_task_n, pages, pages_count));
        });

        PetriNet d_Net = new PetriNet("CourseWork", d_P, d_T, d_In, d_Out);
        PetriP.initNext();
        PetriT.initNext();
        ArcIn.initNext();
        ArcOut.initNext();

        return d_Net;
    }

    private static PetriP create_task_generator(
            ArrayList<PetriP> d_P,
            ArrayList<PetriT> d_T,
            ArrayList<ArcIn> d_In,
            ArrayList<ArcOut> d_Out
    ) {
        final PetriP task_generator = new PetriP("generator_task", 1);
        d_P.add(task_generator);
        final PetriT generate_task = new PetriT("generate_task", 5.0);
        d_T.add(generate_task);
        d_In.add(new ArcIn(task_generator, generate_task, 1));
        d_Out.add(new ArcOut(generate_task, task_generator, 1));
        final PetriP generated_task = new PetriP("generated_task", 0);
        d_P.add(task_generator);
        d_Out.add(new ArcOut(generate_task, generated_task, 1));
        return generated_task;
    }

    private static PetriP create_processors(ArrayList<PetriP> d_P) {
        final var processors = new PetriP("processors", 2);
        d_P.add(processors);
        return processors;
    }

    private static PetriP create_pages(ArrayList<PetriP> d_P) {
        final var pages = new PetriP("pages", 131);
        d_P.add(pages);
        return pages;
    }

    private static PetriP create_free_disks(ArrayList<PetriP> d_P) {
        final var disks = new PetriP("free_disks", 4);
        d_P.add(disks);
        return disks;
    }

    private static PetriP create_busy_disks(ArrayList<PetriP> d_P) {
        final var disks = new PetriP("busy_disks", 0);
        d_P.add(disks);
        return disks;
    }

    private static PetriP create_io_request_generator(
            ArrayList<PetriP> d_P,
            ArrayList<PetriT> d_T,
            ArrayList<ArcIn> d_In,
            ArrayList<ArcOut> d_Out
    ) {
        final PetriP generator_io_request = new PetriP("generator_io_request", 1);
        d_P.add(generator_io_request);

        final PetriT generate_io_request = new PetriT("generate_io_request", 6.0);
        generate_io_request.setDistribution("unif", generate_io_request.getTimeServ());
        generate_io_request.setParamDeviation(5.33);

        d_T.add(generate_io_request);
        d_In.add(new ArcIn(generator_io_request, generate_io_request, 1));
        d_Out.add(new ArcOut(generate_io_request, generator_io_request, 1));

        final PetriP generated_io_request = new PetriP("generated_io_request", 0);
        d_P.add(generator_io_request);
        d_Out.add(new ArcOut(generate_io_request, generated_io_request, 1));
        return generated_io_request;
    }

    private static PetriP create_interrupt_generator(
            ArrayList<PetriP> d_P,
            ArrayList<PetriT> d_T,
            ArrayList<ArcIn> d_In,
            ArrayList<ArcOut> d_Out
    ) {
        final var generator_interrupt = new PetriP("generator_interrupt", 1);
        d_P.add(generator_interrupt);

        final var generate_interrupt = new PetriT("generate_interrupt", 6.0);
        generate_interrupt.setDistribution("exp", generate_interrupt.getTimeServ());
        d_T.add(generate_interrupt);
        d_In.add(new ArcIn(generator_interrupt, generate_interrupt, 1));
        d_Out.add(new ArcOut(generate_interrupt, generator_interrupt, 1));

        final var generated_interrupt = new PetriP("generated_interrupt", 0);
        d_P.add(generator_interrupt);
        d_Out.add(new ArcOut(generate_interrupt, generated_interrupt, 1));

        final var drop_interrupt = new PetriT("drop_interrupt", 0.0);
        drop_interrupt.setPriority(Integer.MIN_VALUE);
        d_In.add(new ArcIn(generated_interrupt, drop_interrupt));

        final var drop_counter = new PetriP("drop_counter", 0);
        d_Out.add(new ArcOut(drop_interrupt, drop_counter, 1));

        return generated_interrupt;
    }

    //Результати співпадають з аналітичними обрахунками
      public static void main(String[] args) throws ExceptionInvalidTimeDelay, ExceptionInvalidNetStructure {
                   
     // цей фрагмент для запуску імітації моделі з заданною мережею Петрі на інтервалі часу timeModeling  
          PetriObjModel model = getModel();
          model.setIsProtokol(false);
          double timeModeling = 1000000;
          model.go(timeModeling);
          
         //Цей фрагмент для виведення результатів моделювання на консоль
          System.out.println("Mean value of queue");
          for (int j = 1; j < 5; j++) {
              System.out.println(model.getListObj().get(j).getNet().getListP()[0].getMean());
          }
          System.out.println("Mean value of channel worked");
          for (int j = 1; j < 4; j++) {
              System.out.println(1.0 - model.getListObj().get(j).getNet().getListP()[1].getMean());
          }
          System.out.println(2.0 - model.getListObj().get(4).getNet().getListP()[1].getMean());
          
          System.out.println("Estimation precision");
          double[] valuesQueue = {1.786,0.003,0.004,0.00001};
                 
           System.out.println(" Mean value of queue  precision: ");
           for (int j = 1; j < 5; j++) {
              double inaccuracy = ( model.getListObj().get(j).getNet().getListP()[0].getMean()-valuesQueue[j-1])/valuesQueue[j-1]*100;
              inaccuracy = Math.abs(inaccuracy);
              System.out.println(inaccuracy+" %");
          }
           
           double[] valuesChannel = {0.714,0.054,0.062,0.036};
           
           System.out.println(" Mean value of channel worked  precision: ");
                    
           for (int j = 1; j < 4; j++) {
              double inaccuracy = ( 1.0 - model.getListObj().get(j).getNet().getListP()[1].getMean()-valuesChannel[j-1])/valuesChannel[j-1]*100;
             inaccuracy = Math.abs(inaccuracy);
              
              System.out.println(inaccuracy+" %");
          }
            double inaccuracy = ( 2.0 - model.getListObj().get(4).getNet().getListP()[1].getMean()-valuesChannel[3])/valuesChannel[3]*100;
            inaccuracy = Math.abs(inaccuracy);
           
           System.out.println(inaccuracy+" %");
          
          
          
       /*   for(PetriSim e: model.getListObj()){
              e.printMark();
                           
         }
         for(PetriSim e: model.getListObj()){
              e.printBuffer();
             
         }*/
           
             
      } 
      
     // метод для конструювання моделі масового обслуговування з 4 СМО 
      
      public static PetriObjModel getModel() throws ExceptionInvalidTimeDelay, ExceptionInvalidNetStructure{
          ArrayList<PetriSim> list = new ArrayList<>();
          list.add(new PetriSim(NetLibrary.CreateNetGenerator(2.0)));
          list.add(new PetriSim(NetLibrary.CreateNetSMOwithoutQueue(1, 0.6,"First")));
          list.add(new PetriSim(NetLibrary.CreateNetSMOwithoutQueue(1, 0.3, "Second")));
          list.add(new PetriSim(NetLibrary.CreateNetSMOwithoutQueue(1, 0.4,"Third")));
          list.add(new PetriSim(NetLibrary.CreateNetSMOwithoutQueue(2, 0.1,"Forth")));
          list.add(new PetriSim(NetLibrary.CreateNetFork(0.15, 0.13, 0.3)));
      //перевірка зв'язків
     //     System.out.println(list.get(0).getNet().getListP()[1].getName() + " == " + list.get(1).getNet().getListP()[0].getName());
     //     System.out.println(list.get(1).getNet().getListP()[2].getName() + " == " + list.get(5).getNet().getListP()[0].getName());

          list.get(0).getNet().getListP()[1] = list.get(1).getNet().getListP()[0]; //gen = > SMO1
          list.get(1).getNet().getListP()[2] = list.get(5).getNet().getListP()[0]; //SMO1 = > fork

          list.get(5).getNet().getListP()[1] = list.get(2).getNet().getListP()[0]; //fork =>SMO2
          list.get(5).getNet().getListP()[2] = list.get(3).getNet().getListP()[0]; //fork =>SMO3
          list.get(5).getNet().getListP()[3] = list.get(4).getNet().getListP()[0]; //fork =>SMO4

          list.get(2).getNet().getListP()[2] = list.get(1).getNet().getListP()[0]; //SMO2 => SMO1
          list.get(3).getNet().getListP()[2] = list.get(1).getNet().getListP()[0];//SMO3 => SMO1
          list.get(4).getNet().getListP()[2] = list.get(1).getNet().getListP()[0];//SMO4 => SMO1

          PetriObjModel model = new PetriObjModel(list);
          return model;
      }
           
}
