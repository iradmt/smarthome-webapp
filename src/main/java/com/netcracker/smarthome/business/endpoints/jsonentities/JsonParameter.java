package com.netcracker.smarthome.business.endpoints.jsonentities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

public class JsonParameter implements Serializable {
    private String name;
    private String value;
    private String type;
    @JsonIgnore
    private long smartHomeId;
    @JsonIgnore
    private long smartObjectId;

    public JsonParameter() {}

    public JsonParameter(String name) {
        this.name = name;
    }

    public JsonParameter(String name, String value, String type) {
        this.name = name;
        this.value = value;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getSmartHomeId() {
        return smartHomeId;
    }

    public void setSmartHomeId(long smartHomeId) {
        this.smartHomeId = smartHomeId;
    }

    public long getSmartObjectId() {
        return smartObjectId;
    }

    public void setSmartObjectId(long smartObjectId) {
        this.smartObjectId = smartObjectId;
    }
}
