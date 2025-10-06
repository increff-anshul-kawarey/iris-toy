package com.iris.increff.controller;

import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class UIController {
    private static final Logger logger = LoggerFactory.getLogger(UIController.class);
    @Value("${app.baseUrl}")
    private String baseUrl;

    @RequestMapping(value = "")
    public ModelAndView index() {
        return mav("index.html");
    }
    @RequestMapping(value = "/ui/upload_Data")
    public ModelAndView uploadData() {
        return mav("uploadData.html");
    }
    @RequestMapping(value = "/ui/algorithm_params")
    public ModelAndView algorithmParams() {
        return mav("algorithmParams.html");
    }
    @RequestMapping(value = "/ui/dashboard")
    public ModelAndView algoProperties() {
        return mav("dashBoard.html");
    }
    @RequestMapping(value = "/ui/report1")
    public ModelAndView report1() {
        return mav("report1.html");
    }
    @RequestMapping(value = "/ui/report2")
    public ModelAndView report2() {
        return mav("report2.html");
    }



    private ModelAndView mav(String page) {
        ModelAndView mav = new ModelAndView(page);
        mav.addObject("baseUrl", baseUrl);
        return mav;
    }

}
