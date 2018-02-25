package org.launchcode.projectmanager.models.data;


import org.launchcode.projectmanager.models.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Transactional
@Repository
public interface UserDao extends CrudRepository<User, Integer> {

    List<User> findByUsername(String username);

}
