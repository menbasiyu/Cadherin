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
    // instance variables
    private ArrayList<Cadherin> cadList_1 = new ArrayList<Cadherin>();
    private ArrayList<Cadherin> cadList_2 = new ArrayList<Cadherin>();
    private double timestep = 1e-5;

    // constructor
    public Force_Interaction(ArrayList<Cadherin> cadList_1, ArrayList<Cadherin> cadList_2, double timestep) {
        this.cadList_1 = cadList_1;
        this.cadList_2 = cadList_2;
        this.timestep = timestep;
    }

    private double distance(Cadherin cad1, Cadherin cad2) {
        return Math.sqrt(Math.pow(cad1.getBead_position()[0]-cad2.getBead_position()[0],2) +
                Math.pow(cad1.getBead_position()[1]-cad2.getBead_position()[1],2));
    }

    private double theta(Cadherin cad1, Cadherin cad2, double dist) {
        return Math.acos((cad1.getBead_position()[0] - cad2.getBead_position()[0]) / dist);
    }

    // function to simulate the force interaction between two layers
    // we first implement the interaction to form X-dimer
    // Here consider all possible binding and unbinding of cadherin in different state
    // Also state is enough to indicate the binding and unbinding of a particular cadherin
    public void interaction() {
        // todo: implement the distance function to decide it is bounded or not
        // todo: implement the force interaction and figure out the relationship between steps
        double cutoff = 0.015; // unit: micrometers, use the 15 nm as the cutoff distance
        double cutoff_2 = 0.001; // unit: micrometers

        // all kinetic rate constant regarding the transition between different states
        double k_fx = 3.8e4; // m -> x unit: 1/s
        double k_bx = 1.84e3; // x -> m unit: 1/s
        double k_fms = 3.1e-1; // m -> s unit: 1/s
        double k_bms = 1.27e-4; // s -> m unit: 1/s
        double k_fxs = 86; // x -> s unit: 1/s
        double k_bxs = 0.8; // s -> x unit: 1/s

        // using k * dt to calculate the probability of transition between different states
        double p_fx = k_fx * timestep;
        double p_bx = k_bx * timestep;
        double p_fms = k_fms * timestep;
        double p_bms = k_bms * timestep;
        double p_fxs = k_fxs * timestep;
        double p_bxs = k_bxs * timestep;

        // the spring constant we use to calculate the force when binding state is 1 (X)
        double k_it = 2e3; // unit: 2pn/um

        // After the update of corresponding state thermal diffusion and domain-diffusion
        // Here, consider the kinetic interaction for different states
        for (Cadherin cad1 : cadList_1) {
            // case 1: if the current state of cad1 is 0. (1) can bind to X-dimer (2) S dimer (3) remain monomer
            if (cad1.getState() == 0) {
                // iterate through all unbound cad in the layer 2 check the certain criteria
                for (Cadherin cad2: cadList_2) {
                    if (cad2.getState() == 0) {
                        double dist = distance(cad1, cad2); // calculate the distance

                        // bound with cadherin in the layer 2 to form X-dimer
                        // (a) dist less than cut-off criteria (b) probability satisfied
                        if ((dist <= cutoff) & (Math.random() <= p_fx)) {

                            double theta = theta(cad1, cad2, dist);
                            // 0.001 is the assumption for short distanc
                            double force = k_it * dist;

                            cad1.setState(1); cad1.setForce(force); cad1.setTheta(theta);
                            cad1.setCadBoundIndex(cadList_2.indexOf(cad2)); // set the value of index of bounded cadherin
                            cad2.setState(1); cad2.setForce(-force); cad2.setTheta(theta);
                            cad2.setCadBoundIndex(cadList_1.indexOf(cad1)); // actually there is no need for this line

                            // update the position of the cadherin
                            cad1.bound_force_update(timestep);
                            cad2.bound_force_update(timestep);

                            // break the inner for loop (which indicate the form of binding between two layers)
                            break;
                        }

                        if ((dist <= cutoff_2) & (Math.random() <= p_fms)) { // from the S-dimer the probability is very small
                            // at this stage, we set the two caderin at the same position (x,y) in two layers
                            cad1.setState(2); cad2.setState(2);
                            cad1.setCadBoundIndex(cadList_2.indexOf(cad2));
                            cad2.setCadBoundIndex(cadList_1.indexOf(cad1));
                            cad2.setBead_position(cad1.getBead_position());

                            break;
                        }
                    }
                }
            }

            // case 2: if the current state of the cadherin is 1 then (1) change to S-dimer (2) back to M (3) remain X
            if (cad1.getState() == 1) {
                // recalculate the force between them
                Cadherin boundCad = cadList_2.get(cad1.getCadBoundIndex());
                double ndist = distance(cad1, boundCad);// get corresponding bound cad in layer 2
                double nforce = k_it * ndist;
                double ntheta = theta(cad1, boundCad, ndist);

                if ((nforce <= 2) & (Math.random() <= p_fxs)) { // from X -> S (maybe make the cutoff force bigger)
                    cad1.setState(2);
                    boundCad.setState(2); // next round move together, todo: move together thermal
                    cad1.setForce(nforce);
                    boundCad.setForce(nforce);
                    cad1.setTheta(ntheta);
                    boundCad.setTheta(ntheta);
                } else if (Math.random() <= p_bx) { // from X -> M
                    cad1.setState(0);
                    boundCad.setState(0);
                    cad1.setForce(0);
                    boundCad.setForce(0); // should update theta? actually no effect
                } else {// remain in the state 1 then update the position
                    cad1.setForce(nforce);
                    boundCad.setForce(-nforce);
                    cad1.setTheta(ntheta);
                    boundCad.setTheta(ntheta);
                    cad1.bound_force_update(timestep);
                    boundCad.bound_force_update(timestep);
                }
            }

            // case 3 if the current state is 2 (a) move back to M (b) move back to X (3) remain
            if (cad1.getState() == 2) {
                // (a) move back to the M
                Cadherin boundCad = cadList_2.get(cad1.getCadBoundIndex());
                if (Math.random() <= p_bms) {
                    cad1.setState(0); boundCad.setState(0);
                    cad1.setForce(0); boundCad.setState(0);
                }

                if (Math.random() <= p_bxs) { // move back to X
                    cad1.setState(1); boundCad.setState(1);
                }
            }
        }
    }
}
