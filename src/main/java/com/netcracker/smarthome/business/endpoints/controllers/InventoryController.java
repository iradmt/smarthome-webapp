package com.netcracker.smarthome.business.endpoints.controllers;

import com.netcracker.smarthome.business.HomeService;
import com.netcracker.smarthome.business.endpoints.JsonRestParser;
import com.netcracker.smarthome.business.endpoints.jsonentities.JsonInventoryObject;
import com.netcracker.smarthome.business.endpoints.jsonentities.JsonParameter;
import com.netcracker.smarthome.business.endpoints.services.DataTypeService;
import com.netcracker.smarthome.business.endpoints.services.SmartObjectService;
import com.netcracker.smarthome.business.endpoints.transformators.InventoryTransformator;
import com.netcracker.smarthome.business.endpoints.transformators.ObjectParamTransformator;
import com.netcracker.smarthome.model.entities.ObjectParam;
import com.netcracker.smarthome.model.entities.SmartHome;
import com.netcracker.smarthome.model.entities.SmartObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class InventoryController {
    private static final Logger LOG = LoggerFactory.getLogger(InventoryController.class);

    @Autowired
    private SmartObjectService smartObjectService;

    @Autowired
    private HomeService homeService;

    @Autowired
    private DataTypeService dataTypeService;

    @RequestMapping(value = "/inventories",
                    method = RequestMethod.POST,
                    consumes = "application/json")
    public ResponseEntity sendInventories(@RequestParam(value="houseId", required=true) long houseId,
                                          @RequestBody String json) {
        SmartHome home = homeService.getHomeById(houseId);
        if (home == null)
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        JsonRestParser parser = new JsonRestParser();
        try {
            List<JsonInventoryObject> objects = parser.parseInventory(json);
            InventoryTransformator inventoryTransformator = new InventoryTransformator(smartObjectService, homeService);
            ObjectParamTransformator paramTransformator = new ObjectParamTransformator(dataTypeService, smartObjectService);
            for (JsonInventoryObject object : objects) {
                object.setSmartHomeId(houseId);
                SmartObject smartObject = inventoryTransformator.fromJsonEntity(object);
                smartObjectService.saveInventory(smartObject);
                for (JsonParameter parameter : object.getParameters()) {
                    parameter.setSmartObjectId(smartObject.getSmartObjectId());
                    ObjectParam objectParam = paramTransformator.fromJsonEntity(parameter);
                    smartObjectService.saveObjectParam(objectParam);
                }
            }
        }
        catch (Exception ex) {
            LOG.error("Error during saving of data", ex);
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "/inventories/{objectId}",
                    method = RequestMethod.PUT,
                    consumes = "application/json")
    public ResponseEntity updateInventory(@PathVariable(value = "objectId", required = true) long objectId,
                                          @RequestParam(value = "houseId", required = true) long houseId,
                                          @RequestBody String json) {
        SmartHome home = homeService.getHomeById(houseId);
        if (home == null)
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        JsonRestParser parser = new JsonRestParser();
        try {
            List<JsonInventoryObject> objects = parser.parseInventory(json);
            InventoryTransformator inventoryTransformator = new InventoryTransformator(smartObjectService, homeService);
            ObjectParamTransformator paramTransformator = new ObjectParamTransformator(dataTypeService, smartObjectService);
            for (JsonInventoryObject object : objects) {
                object.setSmartHomeId(houseId);
                SmartObject smartObject = inventoryTransformator.fromJsonEntity(object);
                SmartObject oldObject = smartObjectService.getObjectByExternalKey(houseId, smartObject.getExternalKey());
                if (oldObject != null) {
                    smartObject.setSmartObjectId(oldObject.getSmartObjectId());
                }
                smartObjectService.updateInventory(smartObject);
                for (JsonParameter parameter : object.getParameters()) {
                    parameter.setSmartObjectId(smartObject.getSmartObjectId());
                    ObjectParam objectParam = paramTransformator.fromJsonEntity(parameter);
                    ObjectParam oldParam = smartObjectService.getObjectParamByName(smartObject.getSmartObjectId(), objectParam.getName());
                    if (oldParam != null) {
                        objectParam.setParamId(oldParam.getParamId());
                    }
                    smartObjectService.updateObjectParam(objectParam);
                }
            }
        }
        catch (Exception ex) {
            LOG.error("Error during saving of data", ex);
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "/inventories/{objectId}",
            method = RequestMethod.DELETE,
            consumes = "application/json")
    public ResponseEntity deleteInventory(@PathVariable(value = "objectId", required = true) long objectId,
                                          @RequestParam(value = "houseId", required = true) long houseId) {
        SmartHome home = homeService.getHomeById(houseId);
        if (home == null)
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        try {
            smartObjectService.deleteObject(smartObjectService.getObjectByExternalKey(houseId, objectId).getSmartObjectId());
        }
        catch (Exception ex) {
            LOG.error("Error during deleting of object", ex);
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity(HttpStatus.OK);
    }
}
