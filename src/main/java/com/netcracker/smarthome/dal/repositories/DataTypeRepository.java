package com.netcracker.smarthome.dal.repositories;

import com.netcracker.smarthome.model.entities.DataType;
import com.netcracker.smarthome.model.enums.Type;
import org.springframework.stereotype.Repository;

import javax.persistence.Query;
import java.util.List;

@Repository
public class DataTypeRepository extends EntityRepository<DataType> {
    public DataTypeRepository() {
        super(DataType.class);
    }

    public DataType getByType(Type type) {
        Query query = getManager().createQuery("select dt from DataType dt where dt.type = :type");
        query.setParameter("type", type);
        List<DataType> result = query.getResultList();
        return result.isEmpty() ? null : result.get(0);
    }

    public DataType getByName(String name) {
        Query query = getManager().createQuery("select dt from DataType dt where lower(dt.name) = :name");
        query.setParameter("name", name);
        List<DataType> result = query.getResultList();
        return result.isEmpty() ? null : result.get(0);
    }
}
