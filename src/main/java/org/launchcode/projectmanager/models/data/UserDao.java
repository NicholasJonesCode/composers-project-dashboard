package org.launchcode.projectmanager.models.data;


import org.launchcode.projectmanager.models.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Transactional
@Repository
@Component
public interface UserDao extends CrudRepository<User, Integer> {

    List<User> findByUsername(String username);

}
