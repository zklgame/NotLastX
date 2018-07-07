package com.demo.notlast.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dell on 7/7/2018.
 */
public class MResult {
    int duration;
    double cost;
    List<Trip> vtrip;

    public MResult() {
        vtrip = new ArrayList<>();
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

    public List<Trip> getVtrip() {
        return vtrip;
    }

    public void setVtrip(List<Trip> vtrip) {
        this.vtrip = vtrip;
    }
}
