package com.demo.notlast.controller;

import com.demo.notlast.entity.Point;
import com.demo.notlast.entity.Route;
import com.demo.notlast.entity.RouteRequest;
import com.demo.notlast.entity.Trip;
import com.demo.notlast.service.GetRouteService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;
import com.sun.javafx.fxml.builder.ProxyBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by dell on 7/7/2018.
 */
@Controller
public class HelloController {

    @Autowired
    private GetRouteService getRouteService;

    private GeoApiContext context;

    public HelloController() {
        context = new GeoApiContext.Builder()
                .apiKey("AIzaSyCrrIbD_sr2g6Li14JKQdyZe6y8KJ1S9us")
                .build();
    }

    @RequestMapping("/")
    public ModelAndView index() {
        ModelAndView mav = new ModelAndView("index");
        mav.addObject("routeRequest", new RouteRequest());
        return mav;
    }

    @RequestMapping("/test")
    public ModelAndView test() {
        ModelAndView mav = new ModelAndView("test");
        mav.addObject("routeRequest", new RouteRequest());
        return mav;
    }

    @RequestMapping("/pretty")
    public ModelAndView pretty() {
        ModelAndView mav = new ModelAndView("pretty");
        mav.addObject("routeRequest", new RouteRequest());
        return mav;
    }

    private Point transfer(String address) {
        try {
            GeocodingResult[] results = GeocodingApi.geocode(context, address).await();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            if (results.length < 1) return null;
            String latitude = gson.toJson(results[0].geometry.location.lat);
            String longitude = gson.toJson(results[0].geometry.location.lng);
            return new Point(Double.parseDouble(latitude), Double.parseDouble(longitude));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @PostMapping("/search")
    public ModelAndView search(@ModelAttribute RouteRequest routeRequest) {
        ModelAndView mav;
        String startAddress = routeRequest.getStartAddress();
        String endAddress = routeRequest.getEndAddress();
        Integer tolerableDuration = routeRequest.getExpTime() * 60;
        //Point startPosition = new Point(31.173926, 121.595576);
        //Point endPosition = new Point(31.235071, 121.508147);
        Point startPosition = transfer(startAddress);
        Point endPosition = transfer(endAddress);
        if (startPosition != null && endPosition != null) {
            List<Route> routes = getRouteService.getRoute(startPosition, endPosition, tolerableDuration);
//            System.out.println("-------------------------------------");
//            System.out.println("routes size:" + routes.size());
//            for (Route r : routes) {
//                List<Trip> trips = r.getVtrip();
//                for (Trip t : trips) {
//                    if (t == null) {
//                        break;
//                    }
//                    if (t.getStartPoint() == null || t.getEndPoint() == null) {
//                        break;
//                    }
//                    System.out.println(t.getStartPoint().getLatitude() + ", start " + t.getStartPoint().getLongitude());
//                    System.out.println(t.getEndPoint().getLatitude() + ", end " + t.getEndPoint().getLongitude());
//                    System.out.println("type:" + t.getOp());
//                    System.out.println(t.getDuration());
//                    System.out.println();
//                }
//                System.out.println(r.getCost() + "," + r.getDuration() + ",");
//            }
//            System.out.println("-------------------------------------");
            Collections.sort(routes, new Comparator<Route>() {
                @Override
                public int compare(Route r1, Route r2) {
                    return (int)(r1.getCost() - r2.getCost());
                }
            });
//            Collections.sort(routes, new Comparator<Route>() {
//                @Override
//                public int compare(Route r1, Route r2) {
//                    return r1.getDuration() - r2.getDuration();
//                }
//            });
            List<Route> topRoutes = new ArrayList<>();
            for (int i = 0; i < Math.min(3, routes.size()); i++) {
                topRoutes.add(routes.get(i));
            }
            mav = new ModelAndView("result");
            mav.addObject("routes", topRoutes);
        } else {
            mav = new ModelAndView("index");
            mav.addObject("routeRequest", new RouteRequest());
        }
        return mav;
    }

}
