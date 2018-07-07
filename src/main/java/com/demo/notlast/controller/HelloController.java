package com.demo.notlast.controller;

import com.demo.notlast.entity.RouteRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by dell on 7/7/2018.
 */
@Controller
public class HelloController {

    @RequestMapping("/")
    public ModelAndView index() {
        ModelAndView mav = new ModelAndView("index");
        mav.addObject("routeRequest", new RouteRequest());
        return mav;
    }

    @PostMapping("/search")
    public ModelAndView search(@ModelAttribute RouteRequest routeRequest) {
        String startAddress = routeRequest.getStartAddress();
        String endAddress = routeRequest.getEndAddress();
        Integer time = routeRequest.getExpTime();
        ModelAndView mav = new ModelAndView("index");
        return mav;
    }

}
