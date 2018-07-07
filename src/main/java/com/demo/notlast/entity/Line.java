package com.demo.notlast.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dell on 7/7/2018.
 */
public class Line {
    List<Point> line;

    public Line() {
        line = new ArrayList<>();
    }

    public void addPoint(Point point) {
        line.add(point);
    }
}
