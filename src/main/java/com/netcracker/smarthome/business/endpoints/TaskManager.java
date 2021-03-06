package com.netcracker.smarthome.business.endpoints;

import com.netcracker.smarthome.business.endpoints.controllers.PoliciesController;
import com.netcracker.smarthome.business.endpoints.jsonentities.HomeTask;
import com.netcracker.smarthome.model.entities.Policy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

@Component
public class TaskManager implements IListener {
    private static final Logger LOG = LoggerFactory.getLogger(PoliciesController.class);
    private Map<Long, List<HomeTask>> taskMap;
    private Map<Long, Set<String>> updateMap;

    @PostConstruct
    private void init() {
        taskMap = new HashMap<Long, List<HomeTask>>();
        updateMap = new HashMap<Long, Set<String>>();
    }

    public void addUpdateEvent(Long houseId, String updateEvent) {
        if (updateMap.get(houseId) != null) {
            updateMap.get(houseId).add(updateEvent);
        } else {
            updateMap.put(houseId, new HashSet<String>());
            updateMap.get(houseId).add(updateEvent);
        }
    }

    public void addHomeTask(Long smartHouseId, HomeTask homeTask) {
        List<HomeTask> list = taskMap.get(smartHouseId);
        if (list != null) {
            boolean exists = false;
            for (HomeTask task : list) {
                if (task.getAction().equals(homeTask.getAction())) {
                    exists = true;
                    break;
                }
            }
            if (!exists)
                taskMap.get(smartHouseId).add(homeTask);
        } else {
            taskMap.put(smartHouseId, new ArrayList<HomeTask>());
            taskMap.get(smartHouseId).add(homeTask);
        }
    }

    public void onSaveOrUpdate(Object object) {
        Long smartHomeId;
        if (object instanceof Policy) {
            smartHomeId = ((Policy) object).getCatalog().getSmartHome().getSmartHomeId();
            addHomeTask(smartHomeId, new HomeTask("GetPolicy"));
        } else
            LOG.warn("SmartHome not found");
    }

    public Map<Long, List<HomeTask>> getTaskMap() {
        return taskMap;
    }

    public void setTaskMap(Map<Long, List<HomeTask>> taskMap) {
        this.taskMap = taskMap;
    }


    public Map<Long, Set<String>> getUpdateMap() {
        return updateMap;
    }

    public void setUpdateMap(Map<Long, Set<String>> updateMap) {
        this.updateMap = updateMap;
    }
}