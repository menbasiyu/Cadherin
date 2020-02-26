package com.company;

import java.util.ArrayList;
import java.lang.Math;

/**
 * Here this module is to achieve the force interaction between these two layers of Cadherin.
 * But, firstly, I need to solve and figure out some important problems.
 * Q1: how do we evaluate if there is a bond between two bead of cadherin (distance / probability to form?) what
 * metrics? cut-off distance + probability
 * Q2: if they form the bond, do we still to have the k_it to calculate the force to update the position of bead?
 * bond type? yes, we still use k_it to update the position of bounded cadherins
 * Q3: after form the bond, will the effect of thermal force getting smaller? if they are in the s-dimer we update
 * their distance to 0 if the force is smaller than certain level
 *
 * In this module we will set up all static methods in terms of the interaction
 */

public class Force_Interaction {
    public static double distance(Cadherin cad1, Cadherin cad2) {
        return Math.sqrt(Math.pow(cad1.getBead_position()[0]-cad2.getBead_position()[0],2) +
                Math.pow(cad1.getBead_position()[1]-cad2.getBead_position()[1],2));
    }

    public static double theta(Cadherin cad1, Cadherin cad2, double dist) {
        return Math.acos((cad1.getBead_position()[0] - cad2.getBead_position()[0]) / dist);
    }

    // function to simulate the force interaction between two layers
    // we first implement the interaction to form X-dimer
    public static void interaction(ArrayList<Cadherin> cadList_1, ArrayList<Cadherin> cadList_2, double timestep
            , double temperature, double dt, double domainSize) {
        // todo: implement the distance function to decide it is bounded or not
        // todo: implement the force interaction and figure out the relationship between steps
        double cutoff = 0.015; // unit: micrometers
        double k_fx = 3.8e4; // m -> x unit: 1/s
        double k_bx = 1.84e3; // x -> m unit: 1//s
        double p_fx = k_fx * timestep;
        double p_bx = k_bx * timestep;
        double k_it = 2; // unit: 2pn/nm
        for (Cadherin cad1 : cadList_1) {
            if (cad1.getBoundCadYN() == 0) {
                for (Cadherin cad2: cadList_2) {
                    if (cad2.getBoundCadYN() == 0) {
                        double dist = distance(cad1, cad2); // calculate the distance

                        // bound with cadherin in the layer 2
                        if ((dist <= cutoff) & (Math.random() <= p_fx)) {
                            // check the probability of forming the bound
                            //update the state of cadherin (can I only use one method to do this?)
                            double theta = theta(cad1, cad2, dist);
                            double force = k_it * (dist - 0.001); // 0.001 is the assumption for short distance

                            cad1.setBoundCadYN(1); cad1.setState(1); cad1.setForce(force); cad1.setTheta(theta);
                            cad1.setCadBoundIndex(cadList_2.indexOf(cad2)); // set the value of index of bounded cadherin
                            cad2.setBoundCadYN(1); cad2.setState(1); cad2.setForce(-force); cad2.setTheta(theta);
                            cad2.setCadBoundIndex(cadList_1.indexOf(cad1)); // actually there is no need for this line

                            // update the position of the cadherin
                            cad1.step(temperature, dt, domainSize);
                            cad2.step(temperature, dt, domainSize);
                        }
                    break;
                    }
                }
                cad1.step(temperature, dt, domainSize);
            } else {
                if (Math.random() <= p_bx) {
                    // update the unbinding of the cadherins
                    cad1.setBoundCadYN(0); cad1.setState(0);
                    cadList_2.get(cad1.getCadBoundIndex()).setBoundCadYN(0);
                    cadList_2.get(cad1.getCadBoundIndex()).setState(0);
                }
            }
        }
    }
}
