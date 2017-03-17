package com.netcracker.smarthome.web.chart.configuration;

import com.netcracker.smarthome.business.chart.ChartService;
import com.netcracker.smarthome.model.entities.MetricSpec;
import com.netcracker.smarthome.model.entities.SmartHome;
import com.netcracker.smarthome.model.entities.SmartObject;
import com.netcracker.smarthome.web.chart.options.jsonfields.SeriesConfig;
import com.netcracker.smarthome.web.chart.options.jsonfields.YAxisNumber;

import java.util.ArrayList;
import java.util.List;

public class MultiChartConfig extends DefaultChartConfig implements ChartConfigInterface {
    private String title;
    private ChartService chartService;

    public MultiChartConfig(SmartHome smartHome, long chartId, String chartTitle, ChartService chartService, double refreshInterval, String chartType, String chartInterval) {
        super(smartHome, chartId, refreshInterval, chartType, chartInterval);
        this.title=chartTitle;
        this.chartService=chartService;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ChartOptionsInterface configure(List<MetricSpec> selectedMetricSpecs, List<SmartObject> selectedSmartObjects, List<SmartObject> selectedSubObject) {

        super.getChartConfig().getChartOptions().setChartTitle(getTitle());

        ArrayList<YAxisNumber> yAxisNumbers = super.setAxisOptions(selectedMetricSpecs,chartService);

        selectedSmartObjects=super.deleteParentObj(selectedSubObject,selectedSmartObjects);

        for (SmartObject smartObject : selectedSmartObjects) {
            for (MetricSpec metricSpec : chartService.getMetricsSpecByObjectId(smartObject.getSmartObjectId())) {
                SeriesConfig seriesConfig = new SeriesConfig();
                seriesConfig.setData("");
                if (isChild(smartObject)) {
                    seriesConfig.setName(smartObject.getParentObject().getName());
                } else {
                    seriesConfig.setName(smartObject.getName());
                }
                seriesConfig.setType(super.getChartType());
                for (YAxisNumber yAxisNumber : yAxisNumbers) {
                    if (yAxisNumber.getSpecId() == metricSpec.getSpecId()) {
                        seriesConfig.setyAxis(yAxisNumber.getNumber());
                    }
                }
                super.getChartConfig().getChartOptions().getSeries().add(seriesConfig);
            }
            super.getChartConfig().getRequestDataOptions().getObjectId().add(smartObject.getSmartObjectId());
        }
        super.setInterval();

        return super.getChartConfig();
    }

    private boolean isChild(SmartObject smartObject) {
        return smartObject.getParentObject() != null;
    }
}
