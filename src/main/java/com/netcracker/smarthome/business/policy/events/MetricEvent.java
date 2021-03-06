package com.netcracker.smarthome.business.policy.events;

import com.netcracker.smarthome.model.entities.MetricSpec;
import com.netcracker.smarthome.model.entities.SmartObject;

import java.sql.Timestamp;

public class MetricEvent extends PolicyEvent {
    private double value;

    public MetricEvent() {
    }

    public MetricEvent(SmartObject object, SmartObject subobject, Timestamp registryDate, MetricSpec spec, com.netcracker.smarthome.model.entities.Event dbEvent, double value) {
        super(EventType.METRIC, object, subobject, registryDate, spec, dbEvent);
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("Metric event [\n\tregistry time: %s\n\tobject: %s;\n\tsubobject: %s;\n\tmetric type: %s;\n\tvalue: %s\n]",
                getRegistryDate(),
                getObject().getName(),
                getSubobject() == null ? "none" : getSubobject().getName(),
                getSpec().getSpecName(),
                getValue()
        );
    }
}