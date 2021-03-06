package com.netcracker.smarthome.web.chart;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.netcracker.smarthome.business.chart.options.ChartOptions;
import com.netcracker.smarthome.business.services.ChartService;
import com.netcracker.smarthome.business.chart.tree.obj.wrapper.CatalogWrapper;
import com.netcracker.smarthome.business.chart.tree.obj.wrapper.MetricSpecWrapper;
import com.netcracker.smarthome.business.chart.tree.obj.wrapper.SmartObjectWrapper;
import com.netcracker.smarthome.business.services.CatalogService;
import com.netcracker.smarthome.model.entities.*;
import com.netcracker.smarthome.model.enums.ChartInterval;
import com.netcracker.smarthome.model.enums.ChartRefreshInterval;
import com.netcracker.smarthome.model.enums.ChartType;
import com.netcracker.smarthome.business.chart.configuration.*;
import com.netcracker.smarthome.business.chart.options.Position;
import com.netcracker.smarthome.web.common.ContextUtils;
import com.netcracker.smarthome.web.home.CurrentUserHomesBean;

import org.primefaces.context.RequestContext;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.faces.bean.*;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

@ManagedBean(name = "chartBean")
@ViewScoped
public class ChartBean implements Serializable{
    private static final Logger LOG = LoggerFactory.getLogger(ChartBean.class);

    @ManagedProperty(value = "#{chartService}")
    private ChartService chartService;
    @ManagedProperty(value = "#{catalogService}")
    private CatalogService catalogService;

    private String chartTitle;

    private List<SmartObject> selectedSmartObjects;
    private List<MetricSpec> selectedMetricSpecs;

    private List<Chart> chartConf;

    private ChartConfiguratorImpl chartConfiguratorImpl;

    private ChartType selectedChartType;
    private ChartType[] chartTypes;

    private ChartInterval selectedChartInterval;
    private ChartInterval[] chartIntervals;

    private ChartRefreshInterval selectedChartRefreshInterval;
    private ChartRefreshInterval[] chartRefreshIntervals;


    private String widgetPos;

    private ObjectMapper objectMapper;

    private HashMap<String, TreeNode> treeObjectMap;
    private HashMap<String, TreeNode> treeMetricMap;
    private TreeNode objectRoot;
    private TreeNode metricRoot;

    private ChartTransformator chartTransformator;


    @PostConstruct
    public void init() {
        chartConfiguratorImpl = new ChartConfiguratorImpl();
        chartTransformator = new ChartTransformator();
        selectedMetricSpecs = new ArrayList<MetricSpec>();
        selectedSmartObjects = new ArrayList<SmartObject>();
        objectMapper = new ObjectMapper();
        chartTypes = ChartType.values();
        chartIntervals = ChartInterval.values();
        chartRefreshIntervals = ChartRefreshInterval.values();
        treeObjectMap = new HashMap<String, TreeNode>();
        treeMetricMap = new HashMap<String, TreeNode>();

        Catalog root = catalogService.getRootCatalog("objectsRootCatalog", getCurrentHome().getSmartHomeId());
        objectRoot = new DefaultTreeNode("Catalog", new CatalogWrapper(null, root), null);

        recursionObjectsCatalog(root, objectRoot);

        Catalog metricRootCatalog = catalogService.getRootCatalog("metricSpecsRootCatalog", getCurrentHome().getSmartHomeId());
        metricRoot = new DefaultTreeNode("Catalog", new CatalogWrapper(null, metricRootCatalog), null);

        recursionMetricSpecsCatalog(metricRootCatalog, metricRoot);
    }

    public void buildCatalog() {
        treeObjectMap = new HashMap<String, TreeNode>();
        treeMetricMap = new HashMap<String, TreeNode>();

        Catalog root = catalogService.getRootCatalog("objectsRootCatalog", getCurrentHome().getSmartHomeId());
        objectRoot = new DefaultTreeNode("Catalog", new CatalogWrapper(null, root), null);

        recursionObjectsCatalog(root, objectRoot);

        Catalog metricRootCatalog = catalogService.getRootCatalog("metricSpecsRootCatalog", getCurrentHome().getSmartHomeId());
        metricRoot = new DefaultTreeNode("Catalog", new CatalogWrapper(null, metricRootCatalog), null);

        recursionMetricSpecsCatalog(metricRootCatalog, metricRoot);
    }

