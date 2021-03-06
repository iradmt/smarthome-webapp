package com.netcracker.smarthome.business.policy.core;

import com.netcracker.smarthome.business.endpoints.IListener;
import com.netcracker.smarthome.business.policy.core.nodes.PolicyNode;
import com.netcracker.smarthome.business.policy.events.PolicyEvent;
import com.netcracker.smarthome.business.policy.transform.core.PolicyConverter;
import com.netcracker.smarthome.business.services.PolicyService;
import com.netcracker.smarthome.model.entities.Policy;
import com.netcracker.smarthome.model.entities.SmartObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

@Component
public class PolicyEngine implements IListener {
    private static final Logger log = LoggerFactory.getLogger(PolicyEngine.class);

    private final PolicyService policyService;
    private Map<PolicyNode, Set<Long>> policies;
    private ReadWriteLock lock;

    @Autowired
    public PolicyEngine(PolicyService policyService) {
        this.policyService = policyService;
        this.lock = new ReentrantReadWriteLock();
    }

    @PostConstruct
    public void initialize() {
        PolicyConverter converter = new PolicyConverter();
        PolicyNode policyNode;
        List<Policy> policyList = policyService.getActivePolicies();
        Set<Long> assignedObjects;
        lock.writeLock().lock();
        try {
            policies = new HashMap<>();
            for (Policy policy : policyList) {
                policyNode = converter.convert(policy);
                assignedObjects = policy.getAssignedObjects().stream().map(SmartObject::getSmartObjectId).collect(Collectors.toSet());
                policies.put(policyNode, assignedObjects);
            }
        } finally {
            lock.writeLock().unlock();
        }
        policyService.addListener(this);
    }

    @Override
    public void onSaveOrUpdate(Object object) {
        Policy policy = (Policy) object;
        Policy activePolicy = policyService.getActiveInitializedPolicy(policy.getPolicyId());
        if (activePolicy != null)
            reinitialize(activePolicy);
        else
            reinitialize(policy.getPolicyId());
    }

    private void reinitialize(long policyToRemove) {
        PolicyNode oldNode;
        lock.writeLock().lock();
        try {
            oldNode = policies.keySet().stream().filter(node -> node.getIdentifier() == policyToRemove).findFirst().orElse(null);
            policies.remove(oldNode);
        } finally {
            lock.writeLock().unlock();
        }
        if (oldNode != null)
            log.info("Policy #{} removed from engine.", policyToRemove);
    }

    private void reinitialize(Policy activePolicy) {
        PolicyConverter converter = new PolicyConverter();
        PolicyNode newNode = converter.convert(activePolicy);
        Set<Long> assignedObjects = activePolicy.getAssignedObjects().stream().map(SmartObject::getSmartObjectId).collect(Collectors.toSet());
        lock.writeLock().lock();
        try {
            PolicyNode oldNode = policies.keySet().stream().filter(node -> node.getIdentifier() == activePolicy.getPolicyId()).findFirst().orElse(null);
            policies.remove(oldNode);
            policies.put(newNode, assignedObjects);
        } finally {
            lock.writeLock().unlock();
        }
        log.info("Policy #{} (re)loaded.", activePolicy.getPolicyId());
    }

    public void handleEvent(PolicyEvent event) {
        lock.readLock().lock();
        try {
            long objectId = event.getSubobject() == null ? event.getObject().getSmartObjectId() : event.getSubobject().getSmartObjectId();
            for (PolicyNode policy : policies.keySet())
                if (policies.get(policy).contains(objectId))
                    policy.handle(event);
        } finally {
            lock.readLock().unlock();
        }
    }
}
