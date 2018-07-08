package com.demo.notlast.service;

import com.demo.notlast.entity.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dell on 7/7/2018.
 */
@Service
public class GetRouteService {

    // fake data todo: get from API
    int default_walk_duration = 10; //最长可步行时间默认值
    int walk_speed = 100; //步行速度,米每分钟
    List<Line> metro = new ArrayList<>(); //整个地铁线路
    List<Point> station = new ArrayList<>(); //所有车站
    GeoApiContext context;

    public GetRouteService() {
        context = new GeoApiContext.Builder()
                .apiKey("AIzaSyCrrIbD_sr2g6Li14JKQdyZe6y8KJ1S9us")
                .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 1080)))  // remove in Google!
                .build();
        station = new ArrayList<>();
        Line line = new Line();
        try {
            File file = ResourceUtils.getFile("metro.txt");
            BufferedReader br = new BufferedReader(new FileReader(file));
            String str;
            int metroLineId = 1;
            while ((str = br.readLine()) != null) {
                String[] ss = str.split(",");
                if (ss[0].equals("1")) {
                    metroLineId = 1;
                } else if (ss[0].equals("2")) {
                    metroLineId = 2;
                    metro.add(line);
                    line = new Line();
                } else {
                    double latitude = Double.parseDouble(ss[1]);
                    double longitude = Double.parseDouble(ss[2]);
                    line.addPoint(new Point(latitude, longitude, metroLineId));
                    station.add(new Point(Double.parseDouble(ss[1]), Double.parseDouble(ss[2])));
                }
            }
            metro.add(line);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Route> getRoute(Point startPoint, Point endPoint, Integer tolerableDuration) {
        List<Route> result = new ArrayList<>();
        for(int i = 0; i < 3/*station.size()*/; i++) {
            for(int j = 0; j < 3/*station.size()*/; j++) {
                if(i == j) continue;
                int walk_duration_1 = getWalkingTime(startPoint, station.get(j));
                if (walk_duration_1 < 0) continue;
                int walk_duration_2 = getWalkingTime(station.get(j), endPoint);
                if (walk_duration_2 < 0) continue;
                TResult taxi_1 = taxiTrip(startPoint, station.get(i));
                if (taxi_1 == null) continue;
                TResult taxi_2 = taxiTrip(station.get(j), endPoint);
                if (taxi_2 == null) continue;
                int taxi_duration_1 = taxi_1.getDuration();
                int taxi_duration_2 = taxi_2.getDuration();
                double taxi_cost_1 = taxi_1.getCost();
                double taxi_cost_2 = taxi_2.getCost();
                Route metro_0 = metroTrip(station.get(i), station.get(j));
                if (metro_0 == null) continue;
                int metro_duration_0 = metro_0.getDuration();
                double metro_cost_0 = metro_0.getCost();
                //w-w
                int t_ww = walk_duration_1 + metro_duration_0 + walk_duration_2;
                if(t_ww <= tolerableDuration) {
                    Route ret = new Route();
                    ret.addTrip(new Trip(startPoint, station.get(i), 0, walk_duration_1));
                    //ret += metro_0.vtrip; //////////////////
                    ret.combineTrip(metro_0);
                    ret.addTrip(new Trip(station.get(j), endPoint, 0, walk_duration_2));
                    ret.setDuration(t_ww);
                    ret.setCost(metro_cost_0);
                    result.add(ret);
                }
                //w-t
                int t_wt = walk_duration_1 + metro_duration_0 + taxi_duration_2;
                if(t_wt <= tolerableDuration) {
                    Route ret = new Route();
                    ret.addTrip(new Trip(startPoint, station.get(i), 0, walk_duration_1));
                    //ret += metro_0.vtrip; //////////////////
                    ret.combineTrip(metro_0);
                    ret.addTrip(new Trip(station.get(j), endPoint, 1, taxi_duration_2));
                    ret.setDuration(t_wt);
                    ret.setCost(metro_cost_0 + taxi_cost_2);
                    result.add(ret);
                }
                //t-w
                int t_tw = taxi_duration_1 + metro_duration_0 + walk_duration_2;
                if(t_tw <= tolerableDuration) {
                    Route ret = new Route();
                    ret.addTrip(new Trip(startPoint, station.get(i), 1, taxi_duration_1));
                    //ret += metro_0.vtrip; //////////////////
                    ret.combineTrip(metro_0);
                    ret.addTrip(new Trip(station.get(j), endPoint, 0, walk_duration_2));
                    ret.setDuration(t_tw);
                    ret.setCost(taxi_cost_1 + metro_cost_0);
                    result.add(ret);
                }
                //t-t
                int t_tt = taxi_duration_1 + metro_duration_0 + taxi_duration_2;
                if(t_tt <= tolerableDuration) {
                    Route ret = new Route();
                    ret.addTrip(new Trip(startPoint, station.get(i), 1, taxi_duration_1));
                    //ret += metro_0.vtrip; //////////////////
                    ret.combineTrip(metro_0);
                    ret.addTrip(new Trip(station.get(j), endPoint, 1, taxi_duration_2));
                    ret.setDuration(t_tt);
                    ret.setCost(taxi_cost_1 + metro_cost_0 + taxi_cost_2);
                    result.add(ret);
                }
                System.out.println(i+","+j);
            }
        }
        return result;
    }

    private int getWalkingTime(Point startPoint, Point endPoint) {
        try {
            String startPosition = startPoint.getLatitude()+","+startPoint.getLongitude();
            String endPosition = endPoint.getLatitude()+","+endPoint.getLongitude();
            DirectionsResult result = DirectionsApi.newRequest(context)
                    .origin(startPosition)
                    .destination(endPosition)
                    .mode(TravelMode.WALKING)
                    .await();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String seconds = gson.toJson(result.routes[0].legs[0].duration.inSeconds);
            return Integer.parseInt(seconds);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private TResult taxiTrip(Point startPoint, Point endPoint) {
        TResult tResult = new TResult();
        try {
            String startPosition = startPoint.getLatitude()+","+startPoint.getLongitude();
            String endPosition = endPoint.getLatitude()+","+endPoint.getLongitude();
            DirectionsResult result = DirectionsApi.newRequest(context)
                    .origin(startPosition)
                    .destination(endPosition)
                    .mode(TravelMode.DRIVING)
                    .await();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String secondStr = gson.toJson(result.routes[0].legs[0].duration.inSeconds);
            String distanceStr = gson.toJson(result.routes[0].legs[0].distance.inMeters);
            double distance = Double.parseDouble(distanceStr) / 1000;
            double cost = getTaxiCost(distance);
            tResult.setDuration(Integer.parseInt(secondStr));
            tResult.setCost(cost);
            return tResult;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private double getTaxiCost(double distance) {
        if (distance < 3.0) {
            return 14.0;
        } else if (distance < 15) {
            return 14 + (distance - 3) / 2.5;
        } else {
            return 14 + 12 * 2.5 + (distance - 15) / 3.8;
        }
    }

    private Route metroTrip(Point startPoint, Point endPoint) {
        Route route = new Route();
        try {
            String startPosition = startPoint.getLatitude()+","+startPoint.getLongitude();
            String endPosition = endPoint.getLatitude()+","+endPoint.getLongitude();
            DirectionsResult result = DirectionsApi.newRequest(context)
                    .origin(startPosition)
                    .destination(endPosition)
                    .mode(TravelMode.TRANSIT)
                    .await();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String secondStr = gson.toJson(result.routes[0].legs[0].duration.inSeconds);
            String distanceStr = gson.toJson(result.routes[0].legs[0].distance.inMeters);
            double cost = getMetroCost(Double.parseDouble(distanceStr) / 1000);
            route.setDuration(Integer.parseInt(secondStr));
            route.setCost(cost);
            return route;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private double getMetroCost(double distance) {
        if (distance < 6) {
            return 3;
        } else {
            return (distance - 6) / 10 + 4;
        }
    }
}
