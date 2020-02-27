package com.company;

import java.io.FileWriter; // to write the results to the file
import java.io.IOException;
import java.util.ArrayList;
import java.lang.Math;
import java.util.Iterator;
import java.util.Random;

/**
 * This is the main module to test the Cadherin object and simulate the force interaction between
 * two layers of Cadherin
 * @author yangchen
 */

public class Main {

    // function to calculate the mean-squared displacement
    public static double msd(double[] ref_pos, double[] curr_pos) {
        return Math.pow(curr_pos[0] - ref_pos[0], 2) + Math.pow(curr_pos[1] - ref_pos[1],2);
    }

    // function to simulate the binding between two layers
    public static void simulate(ArrayList<Cadherin> cadList1, ArrayList<Cadherin> cadList2, double totalTime,
                                Force_Interaction force_interaction, double timestep, String nameofFile,
                                double temperature, double domainSize) {
        int size1 = cadList1.size(); int size2 = cadList2.size();
        String path = "./" + nameofFile + ".csv";
        try {
            FileWriter fw = new FileWriter(path, true);
            fw.write(String.format("    " + "time" + "," + "state" + "," + "force" + "\n"));
            // simulate through
            for (int i = 0; i <= (int)(totalTime/timestep); i++) {
                // step 1: do the thermal and boundary update
                for (Cadherin cad1 : cadList1) {
                    cad1.step(temperature, timestep, domainSize);
                }

                for (Cadherin cad2 : cadList2) {
                    cad2.step(temperature, timestep, domainSize);
                }

                // step 2: force interaction update
                force_interaction.interaction();

                // step 3: record the state
                for (Cadherin cad1 : cadList1) {
                    fw.write(String.format("1index %d" + "," + "%d\n", cadList1.indexOf(cad1), cad1.getState()));
                }

                for (Cadherin cad2 : cadList2) {
                    fw.write(String.format("2index %d" + "," + "%d\n", cadList2.indexOf(cad2), cad2.getState()));
                }
            }
            fw.close();
        } catch (IOException ioe) {
            System.err.println("IOException: " + ioe.getMessage());
        }
    }

    /**
    public static void simulate(ArrayList<Cadherin> cadList, double totalTime,
                                double timestep, String nameofFile, double temperature, double domainSize) {
        int size = cadList1.size();
        int count = 0;
        int index = 0;
        double msdisp; // record the displacement at each timestep of each cadherin
        String path = "./" + nameofFile + ".csv";
        String path2 = "./" + nameofFile + "rmsd" + ".csv";
        String fs;
        try {
            FileWriter fw = new FileWriter(path, true);
            FileWriter fw2 = new FileWriter(path2, true);
            fw.write(String.format("            " + "timesteps" + "   " + "x_pos" + "  " + "y_pos" + "      " + "disp\n"));
            fw2.write("RMSD\n");
            for (int i = 0; i <= (int) (totalTime / timestep); i++) {
                double totaldisp = 0; // initialize the total disp
                for (Cadherin cad : cadList) {
                    index = count % size  + 1;
                    msdisp = msd(cad.getRef_position(), cad.getBead_position());
                    totaldisp += msdisp;
                    fs = String.format("Cadherin " + index + "  %6.4f" + "    %6.4f" + "    %6.4f" +  "    %6.4f" +
                                    "\n", cad.getTime(), cad.getBead_position()[0], cad.getBead_position()[1], msdisp);
                    fw.write(fs);
                    cad.step(temperature, timestep, domainSize);
                    //cad.step_2(timestep, domainSize);
                    count++;
                }
                fw2.write(String.format("%6.4f \n", Math.sqrt(totaldisp / size)));
            }
            fw.close();
            fw2.close();
        } catch (IOException ioe) {
            System.err.println("IOExceoption: " + ioe.getMessage());
        }
    }*/

    public static void main(String[] args) {
        // first test can we generate these cadherin objects
        double temperature = 300;// unit: Kelvin
        double domainSize = 1; // unit: micrometer
        double timestep_1 = 1e-6; // s
        double timestep_2 = 10e-6;
        double timestep_3 = 100e-6;
        double timestep_4 = 1000e-6;
        double timestep_5 = 10000e-6;
        double totalTime = 1; // unit: s
        double zCad_1 = 0;
        double zCad_2 = 0.02; // if we use 20 nm then this value should be 0.02 um
        int nOfCad_1 = 21; // assumption at this stage
        int nOfCad_2 = 21;
        // here I assume the both side have the same frictional coefficient
        //double frictional = 1.37e-3; // unit: pN*s/um = kT / D; D = 3 um^2/s need to adjust the value!;
        double frictional_2 = 0.1469; // unit: pN*s/um with D = 28e-3 um^2/s
        String filename_1 = "cad1_6";
        String filename_2 = "cad2_6";

        // generate the Arraylist for Cadherin layer 1 and 2
        ArrayList<Cadherin> Cadherin_1 = new ArrayList<Cadherin>();
        ArrayList<Cadherin> Cadherin_2 = new ArrayList<Cadherin>();

        for (int i = 0; i < nOfCad_1; i++) {
            // initiate the position of each cadherin randomly
            double x = -domainSize / 2 + Math.random() * domainSize;
            double y = -domainSize / 2 + Math.random() * domainSize;
            double[] ini_position = {x, y, zCad_1};
            //double[] ini_position = {0,0,zCad_1}; // start at the origin
            Cadherin_1.add(new Cadherin(ini_position, frictional_2));
        }

        for (int i = 0; i < nOfCad_2; i++) {
            double x = -domainSize / 2 + Math.random() * domainSize;
            double y = -domainSize / 2 + Math.random() * domainSize;
            double[] ini_position = {x, y, zCad_2};
            Cadherin_2.add(new Cadherin(ini_position, frictional_2));
        }

        Force_Interaction force_interaction = new Force_Interaction(Cadherin_1, Cadherin_2, timestep_2);

        // simulate Brownian Motion
        /**
        System.out.println("Start simulation:");
        System.out.printf("Simulate with timestep %8.6f: \n", timestep_5);
        simulate(Cadherin_1, totalTime, timestep_5, filename_1, temperature, domainSize); // simulate the first cadlist
        simulate(Cadherin_2, totalTime, timestep_5, filename_2, temperature, domainSize); // simulate the second cadlist
        System.out.println("Finished!");*/

        // simulate the two layer force interaction
        System.out.println("Start simulation:");
        System.out.printf("Simulate with time step: %8.6f\n", timestep_2);
        simulate(Cadherin_1, Cadherin_2, totalTime, force_interaction, timestep_2, "test", temperature, domainSize);
        System.out.println("Finished!!!");
    }
}
