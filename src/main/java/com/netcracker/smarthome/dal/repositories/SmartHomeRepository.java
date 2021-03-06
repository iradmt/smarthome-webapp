package com.netcracker.smarthome.dal.repositories;

import com.netcracker.smarthome.model.entities.HomeParam;
import com.netcracker.smarthome.model.entities.SmartHome;
import com.netcracker.smarthome.model.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.Query;
import java.util.List;

@Repository
public class SmartHomeRepository extends EntityRepository<SmartHome> {
    public SmartHomeRepository() {
        super(SmartHome.class);
    }

    @Autowired
    private UserRepository userRepository;

    public SmartHome getHomeByEmail(String email) {
        User user = userRepository.getByEmail(email);
        return getHomesByUser(user).get(0);
    }

    public List<SmartHome> getHomesByUser(User user) {
        Query query = getManager().createQuery("select distinct sh from SmartHome sh left join fetch sh.homeParams where sh.user=:user");
        query.setParameter("user", user);
        return query.getResultList();
    }

    public List<HomeParam> getParams(SmartHome home) {
        Query query = getManager().createQuery("select hp from HomeParam hp where hp.smartHome = :smartHome");
        query.setParameter("smartHome", home);
        return query.getResultList();
    }
}
