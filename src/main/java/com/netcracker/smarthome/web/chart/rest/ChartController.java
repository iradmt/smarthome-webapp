package com.netcracker.smarthome.web.chart.rest;

import com.netcracker.smarthome.business.services.ChartService;
import com.netcracker.smarthome.business.chart.options.RequestDataOptions;
import com.netcracker.smarthome.business.chart.options.jsonfields.DataSeries;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.text.ParseException;
import java.util.ArrayList;

@Controller
public class ChartController {
    private static final Logger LOG = LoggerFactory.getLogger(ChartController.class);
    private ChartConfiguration chartConfiguration;
    private final ChartService chartService;

    @Autowired
    public ChartController(ChartService chartService) {
        this.chartService = chartService;
    }

    @PostConstruct
    private void init() {
        chartConfiguration = new ChartConfiguration();
    }

    @RequestMapping(value = {"/jsonData"}, method = RequestMethod.POST)
    @ApiOperation(value = "Get metric data as json", notes = "Retrieving the metric data")
    @ResponseBody
    public ArrayList<DataSeries> getJsonData(@RequestBody RequestDataOptions requestDataOptions) throws ParseException {
        LOG.info("request:" + requestDataOptions);
        this.chartConfiguration.setChart(new TimeChart(chartService));
        ArrayList<DataSeries> list = this.chartConfiguration.getData(requestDataOptions);
        LOG.info(list.toString());
        return list;
    }

}
