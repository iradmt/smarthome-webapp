package com.netcracker.smarthome.business.policy.services;

import com.netcracker.smarthome.dal.repositories.AlarmRepository;
import com.netcracker.smarthome.dal.repositories.AlarmSpecRepository;
import com.netcracker.smarthome.dal.repositories.UserRepository;
import com.netcracker.smarthome.model.entities.Alarm;
import com.netcracker.smarthome.model.entities.AlarmSpec;
import com.netcracker.smarthome.model.enums.AlarmSeverity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

@Service
public class AlarmService {
    private final AlarmRepository alarmRepository;
    private final UserRepository userRepository;
    private final AlarmSpecRepository alarmSpecRepository;

    @Autowired
    public AlarmService(AlarmRepository alarmRepository, UserRepository userRepository, AlarmSpecRepository alarmSpecRepository) {
        this.alarmRepository = alarmRepository;
        this.userRepository = userRepository;
        this.alarmSpecRepository = alarmSpecRepository;
    }

    @Transactional
    public void clearAll(List<Alarm> alarms, long clearedUserId) {
        Timestamp clearTime = Timestamp.valueOf(LocalDateTime.now());
        for (Alarm alarm : alarms)
            clear(alarm, clearedUserId, clearTime);
    }

    @Transactional
    public void clearAlarm(Alarm alarmProps, boolean withChildren, boolean bySystem) {
        Alarm existingAlarm = alarmRepository.get(alarmProps.getObject(), alarmProps.getSubobject());
        if (existingAlarm == null)
            return;
        long clearedUserId = bySystem ? userRepository.getByEmail("system@system.com").getUserId() : alarmProps.getClearedUserId();
        Timestamp clearTime = Timestamp.valueOf(LocalDateTime.now());
        if (withChildren)
            clearWithChildren(existingAlarm, clearedUserId, clearTime);
        else
            clear(existingAlarm, clearedUserId, clearTime);
    }

    private void clear(Alarm alarm, long clearedUserId, Timestamp clearTime) {
        alarm.setClearedUserId(clearedUserId);
        alarm.setSeverity(AlarmSeverity.CLEARED);
        alarm.setEndTime(clearTime);
        alarmRepository.update(alarm);
    }

    private void clearWithChildren(Alarm alarmToClear, long clearedUserId, Timestamp clearTime) {
        List<Alarm> alarmWithChildren = new LinkedList<Alarm>();
        alarmWithChildren.add(alarmToClear);
        alarmWithChildren.addAll(alarmRepository.getChildAlarmsRecursively(alarmToClear));
        for (Alarm alarm : alarmWithChildren)
            clear(alarm, clearedUserId, clearTime);
    }

    @Transactional
    public void saveRaisedAlarm(Alarm alarm) {
        Alarm existingAlarm = alarmRepository.get(alarm.getObject(), alarm.getSubobject());
        if (existingAlarm != null) {
            alarm.setAlarmId(existingAlarm.getAlarmId());
            if (existingAlarm.getSeverity() != AlarmSeverity.CLEARED) {
                alarm.setStartTime(existingAlarm.getStartTime());
            }
        }
        alarmRepository.update(alarm);
    }

    @Transactional(readOnly = true)
    public AlarmSpec getSpecById(long specId) {
        return alarmSpecRepository.get(specId);
    }
}
