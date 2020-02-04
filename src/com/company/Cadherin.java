package com.company;

import java.util.*;

/**
 * This module is the first version cadherin object with corresponding features
 * Here we only focus on the thermal and boundary force, but leave the actin
 * retrograde flow as a plan for the future
 * Basically, we describe the feature of cadherin in the space and time.
 * @author yangchen
 */

public class Cadherin {
    // instance variables
    private double time = 0.0;
    private double [] bead_position;
    private double [] bead_force; // here I assume the force only change at x, y plane
    private double frictional; // frictional coefficient, D = (1/frictional) * kT
    //private double zCad; // the position of surface, at this stage, I assume zCad would not change

    // rest useful instance variables for evaluation
    // double V; // will be useful when we need the relationship between actin and cadherin
    double kub = 0; // can be private
    // int LigandNum; // I don't think we still need this in the case of cadherin
    int BoundCadYN = 0; // to indicate whether the cadherin is bound with another cad or not (can be private)
    int TimesUnbound, contaAT;
    double contaBoundLifetimeTot, FinalLife, finalTension = 0;

    // to initiate the Cadherin with the most important features
    public Cadherin(double [] ini_position, double frictional) {
        this.bead_position = ini_position.clone();
        this.frictional = frictional;
    }

    // The thermal force and the boundary force part are the stochastic force to update the position of the bead
    // which is the first part of the stochastic diffusion.

    private void thermal_force_update(double temperature, double dt) {
        Random rand = new Random();

        // to calculate the thermal force and update the bead_force
        double thermal_force = Math.sqrt(2*0.0000138*temperature*frictional/dt);
        bead_force[0] += thermal_force*rand.nextGaussian();
        bead_force[1] += thermal_force*rand.nextGaussian();
        // here I don't consider the thermal force in the z direction yet.
    }

    //private void flow_force_update() {bead_force[1] += frictional * V} if necessary

    private void boundary_force_update(double domainSize) {
        // update the x direction
        if (bead_position[0] >= domainSize/2 && BoundCadYN == 0) {
            bead_position[0] -= domainSize;
        }
        if (bead_position[0] < -domainSize/2 && BoundCadYN == 0) {
            bead_position[0] += domainSize;
        }
        // update the y direction
        if (bead_position[1] >= domainSize/2 && BoundCadYN == 0) {
            bead_position[1] -= domainSize;
        }
        if (bead_position[1] < -domainSize/2 && BoundCadYN == 0) {
            bead_position[1] += domainSize;
        }
    }

    private void move(double dt) {
        for (int i = 0; i < 2; i++) {
            bead_position[i] -= bead_force[i]*dt/frictional;
        }
    }

    public void step(double temperature, double dt, double domainSize) {
        bead_force = new double[2]; // each time renew the bead_force
        this.time += dt;
        boundary_force_update(domainSize);
        if (BoundCadYN == 0) {
            thermal_force_update(temperature, dt);
            kub = 0;
        }
        else {
            thermal_force_update(temperature, dt); // here need consider the force generated by the binding
        }

        move(dt);
    }

    // get the bead_position for the user
    public double[] getBead_position() {
        return this.bead_position;
    }

    // getter of time for the user
    public double getTime() {
        return this.time;
    }
}