    private void recursionObjectsCatalog(Catalog recursiveCatalog, TreeNode treeElement) {
        List<SmartObject> objectArrayList = chartService.getMetricObjectsByCatalogId(getCurrentHome().getSmartHomeId(), recursiveCatalog.getCatalogId());
        ArrayList<TreeNode> defaultTreeNodes = new ArrayList<TreeNode>();
        for (SmartObject smartObject : objectArrayList) {
            if (objectArrayList.contains(smartObject.getParentObject())) {
                if (!isContainParent(defaultTreeNodes, smartObject)) {
                    TreeNode parentObject = new DefaultTreeNode(smartObject.getParentObject().getObjectType().getName(), new SmartObjectWrapper(treeElement, smartObject.getParentObject()), treeElement);
                    TreeNode childObject = new DefaultTreeNode(smartObject.getObjectType().getName(), new SmartObjectWrapper(parentObject, smartObject), parentObject);
                    defaultTreeNodes.add(parentObject);
                    defaultTreeNodes.add(childObject);
                    treeObjectMap.put(parentObject.getRowKey(), parentObject);
                    treeObjectMap.put(childObject.getRowKey(), childObject);
                } else {
                    TreeNode childObject = new DefaultTreeNode(smartObject.getObjectType().getName(), new SmartObjectWrapper(getParentNode(defaultTreeNodes, smartObject), smartObject), getParentNode(defaultTreeNodes, smartObject));
                    defaultTreeNodes.add(childObject);
                    treeObjectMap.put(childObject.getRowKey(), childObject);
                }

            } else {
                defaultTreeNodes.add(new DefaultTreeNode(smartObject.getObjectType().getName(), new SmartObjectWrapper(treeElement, smartObject), treeElement));
                treeObjectMap.put(defaultTreeNodes.get(defaultTreeNodes.size() - 1).getRowKey(), defaultTreeNodes.get(defaultTreeNodes.size() - 1));

            }
        }

        for (Catalog catalog : catalogService.getSubcatalogs(recursiveCatalog)) {
            TreeNode subCatalog = new DefaultTreeNode("catalog", new CatalogWrapper(treeElement, catalog), treeElement);
            treeObjectMap.put(subCatalog.getRowKey(), subCatalog);
            recursionObjectsCatalog(catalog, subCatalog);
        }
    }

    private boolean isContainParent(ArrayList<TreeNode> defaultTreeNodes, SmartObject smartObject) {
        for (TreeNode defaultTreeNode : defaultTreeNodes) {
            if (((SmartObjectWrapper) defaultTreeNode.getData()).getSmartObject().equals(smartObject.getParentObject())) {
                return true;
            }
        }
        return false;
    }

    private TreeNode getParentNode(ArrayList<TreeNode> defaultTreeNodes, SmartObject smartObject) {
        for (TreeNode defaultTreeNode : defaultTreeNodes) {
            if (((SmartObjectWrapper) defaultTreeNode.getData()).getSmartObject().equals(smartObject.getParentObject())) {
                return defaultTreeNode;
            }
        }
        return null;
    }

    private void recursionMetricSpecsCatalog(Catalog recursiveCatalog, TreeNode treeElement) {
        for (Catalog catalog : catalogService.getSubcatalogs(recursiveCatalog)) {
            TreeNode subCatalog = new DefaultTreeNode("catalog", new CatalogWrapper(treeElement, catalog), treeElement);
            treeMetricMap.put(subCatalog.getRowKey(), subCatalog);
            recursionMetricSpecsCatalog(catalog, subCatalog);
            for (MetricSpec metricSpec : chartService.getMetricSpecByCatalogId(catalog)) {
                TreeNode object = new DefaultTreeNode(new MetricSpecWrapper(subCatalog, metricSpec), subCatalog);
                treeMetricMap.put(object.getRowKey(), object);
            }
        }
    }

    public boolean setCharts(Dashboard dashboard) {
        chartConf = chartService.getCharts(dashboard.getDashboardId());
        return true;
    }

