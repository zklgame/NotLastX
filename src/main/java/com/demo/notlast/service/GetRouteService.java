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

    public GetRouteService() {
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
                double dis_1 = getDistance(startPoint, station.get(j));
                double dis_2 = getDistance(station.get(j), endPoint);
                int walk_duration_1 = (int)Math.ceil(dis_1/walk_speed);
                int walk_duration_2 = (int)Math.ceil(dis_2/walk_speed);
                TResult taxi_1 = taxiTrip(startPoint, station.get(i));
                TResult taxi_2 = taxiTrip(station.get(j), endPoint);
                int taxi_duration_1 = taxi_1.getDuration();
                int taxi_duration_2 = taxi_2.getDuration();
                double taxi_cost_1 = taxi_1.getCost();
                double taxi_cost_2 = taxi_2.getCost();
                Route metro_0 = metroTrip(station.get(i), station.get(j));
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
            }
        }
        return result;
    }

    private double getDistance(Point startPoint, Point endPoint) {
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey("AIzaSyCrrIbD_sr2g6Li14JKQdyZe6y8KJ1S9us")
                .build();
        try {
            String startPosition = startPoint.getLatitude()+","+startPoint.getLongitude();
            String endPosition = endPoint.getLatitude()+","+endPoint.getLongitude();
            DirectionsResult result = DirectionsApi.newRequest(context)
                    .origin(startPosition)
                    .destination(endPosition)
                    .mode(TravelMode.WALKING)
                    .await();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            System.out.println(gson.toJson(result.geocodedWaypoints));
            System.out.println(gson.toJson(result.routes));
        } catch (ApiException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private TResult taxiTrip(Point startPoint, Point endPoint) {
        TResult tResult = new TResult();
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey("AIzaSyCrrIbD_sr2g6Li14JKQdyZe6y8KJ1S9us")
                .build();
        try {
            String startPosition = startPoint.getLatitude()+","+startPoint.getLongitude();
            String endPosition = endPoint.getLatitude()+","+endPoint.getLongitude();
            DirectionsResult result = DirectionsApi.newRequest(context)
                    .origin(startPosition)
                    .destination(endPosition)
                    .mode(TravelMode.DRIVING)
                    .await();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            System.out.println(gson.toJson(result.geocodedWaypoints));
            System.out.println(gson.toJson(result.routes));
        } catch (ApiException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //todo: test data replaced
        tResult.setCost(1.2);
        tResult.setDuration(10);
        return tResult;
    }

    private Route metroTrip(Point startPoint, Point endPoint) {
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey("AIzaSyCrrIbD_sr2g6Li14JKQdyZe6y8KJ1S9us")
                .build();
        try {
            String startPosition = startPoint.getLatitude()+","+startPoint.getLongitude();
            String endPosition = endPoint.getLatitude()+","+endPoint.getLongitude();
            DirectionsResult result = DirectionsApi.newRequest(context)
                    .origin(startPosition)
                    .destination(endPosition)
                    .mode(TravelMode.TRANSIT)
                    .await();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            System.out.println(gson.toJson(result.geocodedWaypoints));
            System.out.println(gson.toJson(result.routes));
        } catch (ApiException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Route mResult = new Route();
        return mResult;
    }
}
