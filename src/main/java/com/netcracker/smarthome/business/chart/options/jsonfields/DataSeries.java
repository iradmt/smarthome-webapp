package com.netcracker.smarthome.business.chart.options.jsonfields;

import com.netcracker.smarthome.model.entities.MetricHistory;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class DataSeries  {

    private ArrayList<Data> data;

    public DataSeries(ArrayList<Data> data) {
        this.data = data;
    }

    public ArrayList<Data> getData() {
        return data;
    }

    public void setData(ArrayList<Data> data) {
        this.data = data;
    }

    public void addData(MetricHistory metricHistory) {
        Data data = new Data();
        data.setX(metricHistory.getReadDate().getTime());
        data.setY(metricHistory.getValue());
        this.data.add(data);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("data", getData())
                .toString();
    }
}
