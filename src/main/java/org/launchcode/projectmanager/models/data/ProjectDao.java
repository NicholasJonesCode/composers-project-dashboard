package org.launchcode.projectmanager.models.data;

import org.launchcode.projectmanager.models.Project;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public interface ProjectDao extends CrudRepository<Project, Integer> {

    List<Project> findByUserId(int userId);

    List<Project> findByIsPublic(boolean isPublic);

}
