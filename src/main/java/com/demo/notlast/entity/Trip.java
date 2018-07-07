package com.demo.notlast.entity;

/**
 * Created by dell on 7/7/2018.
 */
public class Trip {
    Point startPoint;
    Point endPoint;
    int op; //交通方式 0:walk 1:taxi 2:metro
    int duration;

    public Trip(Point startPoint, Point endPoint, int op, int duration) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.op = op;
        this.duration = duration;
    }

    public Point getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(Point startPoint) {
        this.startPoint = startPoint;
    }

    public Point getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(Point endPoint) {
        this.endPoint = endPoint;
    }

    public int getOp() {
        return op;
    }

    public void setOp(int op) {
        this.op = op;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

}
