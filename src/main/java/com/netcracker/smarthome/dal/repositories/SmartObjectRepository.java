package com.netcracker.smarthome.dal.repositories;

import com.netcracker.smarthome.model.entities.SmartObject;
import org.springframework.stereotype.Repository;

import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

@Repository
public class SmartObjectRepository extends EntityRepository<SmartObject> {
    public SmartObjectRepository() {
        super(SmartObject.class);
    }

    public List<SmartObject> getObjectBySpecId(long smartHomeId, long specId){
        Query query = getManager().createQuery("select so from SmartObject so inner join Metric m on m.object.smartObjectId = so.smartObjectId inner join MetricSpec ms on m.metricSpec.specId = ms.specId  where (ms.specId = :specId and m.smartHome.smartHomeId = :smartHomeId) group by so.smartObjectId");
        query.setParameter("smartHomeId", smartHomeId);
        query.setParameter("specId", specId);
        return query.getResultList();
    }

    public List<SmartObject> getObjectByHomeId(long smartHomeId){
        Query query = getManager().createQuery("select smartObject from SmartObject smartObject where (smartObject.smartHome.smartHomeId = :smartHomeId and smartObject.parentObject = null)");
        query.setParameter("smartHomeId", smartHomeId);
        return query.getResultList();
    }

    public List<SmartObject> getSubObjectByObjectIds(ArrayList<Long> objectId){
        Query query = getManager().createQuery("select smartObject from SmartObject smartObject where smartObject.parentObject.smartObjectId in :objectId");
        query.setParameter("objectId",objectId);
        return query.getResultList();
    }

    public List<SmartObject> getSubobjectsByObjectId(long objectId){
        Query query = getManager().createQuery("select smartObject from SmartObject smartObject where smartObject.parentObject.smartObjectId = :objectId");
        query.setParameter("objectId",objectId);
        return query.getResultList();
    }

    public List<SmartObject> getMetricObjectsByCatalogId(long smartHomeId, long catalogId){
        Query query = getManager().createQuery("select  o\n" +
                "  from  SmartObject o\n" +
                "  where o.smartHome.smartHomeId=:smartHomeId and\n" +
                "        o.catalog.catalogId=:catalogId and\n" +
                "        (o.smartObjectId in\n" +
                "          ( select  m.object.smartObjectId\n" +
                "              from  Metric m) or\n" +
                "         o.smartObjectId in\n" +
                "          ( select  m.subobject.smartObjectId\n" +
                "              from  Metric m))");
        query.setParameter("catalogId", catalogId);
        query.setParameter("smartHomeId", smartHomeId);
        return query.getResultList();
    }

    public SmartObject getObjectById(long smartObjectId) {
        Query query = getManager().createQuery("select smartObject from SmartObject smartObject where smartObject.smartObjectId = :smartObjectId");
        query.setParameter("smartObjectId", smartObjectId);
        List<SmartObject> result = query.getResultList();
        return result.isEmpty() ? null : result.get(0);
    }

    public SmartObject getObjectByExternalKey(long smartHomeId, long externalKey){
        Query query = getManager().createQuery("select so from SmartObject so where (so.smartHome.smartHomeId = :smartHomeId and so.externalKey = :externalKey)");
        query.setParameter("smartHomeId", smartHomeId);
        query.setParameter("externalKey", externalKey);
        List<SmartObject> result = query.getResultList();
        return result.isEmpty() ? null : result.get(0);
    }

    public SmartObject getRootController(long smartHomeId) {
        Query query = getManager().createQuery("select so from SmartObject so where (so.smartHome.smartHomeId = :smartHomeId and so.parentObject.smartObjectId is null)");
        query.setParameter("smartHomeId", smartHomeId);
        List<SmartObject> result = query.getResultList();
        return result.isEmpty() ? null : result.get(0);
    }
}
