package com.demo.notlast.entity;

/**
 * Created by dell on 7/7/2018.
 */
public class Point {

    double longitude;
    double latitude;
    boolean isMetro;
    int metroLineId;

    public Point(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public Point(double longitude, double latitude, int metroLineId) {
        this.longitude = longitude;
        this.latitude = latitude;
        isMetro = true;
        this.metroLineId = metroLineId;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public boolean isMetro() {
        return isMetro;
    }

    public void setMetro(boolean metro) {
        isMetro = metro;
    }

    public int getMetroLineId() {
        return metroLineId;
    }

    public void setMetroLineId(int metroLineId) {
        this.metroLineId = metroLineId;
    }

}
