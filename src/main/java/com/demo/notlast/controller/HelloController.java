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
import java.util.List;

/**
 * Created by dell on 7/7/2018.
 */
@Controller
public class HelloController {

    @Autowired
    private GetRouteService getRouteService;

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

    @RequestMapping("/testGoogleMap")
    public ModelAndView testGoogleMap() {
        ModelAndView mav = new ModelAndView("testGoogleMap");
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey("AIzaSyCrrIbD_sr2g6Li14JKQdyZe6y8KJ1S9us")
                .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 1080)))  // remove in Google!
                .build();
        GeocodingResult[] results = new GeocodingResult[0];
        try {
            results = GeocodingApi.geocode(context,
                    "上海东方明珠").await();
        } catch (ApiException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println(gson.toJson(results[0].geometry));
        return mav;
    }

    @PostMapping("/search")
    public ModelAndView search(@ModelAttribute RouteRequest routeRequest) {
        String startAddress = routeRequest.getStartAddress();
        String endAddress = routeRequest.getEndAddress();
        Integer tolerableDuration = routeRequest.getExpTime();
        //todo: transfer from
        Point startPosition = new Point(31.173926, 121.595576);
        Point endPosition = new Point(31.235071, 121.508147);
        List<Route> routes = getRouteService.getRoute(startPosition, endPosition, tolerableDuration);
        System.out.println("-------------------------------------");
        System.out.println("routes size:"+routes.size());
        for (Route r: routes) {
            List<Trip> trips = r.getVtrip();
            for (Trip t : trips) {
                if (t == null) {
                    break;
                }
                if (t.getStartPoint() == null || t.getEndPoint() == null) {
                    break;
                }
                System.out.println(t.getStartPoint().getLatitude()+", start "+t.getStartPoint().getLongitude());
                System.out.println(t.getEndPoint().getLatitude()+", end "+t.getEndPoint().getLongitude());
                System.out.println("type:"+t.getOp());
                System.out.println(t.getDuration());
                System.out.println();
            }
            System.out.println(r.getCost()+","+r.getDuration()+",");
        }
        System.out.println("-------------------------------------");
        ModelAndView mav = new ModelAndView("result");
        mav.addObject("route", routes.get(0));
        return mav;
    }

}
