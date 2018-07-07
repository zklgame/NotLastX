package com.demo.notlast.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dell on 7/7/2018.
 */
public class Route {

    List<Trip> vtrip;
    int duration;
    double cost;

    public Route() {
        vtrip = new ArrayList<>();
        duration = 0;
        cost = 0d;
    }

    public List<Trip> getVtrip() {
        return vtrip;
    }

    public void setVtrip(List<Trip> vtrip) {
        this.vtrip = vtrip;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public void addTrip(Trip trip) {
        vtrip.add(trip);
    }

    public void combineTrip(Route metro_0) {
        List<Trip> metro_0_trip = metro_0.getVtrip();
        for (Trip t: metro_0_trip
             ) {
            vtrip.add(t);
        }
        duration += metro_0.getDuration();
        cost += metro_0.getCost();
    }
}
