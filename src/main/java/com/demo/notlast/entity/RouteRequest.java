package com.demo.notlast.entity;

/**
 * Created by dell on 7/7/2018.
 */
public class RouteRequest {
    private String startAddress;
    private String endAddress;
    private Integer expTime;

    public String getStartAddress() {
        return startAddress;
    }

    public void setStartAddress(String startAddress) {
        this.startAddress = startAddress;
    }

    public String getEndAddress() {
        return endAddress;
    }

    public void setEndAddress(String endAddress) {
        this.endAddress = endAddress;
    }

    public Integer getExpTime() {
        return expTime;
    }

    public void setExpTime(Integer expTime) {
        this.expTime = expTime;
    }
}
