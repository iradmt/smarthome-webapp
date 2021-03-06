package com.netcracker.smarthome.web.home;

import com.netcracker.smarthome.business.services.HomeService;
import com.netcracker.smarthome.model.entities.SmartHome;
import com.netcracker.smarthome.web.NavigationBean;
import com.netcracker.smarthome.web.alarm.AlarmListBean;
import com.netcracker.smarthome.web.chart.DashboardBean;
import com.netcracker.smarthome.web.common.ContextUtils;
import com.netcracker.smarthome.web.eventhistory.EventHistoryBean;
import com.netcracker.smarthome.web.notification.NotificationBean;
import com.netcracker.smarthome.web.policy.jsf.AssignmentsBean;
import com.netcracker.smarthome.web.policy.jsf.PoliciesBean;
import com.netcracker.smarthome.web.inventory.InventoryBean;
import com.netcracker.smarthome.web.specs.AlarmSpecsBean;
import com.netcracker.smarthome.web.specs.MetricSpecsBean;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import java.util.List;

@ManagedBean
@SessionScoped
public class CurrentUserHomesBean {
    @ManagedProperty(value = "#{homeService}")
    private HomeService homeService;
    private SmartHome currentHome;

    @PostConstruct
    private void init() {
        setCurrentHome(getUserHomes().get(0));
    }

    public SmartHome getCurrentHome() {
        return currentHome;
    }

    public void setCurrentHome(SmartHome currentHome) {
        this.currentHome = currentHome;
        currentHome.setHomeParams(homeService.getHomeParams(currentHome));
    }

    public void setHomeService(HomeService homeService) {
        this.homeService = homeService;
    }

    public List<SmartHome> getUserHomes() {
        return homeService.getHomesList(ContextUtils.getCurrentUser());
    }

    public void switchHome(SmartHome home) {
        setCurrentHome(home);
        ((MetricSpecsBean)ContextUtils.getBean("metricSpecsBean")).changeCurrentHome();
        ((AlarmSpecsBean)ContextUtils.getBean("alarmSpecsBean")).changeCurrentHome();
        ((AlarmListBean)ContextUtils.getBean("alarmListBean")).changeCurrentHome();
        ((InventoryBean)ContextUtils.getBean("inventoryBean")).changeCurrentHome();
        ((PoliciesBean)ContextUtils.getBean("policiesBean")).initialize();
        ((AssignmentsBean)ContextUtils.getBean("assignmentsBean")).initialize();
        ((NotificationBean)ContextUtils.getBean("notificationBean")).init();
        ((EventHistoryBean)ContextUtils.getBean("eventHistoryBean")).init();
        NavigationBean navigation = (NavigationBean)ContextUtils.getBean("navigationBean");
        if (navigation.getPageName().equals("/faces/alarms/alarm"))
            navigation.setPageName("alarms/alarmlist");
        if (navigation.getPageName().equals("/faces/policy/rules"))
            navigation.setPageName("policy/policies");
        ((DashboardBean)ContextUtils.getBean("dashboardBean")).homeChangeListener(home);
    }
}
