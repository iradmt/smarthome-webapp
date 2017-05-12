package com.netcracker.smarthome.business.services;

import com.netcracker.smarthome.ApplicationContextHolder;
import com.netcracker.smarthome.business.endpoints.IListener;
import com.netcracker.smarthome.dal.repositories.*;
import com.netcracker.smarthome.model.entities.*;
import com.netcracker.smarthome.model.enums.PolicyStatus;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class PolicyService {
    private Set<IListener> listeners;
    private PolicyRepository policyRepository;
    private RuleRepository ruleRepository;
    private ActionRepository actionRepository;
    private ConditionRepository conditionRepository;

    @Autowired
    public PolicyService(PolicyRepository policyRepository, RuleRepository ruleRepository, ActionRepository actionRepository, ConditionRepository conditionRepository) {
        this.policyRepository = policyRepository;
        this.ruleRepository = ruleRepository;
        this.actionRepository = actionRepository;
        this.conditionRepository = conditionRepository;
        initListeners();
    }

    private void initListeners() {
        listeners = new HashSet<>();
        Reflections reflections = new Reflections("com.netcracker.smarthome");
        Set<Class<? extends IListener>> subTypes = reflections.getSubTypesOf(IListener.class);
        for (Class cl : subTypes) {
            String[] name = cl.getName().split("(?<=\\.)");
            String beanName = name[name.length - 1].substring(0, 1).toLowerCase() + name[name.length - 1].substring(1);
            try {
                listeners.add((IListener) ApplicationContextHolder.getApplicationContext().getBean(beanName));
            } catch (Exception ex) {
            }
        }
    }

    public void addListener(IListener listener) {
        listeners.add(listener);
    }

    public void removeListener(IListener listener) {
        listeners.remove(listener);
    }

    private void onSaveOrUpdate(Object object) {
        for (IListener listener : listeners) {
            listener.onSaveOrUpdate(object);
        }
    }

    @Transactional(readOnly = true)
    public List<Policy> getActivePoliciesByHome(long homeId) {
        List<Policy> assignedPolicies = policyRepository.getAssignedActivePoliciesByHome(homeId),
                inlinePolicies;
        inlinePolicies = assignedPolicies.isEmpty() ? policyRepository.getInlineActivePoliciesByHome(homeId) : policyRepository.getInlineActivePoliciesByHome(homeId, assignedPolicies);
        for (Policy policy : inlinePolicies)
            policy.getAssignedObjects().addAll(policyRepository.getInlineObjects(policy));
        assignedPolicies.addAll(inlinePolicies);
        return assignedPolicies;
    }

    @Transactional(readOnly = true)
    public List<Policy> getActivePolicies() {
        List<Policy> assignedPolicies = policyRepository.getAssignedActivePolicies(),
                inlinePolicies;
        inlinePolicies = assignedPolicies.isEmpty() ? policyRepository.getInlineActivePolicies() : policyRepository.getInlineActivePolicies(assignedPolicies);
        for (Policy policy : inlinePolicies)
            policy.getAssignedObjects().addAll(policyRepository.getInlineObjects(policy));
        assignedPolicies.addAll(inlinePolicies);
        return assignedPolicies;
    }

    @Transactional(readOnly = true)
    public List<Policy> getPoliciesByCatalog(Catalog catalog) {
        return policyRepository.getPoliciesByCatalog(catalog);
    }

    @Transactional(readOnly = true)
    public Policy getActiveInitializedPolicy(long policyId) {
        Policy policy = policyRepository.getInitializedPolicy(policyId);
        if (policy == null || policy.getStatus() != PolicyStatus.ACTIVE)
            return null;
        policy.getAssignedObjects().addAll(policyRepository.getInlineObjects(policy));
        if (policy.getAssignedObjects().isEmpty())
            return null;
        return policy;
    }

    @Transactional(readOnly = true)
    public Set<Policy> getPoliciesByObject(SmartObject object) {
        return policyRepository.getPoliciesByObject(object);
    }

    @Transactional
    public void saveAssignment(SmartObject object, Policy policy) {
        policyRepository.saveAssignment(policy, object);
        Policy activePolicy = getActiveInitializedPolicy(policy.getPolicyId());
        if (activePolicy != null)
            onSaveOrUpdate(activePolicy);
    }

    @Transactional
    public void deleteAssignment(SmartObject object, Policy policy) {
        policyRepository.deleteAssignment(policy, object);
        Policy activePolicy = getActiveInitializedPolicy(policy.getPolicyId());
        if (activePolicy != null)
            onSaveOrUpdate(activePolicy);
        else
            onSaveOrUpdate(policy.getPolicyId());
    }

    @Transactional
    public Policy savePolicy(Policy policy) {
        Policy updatedPolicy = policyRepository.update(policy);
        Policy activePolicy = getActiveInitializedPolicy(updatedPolicy.getPolicyId());
        if (activePolicy == null)
            onSaveOrUpdate(policy.getPolicyId());
        else
            onSaveOrUpdate(activePolicy);
        return updatedPolicy;
    }

    @Transactional
    public void deletePolicy(long policyId) {
        Policy activePolicy = getActiveInitializedPolicy(policyId);
        policyRepository.delete(policyId);
        if (activePolicy != null)
            onSaveOrUpdate(policyId);
    }

    @Transactional
    public Rule saveRule(Rule rule) {
        Rule updatedRule = ruleRepository.update(rule);
        return updatedRule;
    }

    @Transactional
    public void deleteRule(Rule rule) {
        ruleRepository.delete(rule.getRuleId());
    }

    @Transactional
    public Action saveAction(Action action) {
        Action savedAction = actionRepository.update(action);
        return savedAction;
    }

    @Transactional
    public void deleteAction(Action action) {
        ruleRepository.delete(action.getActionId());
    }

    @Transactional
    public Condition saveCondition(Condition cndition) {
        Condition savedCondition = conditionRepository.update(cndition);
        return savedCondition;
    }

    @Transactional
    public void deleteCondition(Condition condition) {
        ruleRepository.delete(condition.getNodeId());
    }
}