    public void treeToTable() {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String property = params.get("data_rowkey");
        TreeNode selectedNode = treeObjectMap.get(property);
        recursiveGetObject(selectedNode);

    }

    private void recursiveGetObject(TreeNode node) {
        List<TreeNode> childNodes = node.getChildren();
        if (!node.getType().equals("catalog") && !node.getType().equals("Controller") && node.getChildren().size() == 0) {
            SmartObject smartObject = ((SmartObjectWrapper) node.getData()).getSmartObject();
            if (!selectedSmartObjects.contains(smartObject)) {
                selectedSmartObjects.add(((SmartObjectWrapper) node.getData()).getSmartObject());
            }
        }
        Iterator<TreeNode> i = childNodes.iterator();
        while (i.hasNext()) {
            TreeNode treeNode = i.next();
            recursiveGetObject(treeNode);
        }
    }

    public void treeToMetric() {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String property = params.get("data_rowkey");
        TreeNode selectedNode = treeMetricMap.get(property);
        recursiveGetMetric(selectedNode);
    }

    private void recursiveGetMetric(TreeNode node) {
        List<TreeNode> childNodes = node.getChildren();
        if (!node.getType().equals("catalog")) {
            MetricSpec metricSpec = ((MetricSpecWrapper) node.getData()).getMetricSpec();
            if (!selectedMetricSpecs.contains(metricSpec)) {
                selectedMetricSpecs.add(((MetricSpecWrapper) node.getData()).getMetricSpec());
            }
        }
        Iterator<TreeNode> i = childNodes.iterator();
        while (i.hasNext()) {
            TreeNode treeNode = i.next();
            recursiveGetMetric(treeNode);
        }
    }

    public void addChart() throws IOException {
        RequestContext context = RequestContext.getCurrentInstance();
        if(selectedSmartObjects.size()==0 || selectedMetricSpecs.size()==0){
            ContextUtils.addErrorMessageToContext("Error during saving changes!");
            context.addCallbackParam("correcy",false);
            return;
        }

        long chartIdLong = chartService.getChartId() + 1;
        String chartType = selectedChartType.toString().toLowerCase();
        String chartInterval = selectedChartInterval.toString();
        long refreshInterval = selectedChartRefreshInterval.getMillisec();
        if (selectedMetricSpecs.size() == 1) {
            chartConfiguratorImpl.setChartConfigurator(new MetricChartConfig(getCurrentHome(), chartIdLong, chartService, refreshInterval, chartType, chartInterval));
        } else if (selectedSmartObjects.size() == 1) {
            chartConfiguratorImpl.setChartConfigurator(new ObjectChartConfig(getCurrentHome(), chartIdLong, chartService, refreshInterval, chartType, chartInterval));
        } else {
            chartConfiguratorImpl.setChartConfigurator(new MultiChartConfig(getCurrentHome(), chartIdLong, getChartTitle(), chartService, refreshInterval, chartType, chartInterval));
        }
        HighchartConfig chartConfigImpl = chartConfiguratorImpl.getConfig(selectedMetricSpecs, selectedSmartObjects);
        ChartOptionsContainer chartOptionsContainer = chartTransformator.toJsonEntity(chartConfigImpl);
        chartService.addChart(chartOptionsContainer.getJsonChartConfig(), chartOptionsContainer.getJsonRequestOptions(), chartConfigImpl.getRefreshInterval(), getCurrentDashBoard());
        setCharts(getCurrentDashBoard());
        context.addCallbackParam("correct", true);

        setDefault();
    }

    public void setDefault() {
        selectedSmartObjects = new ArrayList<SmartObject>();
        selectedMetricSpecs = new ArrayList<MetricSpec>();
        selectedChartRefreshInterval = null;
        chartTitle = "";
    }

    public void saveWidgetPosition() throws IOException {
        List<Position> positions = Arrays.asList(objectMapper.readValue(widgetPos, Position[].class));
        for (int i = 0; i < chartConf.size(); i++) {
            chartConf.get(i).setCol(positions.get(i).getCol());
            chartConf.get(i).setRow(positions.get(i).getRow());
            chartConf.get(i).setSizeX(positions.get(i).getSize_x());
            chartConf.get(i).setSizeY(positions.get(i).getSize_y());
            chartService.updateChart(chartConf.get(i));
        }
    }

