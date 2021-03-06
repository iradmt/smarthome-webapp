package com.netcracker.smarthome.web.policy.components.conditions;

import com.netcracker.smarthome.web.policy.components.UIParameter;

import java.util.Map;

public class LessThanCondition extends MetricCondition {
    public LessThanCondition(Map<String, String> params) {
        super(params);
    }

    @Override
    protected void initParamsMap(Map<String, String> params) {
        super.initParamsMap(params);
        templateParams.put("@value", new UIParameter(Double.parseDouble(params.get("value"))));
    }

    @Override
    public String getTemplate() {
        return "@metric < @value";
    }
}
