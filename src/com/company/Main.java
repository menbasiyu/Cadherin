package com.company;

import java.util.ArrayList;
import java.util.Random;

/**
 * This is the main module to test the Cadherin object and simulate the force interaction between
 * two layers of Cadherin
 * @author yangchen
 */

public class Main {

    public static void main(String[] args) {
        // first test can we generate these cadherin objects
        double temperature = 300;// unit: Kelvin
        double domainSize = 1; // unit: micrometer
        double timestep = 0.0001;
        double zCad_1 = 0;
        double zCad_2 = 0.2;
        double nOfCad_1 = 10; // assumption at this stage
        double nOfCad_2 = 10;
        // here I assume the both side have the same frictional coefficient
        double frictional = 1.37e-3; // unit: pN*s/um = kT / D; D = 3 um^2/s;


        // generate the Arraylist for Cadherin layer 1 and 2
        ArrayList<Cadherin> Cadherin_1 = new ArrayList<Cadherin>();
        ArrayList<Cadherin> Cadherin_2 = new ArrayList<Cadherin>();

        for (int i = 0; i <= nOfCad_1; i++) {
            // initiate the position of each cadherin randomly
            double x = -domainSize / 2 + Math.random()*domainSize;
            double y = -domainSize / 2 + Math.random()*domainSize;
            double[] ini_position = {x, y, zCad_1};
            Cadherin_1.add(new Cadherin(ini_position, frictional));
        }

        for (int i = 0; i <= nOfCad_2; i++) {
            double x = -domainSize / 2 + Math.random()*domainSize;
            double y = -domainSize / 2 + Math.random()*domainSize;
            double[] ini_position = {x, y, zCad_2};
            Cadherin_2.add(new Cadherin(ini_position, frictional));
        }
    }
}