    public void removeChart(Chart chart) {
        chartService.removeChart(chart.getChartId());
        setCharts(getCurrentDashBoard());
    }

    public CatalogService getCatalogService() {
        return catalogService;
    }

    public void setCatalogService(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    public ChartService getChartService() {
        return chartService;
    }

    public void setChartService(ChartService chartService) {
        this.chartService = chartService;
    }

    public TreeNode getObjectRoot() {
        return objectRoot;
    }

    public void setObjectRoot(TreeNode objectRoot) {
        this.objectRoot = objectRoot;
    }

    public TreeNode getMetricRoot() {
        return metricRoot;
    }

    public void setMetricRoot(TreeNode metricRoot) {
        this.metricRoot = metricRoot;
    }

    public ChartInterval getSelectedChartInterval() {
        return selectedChartInterval;
    }

    public void setSelectedChartInterval(ChartInterval selectedChartInterval) {
        this.selectedChartInterval = selectedChartInterval;
    }

    public String getWidgetPos() {
        return widgetPos;
    }

    public void setWidgetPos(String widgetPos) {
        this.widgetPos = widgetPos;
    }

    public ChartInterval[] getChartIntervals() {
        return chartIntervals;
    }

    public void setChartIntervals(ChartInterval[] chartIntervals) {
        this.chartIntervals = chartIntervals;
    }

    public ChartType getSelectedChartType() {
        return selectedChartType;
    }

    public void setSelectedChartType(ChartType selectedChartType) {
        this.selectedChartType = selectedChartType;
    }

    public ChartType[] getChartTypes() {
        return chartTypes;
    }

    public void setChartTypes(ChartType[] chartTypes) {
        this.chartTypes = chartTypes;
    }

    public ChartRefreshInterval getSelectedChartRefreshInterval() {
        return selectedChartRefreshInterval;
    }

    public void setSelectedChartRefreshInterval(ChartRefreshInterval selectedChartRefreshInterval) {
        this.selectedChartRefreshInterval = selectedChartRefreshInterval;
    }

    public ChartRefreshInterval[] getChartRefreshIntervals() {
        return chartRefreshIntervals;
    }

    public void setChartRefreshIntervals(ChartRefreshInterval[] chartRefreshIntervals) {
        this.chartRefreshIntervals = chartRefreshIntervals;
    }

    public List<Chart> getChartConf() {
        return chartConf;
    }

    public void setChartConf(List<Chart> chartConf) {
        this.chartConf = chartConf;
    }

    public List<SmartObject> getSelectedSmartObjects() {
        return selectedSmartObjects;
    }

    public void setSelectedSmartObjects(List<SmartObject> selectedSmartObjects) {
        this.selectedSmartObjects = selectedSmartObjects;
    }


    public List<MetricSpec> getSelectedMetricSpecs() {
        return selectedMetricSpecs;
    }

    public void setSelectedMetricSpecs(List<MetricSpec> selectedMetricSpecs) {
        this.selectedMetricSpecs = selectedMetricSpecs;
    }

    public String getChartTitle() {
        return chartTitle;
    }

    public void setChartTitle(String chartTitle) {
        this.chartTitle = chartTitle;
    }

    private SmartHome getCurrentHome() {
        return ((CurrentUserHomesBean) ContextUtils.getBean("currentUserHomesBean")).getCurrentHome();
    }

    public boolean isRendered() {
        return selectedMetricSpecs != null && selectedSmartObjects != null && selectedSmartObjects.size() > 1 && selectedMetricSpecs.size() > 1;
    }

    public Dashboard getCurrentDashBoard() {
        return ((DashboardBean) ContextUtils.getBean("dashboardBean")).getCurrentDashboard();
    }

    public void deleteSelectedObject(SmartObject smartObject) {
        selectedSmartObjects.remove(smartObject);
    }

    public void deleteSelectedMetric(MetricSpec metricSpec) {
        selectedMetricSpecs.remove(metricSpec);
    }


}