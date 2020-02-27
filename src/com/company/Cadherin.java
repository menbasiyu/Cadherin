package com.company;

import java.util.*;
import java.lang.Math; // to get the constant value pi

/**
 * This module is the first version cadherin object with corresponding features
 * Here we only focus on the thermal and boundary force, but leave the actin
 * retrograde flow as a plan for the future
 * Basically, we describe the feature of cadherin in the space and time.
 * @author yangchen
 * todo: Should implement the bead_force / boundary force change with a scale factor between 0~1 when BoundCadYN = 1
 */

public class Cadherin {
    // instance variables
    private double time = 0.0;
    private double [] bead_position;
    private double [] ref_position;
    private double [] bead_force; // here I assume the force only change at x, y plane
    private double frictional; // frictional coefficient, D = (1/frictional) * kT
    private double diffusion = 28e-3;  // to test this.
    //private double zCad; // the position of surface, at this stage, I assume zCad would not change
    private int state = 0; // to indicate the state of the cadherin, can have three different states, 0-M, 1-X, 2-S
    private double force;
    private double theta;
    //private double k_it = 2; // the spring constant between bound, unit pn/nm
    // The following will be useful when implement the second version
    //private double stime_0 = 0.0; // to indicate the time of cadherin in the state 0;
    //private double stime_1 = 0.0; // to indicate the time of cadherin in the state 1;
    //private double stime_2 = 0.0; // to indicate the time of cadherin in the state 2;

    // rest useful instance variables for evaluation
    // double V; // will be useful when we need the relationship between actin and cadherin
    double kub = 0; // can be private
    // int LigandNum; // I don't think we still need this in the case of cadherin
    private int BoundCadYN = 0; // to indicate whether the cadherin is bound with another cad or not (can be private)
    private int cadBoundIndex; // the index of bound Cadherin
    int TimesUnbound, contaAT;
    double contaBoundLifetimeTot, FinalLife, finalTension = 0;

    // to initiate the Cadherin with the most important features
    public Cadherin(double [] ini_position, double frictional) {
        this.ref_position = ini_position.clone();
        this.bead_position = ini_position.clone();
        this.frictional = frictional;
    }

    // The thermal force and the boundary force part are the stochastic force to update the position of the bead
    // which is the first part of the stochastic diffusion.

    private void thermal_force_update(double temperature, double dt) {
        Random rand = new Random();
        double thermal_force = Math.sqrt(2*0.0000138*temperature*frictional/dt);

        // to calculate the thermal force and update the bead_force
        if (this.state == 0 | this.state == 1) {
            bead_force[0] += thermal_force*rand.nextGaussian();
            bead_force[1] += thermal_force*rand.nextGaussian();
        } else { // move the same direction
            double random_force = thermal_force*rand.nextGaussian();
            bead_force[0] += random_force;
            bead_force[1] += random_force;
        }


        // here I don't consider the thermal force in the z direction yet.
    }

    public void bound_force_update(double dt) {
        bead_force = new double[2];
        bead_force[0] += this.force * Math.cos(this.theta);
        bead_force[1] += this.force * Math.sin(this.theta);

        move(dt);
    }

    public void step_2(double dt, double domainSize) {
        double theta = Math.PI + 2*Math.PI*Math.random();  // get a random direction
        double distance = Math.sqrt(4*diffusion*dt);
        this.time += dt;
        boundary_force_update(domainSize);
        bead_position[0] += distance*Math.cos(theta);
        bead_position[1] += distance*Math.sin(theta);  // to update the location!
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

    // Here the step function I need to make some changes
    // the step depends on the state of the cadherin; Therefore, we don't need the BoundYN to indicate the state
    // todo: if (state == 0 / 1) do the regular thermal diffusion
    //        else (when state = 2) move the two cadherin together
    public void step(double temperature, double dt, double domainSize) {
        bead_force = new double[2]; // each time renew the bead_force
        this.time += dt;
        boundary_force_update(domainSize);
        thermal_force_update(temperature, dt);

        move(dt);
    }

    // set the value of the BoundYN
    //public void setBoundCadYN(int value) { this.BoundCadYN = value; }

    // set the value of the cadBoundIndex
    public void setCadBoundIndex(int value) { this.cadBoundIndex = value; }

    // set the value of the state of the Cadherin
    public void setState(int value) { this.state = value; }

    public void setForce(double value) { this.force = value; }

    public void setTheta(double value) { this.theta = value; }

    // set the bead_position
    public void setBead_position(double[] position) { this.bead_position = position.clone(); }

    // get the bead_position for the user
    public double[] getBead_position() {
        return this.bead_position;
    }

    // getter of time for the user
    public double getTime() {
        return this.time;
    }

    public double[] getRef_position() {return this.ref_position; }

    // get the current state of cadherin
    public int getState() { return this.state; }

    // get the value of the BoundYN
    //public int getBoundCadYN() { return this.BoundCadYN; }

    // get the index of bounded cadherin
    public int getCadBoundIndex() { return this.cadBoundIndex; }
}
