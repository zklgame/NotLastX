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

    int default_walk_duration = 10; //最长可步行时间默认值
    int walk_speed = 100; //步行速度,米每分钟
    List<Line> metro = new ArrayList<>(); //整个地铁线路
    List<Point> station = new ArrayList<>(); //所有车站
    GeoApiContext context;

    public GetRouteService() {
        context = new GeoApiContext.Builder()
                .apiKey("AIzaSyCrrIbD_sr2g6Li14JKQdyZe6y8KJ1S9us")
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
        for(int i = 0; i < station.size(); i++) {
            for(int j = 0; j < station.size(); j++) {
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
                    ret.setDuration(t_ww / 60);
                    ret.setCost((int)metro_cost_0);
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
                    ret.setDuration(t_wt / 60);
                    ret.setCost((int)(metro_cost_0 + taxi_cost_2));
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
                    ret.setDuration(t_tw / 60);
                    ret.setCost((int)(taxi_cost_1 + metro_cost_0));
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
                    ret.setDuration(t_tt / 60);
                    ret.setCost((int)(taxi_cost_1 + metro_cost_0 + taxi_cost_2));
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
        Route mResult = new Route();
        try {
            String startPosition = startPoint.getLatitude()+","+startPoint.getLongitude();
            String endPosition = endPoint.getLatitude()+","+endPoint.getLongitude();
            DirectionsResult result = DirectionsApi.newRequest(context)
                    .origin(startPosition)
                    .destination(endPosition)
                    .mode(TravelMode.TRANSIT)
                    .await();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String duration = gson.toJson(result.routes[0].legs[0].duration.inSeconds);
            mResult.setDuration(Integer.parseInt(duration));
            double cost = 0;
            int distance = 0;
            for(int i=0; i<result.routes[0].legs[0].steps.length; ++i){
                double start_lat = result.routes[0].legs[0].steps[i].startLocation.lat;
                double start_lng = result.routes[0].legs[0].steps[i].startLocation.lng;
                double end_lat = result.routes[0].legs[0].steps[i].startLocation.lat;
                double end_lng = result.routes[0].legs[0].steps[i].startLocation.lng;
                String step_duration_str = gson.toJson(result.routes[0].legs[0].duration.inSeconds);
                int step_duration = Integer.parseInt(step_duration_str);
                String step_distance_str = gson.toJson(result.routes[0].legs[0].distance.inMeters);
                int flag = 1;
                String travel_mode = gson.toJson(result.routes[0].legs[0].steps[i].travelMode);
                if(travel_mode.equals("TRANSIT") || travel_mode.equals("transit")){
                    flag = 2;
                    distance += Integer.parseInt(step_distance_str);
                }
                else if(travel_mode.equals("WALKING") || travel_mode.equals("walking")){
                    flag = 0;
                }
                Point start_loc = new Point(start_lng, start_lat);
                Point end_loc = new Point(end_lng, end_lat);
                Trip step = new Trip(start_loc, end_loc, flag, step_duration);
                mResult.addTrip(step);
            }
            cost = getMetroCost(distance / 1000.0);
            mResult.setCost(cost);
            return mResult;
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
