package com.netcracker.smarthome.business.endpoints.controllers;

import com.netcracker.smarthome.business.policy.transform.json.PolicyTransformer;
import com.netcracker.smarthome.business.policy.transform.json.entities.JsonPolicy;
import com.netcracker.smarthome.business.services.PolicyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class PoliciesController {
    private static final Logger LOG = LoggerFactory.getLogger(PoliciesController.class);

    private final PolicyService policyService;
    private PolicyTransformer policyTransformer;

    @Autowired
    public PoliciesController(PolicyService policyService) {
        this.policyService = policyService;
        this.policyTransformer = new PolicyTransformer();
    }

    @RequestMapping(value = "/policies",
            method = RequestMethod.GET,
            produces = "application/json")
    public ResponseEntity<List<JsonPolicy>> getPolicies(@RequestParam(value = "houseId", required = true) long houseId) {
        List<JsonPolicy> jsonPolicies = policyService.getActivePoliciesByHome(houseId)
                .stream()
                .map(policyTransformer::toJsonEntity)
                .collect(Collectors.toList());
        if (jsonPolicies.isEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(jsonPolicies, HttpStatus.OK);
    }
}
